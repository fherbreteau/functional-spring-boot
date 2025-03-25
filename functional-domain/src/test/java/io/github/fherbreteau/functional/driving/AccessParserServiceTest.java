package io.github.fherbreteau.functional.driving;

import static io.github.fherbreteau.functional.domain.entities.AccessRight.executeOnly;
import static io.github.fherbreteau.functional.domain.entities.AccessRight.full;
import static io.github.fherbreteau.functional.domain.entities.AccessRight.none;
import static io.github.fherbreteau.functional.domain.entities.AccessRight.readExecute;
import static io.github.fherbreteau.functional.domain.entities.AccessRight.readOnly;
import static io.github.fherbreteau.functional.domain.entities.AccessRight.readWrite;
import static io.github.fherbreteau.functional.domain.entities.AccessRight.writeExecute;
import static io.github.fherbreteau.functional.domain.entities.AccessRight.writeOnly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Stream;

import io.github.fherbreteau.functional.domain.access.CompositeAccessParserFactory;
import io.github.fherbreteau.functional.domain.access.factory.AccessParserFactory;
import io.github.fherbreteau.functional.domain.access.factory.impl.AddAccessParserFactory;
import io.github.fherbreteau.functional.domain.access.factory.impl.AttributionAccessParserAccessFactory;
import io.github.fherbreteau.functional.domain.access.factory.impl.EveryoneAccessParserFactory;
import io.github.fherbreteau.functional.domain.access.factory.impl.ExecuteAccessParserFactory;
import io.github.fherbreteau.functional.domain.access.factory.impl.FullAccessParserAccessFactory;
import io.github.fherbreteau.functional.domain.access.factory.impl.GroupAccessParserFactory;
import io.github.fherbreteau.functional.domain.access.factory.impl.OtherAccessParserFactory;
import io.github.fherbreteau.functional.domain.access.factory.impl.OwnerAccessParserFactory;
import io.github.fherbreteau.functional.domain.access.factory.impl.ReadAccessParserFactory;
import io.github.fherbreteau.functional.domain.access.factory.impl.RemoveAccessParserFactory;
import io.github.fherbreteau.functional.domain.access.factory.impl.RightAccessParserAccessFactory;
import io.github.fherbreteau.functional.domain.access.factory.impl.SetAccessParserFactory;
import io.github.fherbreteau.functional.domain.access.factory.impl.UnsupportedAccessParserFactory;
import io.github.fherbreteau.functional.domain.access.factory.impl.WriteAccessParserFactory;
import io.github.fherbreteau.functional.domain.entities.AccessRight;
import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.ItemInput;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driving.impl.AccessParserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class AccessParserServiceTest {
    private AccessParserService accessParserService;

    public static Stream<Arguments> shouldMapAnAccessStringToRequiredAccessRight() {
        File fileNoAccess = File.builder()
                .withName("")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        File fileFullAccess = fileNoAccess.copyBuilder()
                .withOwnerAccess(full())
                .withGroupAccess(full())
                .withOtherAccess(full())
                .build();
        return Stream.of(
                Arguments.of("+r", fileNoAccess, readOnly(), null, null),
                Arguments.of("+w", fileNoAccess, writeOnly(), null, null),
                Arguments.of("+x", fileNoAccess, executeOnly(), null, null),
                Arguments.of("-r", fileFullAccess, writeExecute(), null, null),
                Arguments.of("-w", fileFullAccess, readExecute(), null, null),
                Arguments.of("-x", fileFullAccess, readWrite(), null, null),
                Arguments.of("=r", fileNoAccess, readOnly(), null, null),
                Arguments.of("=w", fileNoAccess, writeOnly(), null, null),
                Arguments.of("=x", fileNoAccess, executeOnly(), null, null),
                Arguments.of("g+r", fileNoAccess, null, readOnly(), null),
                Arguments.of("g+w", fileNoAccess, null, writeOnly(), null),
                Arguments.of("g+x", fileNoAccess, null, executeOnly(), null),
                Arguments.of("g-r", fileFullAccess, null, writeExecute(), null),
                Arguments.of("g-w", fileFullAccess, null, readExecute(), null),
                Arguments.of("g-x", fileFullAccess, null, readWrite(), null),
                Arguments.of("g=r", fileNoAccess, null, readOnly(), null),
                Arguments.of("g=w", fileNoAccess, null, writeOnly(), null),
                Arguments.of("g=x", fileNoAccess, null, executeOnly(), null),
                Arguments.of("o+r", fileNoAccess, null, null, readOnly()),
                Arguments.of("o+w", fileNoAccess, null, null, writeOnly()),
                Arguments.of("o+x", fileNoAccess, null, null, executeOnly()),
                Arguments.of("o-r", fileFullAccess, null, null, writeExecute()),
                Arguments.of("o-w", fileFullAccess, null, null, readExecute()),
                Arguments.of("o-x", fileFullAccess, null, null, readWrite()),
                Arguments.of("o=r", fileNoAccess, null, null, readOnly()),
                Arguments.of("o=w", fileNoAccess, null, null, writeOnly()),
                Arguments.of("o=x", fileNoAccess, null, null, executeOnly()),
                Arguments.of("a+r", fileNoAccess, readOnly(), readOnly(), readOnly()),
                Arguments.of("a+w", fileNoAccess, writeOnly(), writeOnly(), writeOnly()),
                Arguments.of("a+x", fileNoAccess, executeOnly(), executeOnly(), executeOnly()),
                Arguments.of("a-r", fileFullAccess, writeExecute(), writeExecute(), writeExecute()),
                Arguments.of("a-w", fileFullAccess, readExecute(), readExecute(), readExecute()),
                Arguments.of("a-x", fileFullAccess, readWrite(), readWrite(), readWrite()),
                Arguments.of("a=r", fileNoAccess, readOnly(), readOnly(), readOnly()),
                Arguments.of("a=w", fileNoAccess, writeOnly(), writeOnly(), writeOnly()),
                Arguments.of("a=x", fileNoAccess, executeOnly(), executeOnly(), executeOnly()),
                Arguments.of("ugo+rwx", fileNoAccess, full(), full(), full()),
                Arguments.of("+rwx", fileNoAccess, full(), null, null),
                Arguments.of("o-rwx", fileFullAccess, null, null, none()),
                Arguments.of("g+x", fileFullAccess, null, full(), null),
                Arguments.of("uo-wx", fileFullAccess, readOnly(), null, readOnly()),
                Arguments.of("go-w", fileFullAccess, null, readExecute(), readExecute())
        );
    }

    public static Stream<Arguments> shouldThrowAnIllegalStateException() {
        File file = File.builder()
                .withName("")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        return Stream.of(
                Arguments.of(null, file),
                Arguments.of("", file),
                Arguments.of("a", file),
                Arguments.of("g", file),
                Arguments.of("r", file),
                Arguments.of("u", file),
                Arguments.of("w", file),
                Arguments.of("x", file),
                Arguments.of("+rwx", null),
                Arguments.of("l", file)
        );
    }

    @BeforeEach
    void setup() {
        List<AccessParserFactory> accessRightParserFactories = List.of(
                new AddAccessParserFactory(),
                new AttributionAccessParserAccessFactory(),
                new EveryoneAccessParserFactory(),
                new UnsupportedAccessParserFactory(),
                new ExecuteAccessParserFactory(),
                new FullAccessParserAccessFactory(),
                new GroupAccessParserFactory(),
                new OtherAccessParserFactory(),
                new OwnerAccessParserFactory(),
                new ReadAccessParserFactory(),
                new RemoveAccessParserFactory(),
                new RightAccessParserAccessFactory(),
                new SetAccessParserFactory(),
                new WriteAccessParserFactory()
        );
        CompositeAccessParserFactory accessRightParserFactory = new CompositeAccessParserFactory(accessRightParserFactories);
        accessRightParserFactory.configureRecursive();
        accessParserService = new AccessParserServiceImpl(accessRightParserFactory);
    }

    @ParameterizedTest(name = "{0} should be parsed to {2}{3}{4}")
    @MethodSource
    void shouldMapAnAccessStringToRequiredAccessRight(String right, Item itemToUpdate, AccessRight owner, AccessRight group, AccessRight other) {
        ItemInput itemInput = accessParserService.parseAccessRights(right, itemToUpdate);
        assertThat(itemInput.getOwnerAccess()).isEqualTo(owner);
        assertThat(itemInput.getGroupAccess()).isEqualTo(group);
        assertThat(itemInput.getOtherAccess()).isEqualTo(other);
    }

    @ParameterizedTest(name = "{0} should not be parsable")
    @MethodSource
    void shouldThrowAnIllegalStateException(String right, Item itemToUpdate) {
        assertThatThrownBy(() -> accessParserService.parseAccessRights(right, itemToUpdate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Access right is not valid");
    }
}

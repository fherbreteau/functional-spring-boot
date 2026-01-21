package io.github.fherbreteau.functional.domain.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import io.github.fherbreteau.functional.domain.command.factory.ItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.UserCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.*;
import io.github.fherbreteau.functional.domain.command.impl.check.*;
import io.github.fherbreteau.functional.domain.entities.UserCommandType;
import io.github.fherbreteau.functional.domain.entities.UserInput;
import io.github.fherbreteau.functional.driven.PasswordProtector;
import io.github.fherbreteau.functional.driven.repository.GroupRepository;
import io.github.fherbreteau.functional.driven.repository.UserRepository;
import io.github.fherbreteau.functional.driven.rules.UserChecker;
import io.github.fherbreteau.functional.driven.rules.UserUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompositeUserCommandFactoryTest {
    private CompositeUserCommandFactory factory;
    @Mock
    private UserChecker userChecker;
    @Mock
    private UserUpdater userUpdater;
    @Mock
    private PasswordProtector passwordProtector;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;

    public static Stream<Arguments> validCommandArguments() {
        return Stream.of(
                // ID Command
                Arguments.of(UserCommandType.ID, UserInput.builder(null).withUserId(UUID.randomUUID()).build(), CheckGetUserCommand.class),
                Arguments.of(UserCommandType.ID, UserInput.builder("user").build(), CheckGetUserCommand.class),
                Arguments.of(UserCommandType.ID, UserInput.builder(null).build(), CheckGetUserCommand.class),
                // USERADD Command
                Arguments.of(UserCommandType.USERADD, UserInput.builder("user").build(), CheckCreateUserCommand.class),
                Arguments.of(UserCommandType.USERADD, UserInput.builder("user").withUserId(UUID.randomUUID()).build(), CheckCreateUserCommand.class),
                Arguments.of(UserCommandType.USERADD, UserInput.builder("user").withGroupId(UUID.randomUUID()).build(), CheckCreateUserCommand.class),
                Arguments.of(UserCommandType.USERADD, UserInput.builder("user").withGroups(List.of("group")).build(), CheckCreateUserCommand.class),
                Arguments.of(UserCommandType.USERADD, UserInput.builder("user").withPassword("password").build(), CheckCreateUserCommand.class),
                // USERMOD Command
                Arguments.of(UserCommandType.USERMOD, UserInput.builder("user").withGroups(List.of("group")).build(), CheckUpdateUserCommand.class),
                Arguments.of(UserCommandType.USERMOD, UserInput.builder("user").withGroupId(UUID.randomUUID()).build(), CheckUpdateUserCommand.class),
                Arguments.of(UserCommandType.USERMOD, UserInput.builder("user").withUserId(UUID.randomUUID()).build(), CheckUpdateUserCommand.class),
                Arguments.of(UserCommandType.USERMOD, UserInput.builder("user").withNewName("user2").build(), CheckUpdateUserCommand.class),
                Arguments.of(UserCommandType.USERMOD, UserInput.builder("user").withPassword("password").build(), CheckUpdateUserCommand.class),
                // USERDEL Command
                Arguments.of(UserCommandType.USERDEL, UserInput.builder("user").build(), CheckDeleteUserCommand.class),
                Arguments.of(UserCommandType.USERDEL, UserInput.builder("user").withForce(true).build(), CheckDeleteUserCommand.class),
                // PASSWD Command
                Arguments.of(UserCommandType.PASSWD, UserInput.builder("user").withPassword("password").build(), CheckUpdateUserCommand.class),
                // GROUPS Command
                Arguments.of(UserCommandType.GROUPS, UserInput.builder(null).build(), CheckGetGroupCommand.class),
                Arguments.of(UserCommandType.GROUPS, UserInput.builder("user").build(), CheckGetGroupCommand.class),
                Arguments.of(UserCommandType.GROUPS, UserInput.builder(null).withGroupId(UUID.randomUUID()).build(), CheckGetGroupCommand.class),
                // GROUPADD Command
                Arguments.of(UserCommandType.GROUPADD, UserInput.builder("group").build(), CheckCreateGroupCommand.class),
                Arguments.of(UserCommandType.GROUPADD, UserInput.builder("group").withGroupId(UUID.randomUUID()).build(), CheckCreateGroupCommand.class),
                // GROUPMOD Command
                Arguments.of(UserCommandType.GROUPMOD, UserInput.builder("group").withNewName("group").build(), CheckUpdateGroupCommand.class),
                Arguments.of(UserCommandType.GROUPMOD, UserInput.builder("group").withGroupId(UUID.randomUUID()).build(), CheckUpdateGroupCommand.class),
                // GROUPDEL Command
                Arguments.of(UserCommandType.GROUPDEL, UserInput.builder("group").build(), CheckDeleteGroupCommand.class),
                Arguments.of(UserCommandType.GROUPDEL, UserInput.builder("group").withForce(true).build(), CheckDeleteGroupCommand.class)
        );
    }

    public static Stream<Arguments> invalidCommandArguments() {
        return Stream.of(
                Arguments.of(UserCommandType.ID, UserInput.builder("user").withUserId(UUID.randomUUID()).build()),
                Arguments.of(UserCommandType.USERADD, UserInput.builder(null).build()),
                Arguments.of(UserCommandType.USERMOD, UserInput.builder(null).build()),
                Arguments.of(UserCommandType.USERMOD, UserInput.builder("name").build()),
                Arguments.of(UserCommandType.USERDEL, UserInput.builder(null).build()),
                Arguments.of(UserCommandType.PASSWD, UserInput.builder(null).build()),
                Arguments.of(UserCommandType.PASSWD, UserInput.builder("user").build()),
                Arguments.of(UserCommandType.GROUPS, UserInput.builder("user").withUserId(UUID.randomUUID()).build()),
                Arguments.of(UserCommandType.GROUPADD, UserInput.builder(null).build()),
                Arguments.of(UserCommandType.GROUPMOD, UserInput.builder(null).build()),
                Arguments.of(UserCommandType.GROUPMOD, UserInput.builder("name").build()),
                Arguments.of(UserCommandType.GROUPDEL, UserInput.builder(null).build())
        );
    }

    @BeforeEach
    void setup() {
        List<UserCommandFactory<?>> factories = List.of(
                new CreateGroupCommandFactory(),
                new DeleteGroupCommandFactory(),
                new GetGroupCommandFactory(),
                new UpdateGroupCommandFactory(),
                new UnsupportedUserCommandFactory(),
                new CreateUserCommandFactory(),
                new DeleteUserCommandFactory(),
                new GetUserCommandFactory(),
                new UpdateUserCommandFactory()
        );
        factory = new CompositeUserCommandFactory(userRepository, groupRepository, userChecker, userUpdater,
                passwordProtector, factories);
    }

    @ParameterizedTest(name = "Command of {0} with args {1} is supported")
    @MethodSource("validCommandArguments")
    void testCommandCreatedForSpecificTypeAndValidInput(UserCommandType type, UserInput itemInput, Class<? extends CheckCommand<?>> expected) {
        Command<?> command = factory.createCommand(type, itemInput);

        assertThat(command).isNotNull().isInstanceOf(expected);
    }

    @ParameterizedTest(name = "Command of {0} with args {1} is not supported")
    @MethodSource("invalidCommandArguments")
    void testCommandCreatedForSpecificTypeAndInvalidInput(UserCommandType type, UserInput itemInput) {
        Command<?> command = factory.createCommand(type, itemInput);

        assertThat(command).isNotNull().isInstanceOf(CheckUnsupportedUserCommand.class);
    }

    @Test
    void testOrderOfCommandFactoriesIsRespected() {
        List<ItemCommandFactory<?>> factories = List.of(
                new UnsupportedItemCommandFactory(),
                new ListChildrenCommandFactory(),
                new UploadCommandFactory()
        );
        List<ItemCommandFactory<?>> sortedFactories = factories.stream().sorted(Comparator.comparing(ItemCommandFactory::order)).toList();
        assertThat(sortedFactories).last(type(ItemCommandFactory.class))
                .isInstanceOf(UnsupportedItemCommandFactory.class);

    }
}

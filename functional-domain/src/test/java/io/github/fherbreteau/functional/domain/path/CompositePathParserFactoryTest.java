package io.github.fherbreteau.functional.domain.path;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import io.github.fherbreteau.functional.domain.entities.Failure;
import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.Folder;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Path;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.domain.path.factory.PathParserFactory;
import io.github.fherbreteau.functional.domain.path.factory.impl.ComplexSegmentPathParserPathFactory;
import io.github.fherbreteau.functional.domain.path.factory.impl.CurrentSegmentPathParserFactory;
import io.github.fherbreteau.functional.domain.path.factory.impl.EmptySegmentPathParserFactory;
import io.github.fherbreteau.functional.domain.path.factory.impl.InvalidPathParserFactory;
import io.github.fherbreteau.functional.domain.path.factory.impl.ParentSegmentPathParserFactory;
import io.github.fherbreteau.functional.domain.path.factory.impl.SingleSegmentPathParserFactory;
import io.github.fherbreteau.functional.domain.path.impl.InvalidPathParser;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.rules.AccessChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompositePathParserFactoryTest {
    private CompositePathParserFactory compositePathParserFactory;
    @Mock
    private ItemRepository repository;
    @Mock
    private AccessChecker accessChecker;
    @Mock
    private User actor;

    public static Stream<Arguments> validPathArguments() {
        Path folder = Path.success(Folder.builder()
                .withName("folder")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .build());
        Path file = Path.success(File.builder()
                .withName("file")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .build());
        return Stream.of(
                // Element checker
                Arguments.of(Path.ROOT, "file"),
                Arguments.of(Path.ROOT, "folder"),
                Arguments.of(Path.ROOT, "."),
                Arguments.of(folder, ".."),
                Arguments.of(folder, "."),
                // Start checker
                Arguments.of(Path.ROOT, "/file"),
                Arguments.of(Path.ROOT, "/folder"),
                // Sub Element
                Arguments.of(Path.ROOT, "folder/file"),
                Arguments.of(file, "../folder")
        );
    }

    public static Stream<Arguments> invalidPathArguments() {
        Path folder = Path.success(Folder.builder()
                .withName("folder")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .build());
        Path file = Path.success(File.builder()
                .withName("file")
                .withParent(folder.getAsFolder())
                .withOwner(User.root())
                .withGroup(Group.root())
                .build());
        Path error = Path.error(Failure.failure("error"));
        return Stream.of(
                // Go up
                Arguments.of(Path.ROOT, ".."),
                // Element after file
                Arguments.of(file, "folder"),
                // Element after error
                Arguments.of(error, ".")
        );
    }

    @BeforeEach
    void setup() {
        List<PathParserFactory> pathFactories = List.of(
                new ParentSegmentPathParserFactory(),
                new CurrentSegmentPathParserFactory(),
                new EmptySegmentPathParserFactory(),
                new SingleSegmentPathParserFactory(),
                new ComplexSegmentPathParserPathFactory(),
                new InvalidPathParserFactory()
        );
        compositePathParserFactory = new CompositePathParserFactory(repository, accessChecker, pathFactories);
        compositePathParserFactory.configureRecursive();
    }

    @ParameterizedTest
    @MethodSource("validPathArguments")
    void testParserCreatedForValidPathAndRoot(Path currentPath, String path) {
        // WHEN
        PathParser parser = compositePathParserFactory.createParser(currentPath, path);
        // THEN
        assertThat(parser).isNotNull().isNotInstanceOf(InvalidPathParser.class);
    }

    @ParameterizedTest
    @MethodSource("invalidPathArguments")
    void testParserCreatedForInvalidPathAndRoot(Path currentPath, String path) {
        // WHEN
        PathParser parser = compositePathParserFactory.createParser(currentPath, path);
        // THEN
        assertThat(parser).isNotNull().isInstanceOf(InvalidPathParser.class);
    }

    @Test
    void testCreatedParserWillResolveAComplexPath() {
        // GIVEN
        given(accessChecker.canExecute(any(), eq(actor))).willReturn(true);
        File file1 = File.builder()
                .withName("file")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        given(repository.findByNameAndParentAndUser("file", Folder.getRoot(), actor)).willReturn(of(file1));
        Folder folder = Folder.builder()
                .withName("folder")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        given(repository.findByNameAndParentAndUser("folder", Folder.getRoot(), actor)).willReturn(of(folder));
        File file2 = File.builder()
                .withName("file")
                .withParent(folder)
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        given(repository.findByNameAndParentAndUser("file", folder, actor)).willReturn(of(file2));
        // WHEN
        PathParser parser = compositePathParserFactory.createParser(Path.ROOT, "/file/../folder/./file");
        // THEN
        assertThat(parser).isNotNull().isNotInstanceOf(InvalidPathParser.class);

        // WHEN
        Path resolved = parser.resolve(actor);
        // THEN
        assertThat(resolved).isNotNull().extracting(Path::isError, BOOLEAN).isFalse();
        assertThat(resolved).extracting(Path::isItemFile, BOOLEAN).isTrue();
        assertThat(resolved).extracting(Path::getItem).isEqualTo(file2);
    }

    @Test
    void testCreatedParserWillResolveAComplexPathWithFolder() {
        // GIVEN
        given(accessChecker.canExecute(any(), eq(actor))).willReturn(true);
        File file1 = File.builder()
                .withName("file")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        given(repository.findByNameAndParentAndUser("file", Folder.getRoot(), actor)).willReturn(of(file1));
        Folder folder1 = Folder.builder()
                .withName("folder")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        given(repository.findByNameAndParentAndUser("folder", Folder.getRoot(), actor)).willReturn(of(folder1));
        Folder folder2 = Folder.builder()
                .withName("folder")
                .withParent(folder1)
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        given(repository.findByNameAndParentAndUser("folder", folder1, actor)).willReturn(of(folder2));
        // WHEN
        PathParser parser = compositePathParserFactory.createParser(Path.ROOT, "/file/../folder/./folder/");
        // THEN
        assertThat(parser).isNotNull().isNotInstanceOf(InvalidPathParser.class);

        // WHEN
        Path resolved = parser.resolve(actor);
        // THEN
        assertThat(resolved).isNotNull().extracting(Path::isError, BOOLEAN).isFalse();
        assertThat(resolved).extracting(Path::isItemFolder, BOOLEAN).isTrue();
        assertThat(resolved).extracting(Path::getItem).isEqualTo(folder2);
    }

    @Test
    void testOrderOfPathFactoriesIsRespected() {
        List<PathParserFactory> factories = List.of(
                new InvalidPathParserFactory(),
                new CurrentSegmentPathParserFactory(),
                new SingleSegmentPathParserFactory()
        );
        List<PathParserFactory> sortedFactories = factories.stream().sorted(Comparator.comparing(PathParserFactory::order)).toList();
        assertThat(sortedFactories).last(type(PathParserFactory.class))
                .isInstanceOf(InvalidPathParserFactory.class);

    }
}

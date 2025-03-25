package io.github.fherbreteau.functional.domain.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import io.github.fherbreteau.functional.domain.command.factory.ItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.ChangeGroupCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.ChangeModeCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.ChangeOwnerCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.CopyItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.CreateItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.DeleteItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.DownloadCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.ListChildrenCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.MoveItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.UnsupportedItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.impl.UploadCommandFactory;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckChangeGroupCommand;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckChangeModeCommand;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckChangeOwnerCommand;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckCopyItemCommand;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckCreateFileCommand;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckCreateFolderCommand;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckDeleteItemCommand;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckDownloadCommand;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckListChildrenCommand;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckMoveItemCommand;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckUnsupportedItemCommand;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckUploadCommand;
import io.github.fherbreteau.functional.domain.entities.AccessRight;
import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.Folder;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.ItemCommandType;
import io.github.fherbreteau.functional.domain.entities.ItemInput;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.ContentRepository;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.rules.AccessChecker;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompositeItemCommandFactoryTest {
    private CompositeItemCommandFactory factory;
    @Mock
    private AccessChecker accessChecker;
    @Mock
    private AccessUpdater accessUpdater;
    @Mock
    private ItemRepository repository;
    @Mock
    private ContentRepository contentRepository;

    public static Stream<Arguments> validCommandArguments() {
        Folder folder = mock(Folder.class);
        File file = mock(File.class);
        File newFile = mock(File.class);
        User user = mock(User.class);
        Group group = mock(Group.class);
        InputStream stream = mock(InputStream.class);
        return Stream.of(
                Arguments.of(ItemCommandType.TOUCH, ItemInput.builder(folder).withName("item").build(), CheckCreateFileCommand.class),
                Arguments.of(ItemCommandType.MKDIR, ItemInput.builder(folder).withName("item").build(), CheckCreateFolderCommand.class),
                Arguments.of(ItemCommandType.LIST, ItemInput.builder(folder).build(), CheckListChildrenCommand.class),
                Arguments.of(ItemCommandType.CHOWN, ItemInput.builder(file).withUser(user).build(), CheckChangeOwnerCommand.class),
                Arguments.of(ItemCommandType.CHGRP, ItemInput.builder(file).withGroup(group).build(), CheckChangeGroupCommand.class),
                Arguments.of(ItemCommandType.CHMOD, ItemInput.builder(file).withOwnerAccess(AccessRight.full()).build(), CheckChangeModeCommand.class),
                Arguments.of(ItemCommandType.DOWNLOAD, ItemInput.builder(file).build(), CheckDownloadCommand.class),
                Arguments.of(ItemCommandType.UPLOAD, ItemInput.builder(file).withContent(stream).withContentType("content").build(), CheckUploadCommand.class),
                Arguments.of(ItemCommandType.DELETE, ItemInput.builder(folder).build(), CheckDeleteItemCommand.class),
                Arguments.of(ItemCommandType.DELETE, ItemInput.builder(file).build(), CheckDeleteItemCommand.class),
                Arguments.of(ItemCommandType.COPY, ItemInput.builder(file).withDestination(newFile).build(), CheckCopyItemCommand.class),
                Arguments.of(ItemCommandType.COPY, ItemInput.builder(file).withDestination(folder).build(), CheckCopyItemCommand.class),
                Arguments.of(ItemCommandType.MOVE, ItemInput.builder(file).withDestination(newFile).build(), CheckMoveItemCommand.class),
                Arguments.of(ItemCommandType.MOVE, ItemInput.builder(file).withDestination(folder).build(), CheckMoveItemCommand.class)
        );
    }

    public static Stream<Arguments> invalidCommandArguments() {
        Folder folder = mock(Folder.class);
        File file = mock(File.class);
        InputStream stream = mock(InputStream.class);
        return Stream.of(
                Arguments.of(ItemCommandType.TOUCH, ItemInput.builder(file).withName("item").build()),
                Arguments.of(ItemCommandType.TOUCH, ItemInput.builder(folder).build()),
                Arguments.of(ItemCommandType.MKDIR, ItemInput.builder(file).withName("item").build()),
                Arguments.of(ItemCommandType.LIST, ItemInput.builder(file).build()),
                Arguments.of(ItemCommandType.CHOWN, ItemInput.builder(file).build()),
                Arguments.of(ItemCommandType.CHGRP, ItemInput.builder(file).build()),
                Arguments.of(ItemCommandType.CHMOD, ItemInput.builder(file).build()),
                Arguments.of(ItemCommandType.DOWNLOAD, ItemInput.builder(folder).build()),
                Arguments.of(ItemCommandType.UPLOAD, ItemInput.builder(folder).withContent(stream).withContentType("content").build()),
                Arguments.of(ItemCommandType.UPLOAD, ItemInput.builder(file).withContentType("content").build()),
                Arguments.of(ItemCommandType.UPLOAD, ItemInput.builder(file).withContent(stream).build()),
                Arguments.of(ItemCommandType.COPY, ItemInput.builder(file).withDestination(file).build()),
                Arguments.of(ItemCommandType.COPY, ItemInput.builder(folder).withDestination(folder).build()),
                Arguments.of(ItemCommandType.COPY, ItemInput.builder(folder).build()),
                Arguments.of(ItemCommandType.MOVE, ItemInput.builder(file).withDestination(file).build()),
                Arguments.of(ItemCommandType.MOVE, ItemInput.builder(folder).withDestination(folder).build()),
                Arguments.of(ItemCommandType.MOVE, ItemInput.builder(folder).build()),

                Arguments.of(ItemCommandType.MKDIR, ItemInput.builder(null).withName("item").build()),
                Arguments.of(ItemCommandType.LIST, ItemInput.builder(null).build()),
                Arguments.of(ItemCommandType.CHOWN, ItemInput.builder(null).build()),
                Arguments.of(ItemCommandType.CHGRP, ItemInput.builder(null).build()),
                Arguments.of(ItemCommandType.CHMOD, ItemInput.builder(null).build()),
                Arguments.of(ItemCommandType.DOWNLOAD, ItemInput.builder(null).build()),
                Arguments.of(ItemCommandType.UPLOAD, ItemInput.builder(null).withContent(stream).withContentType("content").build()),
                Arguments.of(ItemCommandType.DELETE, ItemInput.builder(null).build()),
                Arguments.of(ItemCommandType.COPY, ItemInput.builder(null).build()),
                Arguments.of(ItemCommandType.COPY, ItemInput.builder(null).build()),
                Arguments.of(ItemCommandType.MOVE, ItemInput.builder(null).build()),
                Arguments.of(ItemCommandType.MOVE, ItemInput.builder(null).build())
        );
    }

    @BeforeEach
    void setup() {
        List<ItemCommandFactory<?>> factories = List.of(
                new ChangeOwnerCommandFactory(),
                new ChangeGroupCommandFactory(),
                new ChangeModeCommandFactory(),
                new CreateItemCommandFactory(),
                new DeleteItemCommandFactory(),
                new DownloadCommandFactory(),
                new ListChildrenCommandFactory(),
                new UploadCommandFactory(),
                new CopyItemCommandFactory(),
                new MoveItemCommandFactory(),
                new UnsupportedItemCommandFactory());
        factory = new CompositeItemCommandFactory(repository, contentRepository, accessChecker, accessUpdater, factories);
    }

    @ParameterizedTest(name = "Command of {0} with args {1} is supported")
    @MethodSource("validCommandArguments")
    void testCommandCreatedForSpecificTypeAndValidInput(ItemCommandType type, ItemInput itemInput,
                                                        Class<? extends CheckCommand<?>> expected) {
        Command<?> command = factory.createCommand(type, itemInput);

        assertThat(command).isNotNull().isInstanceOf(expected);
    }

    @ParameterizedTest(name = "Command of {0} with args {1} is not supported")
    @MethodSource("invalidCommandArguments")
    void testCommandCreatedForSpecificTypeAndInvalidInput(ItemCommandType type, ItemInput itemInput) {
        Command<?> command = factory.createCommand(type, itemInput);

        assertThat(command).isNotNull().isInstanceOf(CheckUnsupportedItemCommand.class);
    }

    @Test
    void testOrderOfCommandFactoriesIsRespected() {
        List<ItemCommandFactory<?>> factories = List.of(
                new UnsupportedItemCommandFactory(),
                new ListChildrenCommandFactory(),
                new UploadCommandFactory()
        );
        List<ItemCommandFactory<?>> sortedFactories = factories.stream()
                .sorted(Comparator.comparing(ItemCommandFactory::order))
                .toList();
        assertThat(sortedFactories).last(type(ItemCommandFactory.class))
                .isInstanceOf(UnsupportedItemCommandFactory.class);

    }
}

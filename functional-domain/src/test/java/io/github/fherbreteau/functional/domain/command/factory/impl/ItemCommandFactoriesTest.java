package io.github.fherbreteau.functional.domain.command.factory.impl;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.factory.ItemCommandFactory;
import io.github.fherbreteau.functional.domain.entities.*;
import io.github.fherbreteau.functional.driven.rules.AccessChecker;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;
import io.github.fherbreteau.functional.driven.repository.ContentRepository;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static io.github.fherbreteau.functional.domain.entities.Output.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ItemCommandFactoriesTest {

    @Mock
    private ItemRepository repository;
    @Mock
    private ContentRepository contentRepository;
    @Mock
    private AccessChecker accessChecker;
    @Mock
    private AccessUpdater accessUpdater;
    @Mock
    private User actor;

    @Test
    void shouldCreateAFileWithTheGivenName() {
        ItemCommandFactory<Item> factory = new CreateItemCommandFactory();
        Folder folder = Folder.builder()
                .withName("folder")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        ItemInput itemInput = ItemInput.builder(folder).withName("file").build();

        given(accessChecker.canWrite(folder, actor)).willReturn(true);
        given(repository.exists(folder, "file")).willReturn(false);
        given(repository.create(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(accessUpdater.createItem(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(contentRepository.initContent(any())).willAnswer(invocation -> success(invocation.getArgument(0)));
        given(actor.getGroup()).willReturn(Group.root());

        Command<Command<Output<Item>>> checker = factory.createCommand(repository, contentRepository, accessChecker,
                accessUpdater, ItemCommandType.TOUCH, itemInput);
        Command<Output<Item>> executor = checker.execute(actor);
        Output<Item> output = executor.execute(actor);

        assertThat(output).extracting(Output::getValue, type(File.class))
                .isNotNull()
                .extracting(AbstractItem::getPath)
                .isEqualTo("/folder/file");
    }

    @Test
    void shouldChangeTheAccessOfTheFileWithTheGivenAccessRight() {
        ItemCommandFactory<Item> factory = new ChangeModeCommandFactory();
        File file = File.builder()
                .withName("file")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        ItemInput itemInput = ItemInput.builder(file)
                .withOwnerAccess(AccessRight.full())
                .withGroupAccess(AccessRight.readExecute())
                .withOtherAccess(AccessRight.executeOnly())
                .build();

        given(accessChecker.canChangeMode(file, actor)).willReturn(true);
        given(accessUpdater.updateOwnerAccess(any(), any())).willAnswer(invocation -> invocation.getArgument(0));
        given(accessUpdater.updateGroupAccess(any(), any())).willAnswer(invocation -> invocation.getArgument(0));
        given(accessUpdater.updateOtherAccess(any(), any())).willAnswer(invocation -> invocation.getArgument(0));
        given(repository.update(any())).willAnswer(invocation -> invocation.getArgument(0));

        Command<Command<Output<Item>>> checker = factory.createCommand(repository, contentRepository, accessChecker,
                accessUpdater, ItemCommandType.CHMOD, itemInput);
        Command<Output<Item>> executor = checker.execute(actor);
        Output<Item> output = executor.execute(actor);

        assertThat(output).extracting(Output::getValue, type(File.class))
                .isNotNull()
                .satisfies(i -> {
                    assertThat(i.getOwnerAccess()).isEqualTo(AccessRight.full());
                    assertThat(i.getGroupAccess()).isEqualTo(AccessRight.readExecute());
                    assertThat(i.getOtherAccess()).isEqualTo(AccessRight.executeOnly());
                });
    }

    @Test
    void shouldUpdateTheContentTypeOfTheFileWithTheGivenContentType() {
        ItemCommandFactory<Item> factory = new UploadCommandFactory();
        File file = File.builder()
                .withName("file")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .withContentType("oldValue")
                .build();
        ItemInput itemInput = ItemInput.builder(file)
                .withContentType("newValue")
                .withContent(InputStream.nullInputStream())
                .build();

        given(accessChecker.canWrite(file, actor)).willReturn(true);
        given(repository.update(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(contentRepository.writeContent(any(), any())).willAnswer(invocation -> success(invocation.getArgument(0)));

        Command<Command<Output<Item>>> checker = factory.createCommand(repository, contentRepository, accessChecker,
                accessUpdater, ItemCommandType.UPLOAD, itemInput);
        Command<Output<Item>> executor = checker.execute(actor);
        Output<Item> output = executor.execute(actor);

        assertThat(output).extracting(Output::getValue, type(File.class))
                .isNotNull()
                .extracting(File::getContentType)
                .isEqualTo("newValue");
    }

    @Test
    void shouldDeleteTheGivenItem() {
        ItemCommandFactory<Void> factory = new DeleteItemCommandFactory();
        Folder folder = Folder.builder()
                .withName("folder")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        ItemInput itemInput = ItemInput.builder(folder).build();

        given(accessChecker.canWrite(Folder.getRoot(), actor)).willReturn(true);
        willDoNothing().given(repository).delete(any());
        willDoNothing().given(accessUpdater).deleteItem(any());

        Command<Command<Output<Void>>> checker = factory.createCommand(repository, contentRepository, accessChecker,
                accessUpdater, ItemCommandType.DELETE, itemInput);
        Command<Output<Void>> executor = checker.execute(actor);
        Output<Void> output = executor.execute(actor);

        assertThat(output).extracting(Output::isSuccess, BOOLEAN)
                .isTrue();
    }

    @Test
    void shouldCopyTheGivenFileToExpectedLocation() {
        ItemCommandFactory<Item> factory = new CopyItemCommandFactory();
        File source = File.builder()
                .withName("source")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .withContentType("content-type")
                .build();
        File destination = File.builder()
                .withName("destination")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .withContentType("content-type")
                .build();
        InputStream stream = mock(InputStream.class);
        ItemInput itemInput = ItemInput.builder(source).withDestination(destination).build();

        given(accessChecker.canWrite(Folder.getRoot(), actor)).willReturn(true);
        given(repository.exists(Folder.getRoot(), "destination")).willReturn(false);
        given(repository.create(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(accessUpdater.createItem(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(contentRepository.initContent(any())).willAnswer(invocation -> Output.success(invocation.getArgument(0)));
        given(contentRepository.readContent(any())).willReturn(success(stream));
        given(contentRepository.writeContent(any(), eq(stream)))
                .willAnswer(invocation -> success(invocation.getArgument(0)));

        Command<Command<Output<Item>>> checker = factory.createCommand(repository, contentRepository, accessChecker,
                accessUpdater, ItemCommandType.COPY, itemInput);
        Command<Output<Item>> executor = checker.execute(actor);
        Output<Item> output = executor.execute(actor);

        assertThat(output).extracting(Output::isSuccess, BOOLEAN)
                .isTrue();
    }

    @Test
    void shouldMoveTheGivenFileToExpectedLocation() {
        ItemCommandFactory<Item> factory = new MoveItemCommandFactory();
        File source = File.builder()
                .withName("source")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .withContentType("content-type")
                .build();
        File destination = File.builder()
                .withName("destination")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .withContentType("content-type")
                .build();
        ItemInput itemInput = ItemInput.builder(source).withDestination(destination).build();

        given(accessChecker.canWrite(Folder.getRoot(), actor)).willReturn(true);
        given(repository.exists(Folder.getRoot(), "destination")).willReturn(false);
        given(repository.create(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(accessUpdater.createItem(any())).willAnswer(invocation -> invocation.getArgument(0));

        Command<Command<Output<Item>>> checker = factory.createCommand(repository, contentRepository, accessChecker,
                accessUpdater, ItemCommandType.COPY, itemInput);
        Command<Output<Item>> executor = checker.execute(actor);
        Output<Item> output = executor.execute(actor);

        assertThat(output).extracting(Output::isSuccess, BOOLEAN)
                .isTrue();
    }

    @Test
    void testInputHasRequiredInfoInToString() {
        File file = File.builder()
                .withName("file")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        ItemInput itemInput = ItemInput.builder(file).build();
        assertThat(itemInput).hasToString("Input{item='file root(00000000-0000-0000-0000-000000000000):root(00000000-0000-0000-0000-000000000000) --------- '," +
                " name='null', user=null, group=null, ownerAccess=null, groupAccess=null, otherAccess=null, contentType=null, destination=null}");
    }

    @Test
    void testOutputHasRequiredInfoInToString() {
        Output<String> output = success("success");
        assertThat(output).hasToString("Output{value=success}");
        output = Output.error("error");
        assertThat(output).hasToString("Output{error=Error{message='error', reasons=[]}}");
    }
}

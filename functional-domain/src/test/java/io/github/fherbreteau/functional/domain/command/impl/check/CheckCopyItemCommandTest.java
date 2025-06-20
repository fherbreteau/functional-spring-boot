package io.github.fherbreteau.functional.domain.command.impl.check;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.impl.error.ItemErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.CopyItemCommand;
import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.Folder;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.ContentRepository;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.rules.AccessChecker;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckCopyItemCommandTest {
    private CheckCopyItemCommand command;
    @Mock
    private ItemRepository repository;
    @Mock
    private ContentRepository contentRepository;
    @Mock
    private AccessChecker accessChecker;
    @Mock
    private AccessUpdater accessUpdater;
    private Item source;
    private Folder destination;
    private User actor;

    @BeforeEach
    void setup() {
        source = File.builder()
                .withName("source")
                .withOwner(User.root())
                .withGroup(Group.root())
                .withParent(Folder.getRoot())
                .withContentType("content-type")
                .build();
        destination = Folder.builder()
                .withName("destination")
                .withOwner(User.root())
                .withGroup(Group.root())
                .withParent(Folder.getRoot())
                .build();
        actor = User.builder("actor").build();
        command = new CheckCopyItemCommand(repository, contentRepository, accessChecker, accessUpdater, source,
                destination);
    }

    @Test
    void shouldGenerateCopyItemCommandWhenAllChecksSucceed() {
        given(accessChecker.canWrite(destination, actor)).willReturn(true);
        given(repository.exists(destination, "source")).willReturn(false);

        Command<Output<Item>> result = command.execute(actor);
        assertThat(result).isInstanceOf(CopyItemCommand.class);
    }

    @Test
    void shouldGenerateCopyItemCommandWhenAllChecksSucceedForFolderToFolder() {
        source = Folder.builder()
                .withName("destination")
                .withOwner(User.root())
                .withGroup(Group.root())
                .withParent(Folder.getRoot())
                .build();
        command = new CheckCopyItemCommand(repository, contentRepository, accessChecker, accessUpdater, source,
                destination);
        given(accessChecker.canWrite(destination, actor)).willReturn(true);
        given(repository.exists(destination, "destination")).willReturn(false);

        Command<Output<Item>> result = command.execute(actor);
        assertThat(result).isInstanceOf(CopyItemCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenParentFolderIsNotWriteable() {
        given(accessChecker.canWrite(destination, actor)).willReturn(false);
        given(repository.exists(destination, "source")).willReturn(false);

        Command<Output<Item>> result = command.execute(actor);
        assertThat(result).isInstanceOf(ItemErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenDestinationFileAlreadyExists() {
        given(accessChecker.canWrite(destination, actor)).willReturn(true);
        given(repository.exists(destination, "source")).willReturn(true);

        Command<Output<Item>> result = command.execute(actor);
        assertThat(result).isInstanceOf(ItemErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenSourceIsAFolderAndDestinationIsAFile() {
        command = new CheckCopyItemCommand(repository, contentRepository, accessChecker, accessUpdater, destination,
                source);

        Command<Output<Item>> result = command.execute(actor);
        assertThat(result).isInstanceOf(ItemErrorCommand.class);
    }
}

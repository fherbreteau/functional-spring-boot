package io.github.fherbreteau.functional.domain.command.impl.check;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.impl.error.ItemErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.MoveItemCommand;
import io.github.fherbreteau.functional.domain.entities.*;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.rules.AccessChecker;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CheckMoveItemCommandTest {
    private CheckMoveItemCommand command;
    @Mock
    private ItemRepository repository;
    @Mock
    private AccessChecker accessChecker;
    @Mock
    private AccessUpdater accessUpdater;
    private Item source;
    private Item destination;
    private User actor;

    @BeforeEach
    public void setup() {
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
        command = new CheckMoveItemCommand(repository, accessChecker, accessUpdater, source, destination);
    }

    @Test
    void shouldGenerateMoveItemCommandWhenAllChecksSucceed() {
        given(accessChecker.canWrite((Folder) destination, actor)).willReturn(true);
        given(repository.exists((Folder) destination, "source")).willReturn(false);

        Command<Output<Item>> result = command.execute(actor);
        assertThat(result).isInstanceOf(MoveItemCommand.class);
    }

    @Test
    void shouldGenerateMoveItemCommandWhenAllChecksSucceedForFolderToFolder() {
        source = Folder.builder()
                .withName("source")
                .withOwner(User.root())
                .withGroup(Group.root())
                .withParent(Folder.getRoot())
                .build();
        command = new CheckMoveItemCommand(repository, accessChecker, accessUpdater, source, destination);
        given(accessChecker.canWrite(destination, actor)).willReturn(true);
        given(repository.exists((Folder) destination, "source")).willReturn(false);

        Command<Output<Item>> result = command.execute(actor);
        assertThat(result).isInstanceOf(MoveItemCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenParentFolderIsNotWriteable() {
        given(accessChecker.canWrite((Folder) destination, actor)).willReturn(false);
        given(repository.exists((Folder) destination, "source")).willReturn(false);

        Command<Output<Item>> result = command.execute(actor);
        assertThat(result).isInstanceOf(ItemErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenDestinationFileAlreadyExists() {
        given(accessChecker.canWrite((Folder) destination, actor)).willReturn(true);
        given(repository.exists((Folder) destination, "source")).willReturn(true);

        Command<Output<Item>> result = command.execute(actor);
        assertThat(result).isInstanceOf(ItemErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenSourceIsAFolderAndDestinationIsAFile() {
        command = new CheckMoveItemCommand(repository, accessChecker, accessUpdater, destination, source);

        Command<Output<Item>> result = command.execute(actor);
        assertThat(result).isInstanceOf(ItemErrorCommand.class);
    }
}

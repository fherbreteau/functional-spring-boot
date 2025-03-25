package io.github.fherbreteau.functional.domain.command.impl.check;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.mockito.BDDMockito.given;

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

@ExtendWith(MockitoExtension.class)
class CheckMoveItemCommandTest {
    private CheckMoveItemCommand command;
    @Mock
    private ItemRepository repository;
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
        command = new CheckMoveItemCommand(repository, accessChecker, accessUpdater, source, destination);
    }

    @Test
    void shouldGenerateMoveItemCommandWhenAllChecksSucceed() {
        given(accessChecker.canWrite(destination, actor)).willReturn(true);
        given(repository.exists(destination, "source")).willReturn(false);

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
        given(repository.exists(destination, "source")).willReturn(false);

        Command<Output<Item>> result = command.execute(actor);
        assertThat(result).isInstanceOf(MoveItemCommand.class);
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
        command = new CheckMoveItemCommand(repository, accessChecker, accessUpdater, destination, source);

        Command<Output<Item>> result = command.execute(actor);
        assertThat(result).isInstanceOf(ItemErrorCommand.class);
        Output<Item> error = result.execute(actor);
        assertThat(error).extracting(Output::isFailure, BOOLEAN).isTrue();
        String expectedError = String.format("%s can't move %s to a file", actor, destination.getHandle());
        assertThat(error)
                .extracting(Output::getFailure)
                .extracting(Failure::getReasons, list(String.class))
                .contains(expectedError);
    }
}

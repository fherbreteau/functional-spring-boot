package io.github.fherbreteau.functional.domain.command.impl.check;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.command.impl.error.ItemErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.CreateFolderCommand;
import io.github.fherbreteau.functional.domain.entities.Folder;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.AccessChecker;
import io.github.fherbreteau.functional.driven.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CheckCreateFolderCommandTest {
    private CheckCreateFolderCommand command;
    @Mock
    private FileRepository repository;
    @Mock
    private AccessChecker accessChecker;
    private Folder parent;
    private User actor;

    @BeforeEach
    public void setup() {
        parent = Folder.builder()
                .withName("parent")
                .build();
        actor = User.builder("actor").build();
        command = new CheckCreateFolderCommand(repository, accessChecker, "folder", parent);
    }

    @Test
    void shouldGenerateCreateFolderCommandWhenCheckingSucceed() {
        // GIVEN
        given(accessChecker.canWrite(parent, actor)).willReturn(true);
        // WHEN
        Command<Output> result = command.execute(actor);
        // THEN
        assertThat(result).isInstanceOf(CreateFolderCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenCheckingFails() {
        // GIVEN
        given(accessChecker.canWrite(parent, actor)).willReturn(false);
        // WHEN
        Command<Output> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(ItemErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenExistenceCheckingFails() {
        // GIVEN
        given(accessChecker.canWrite(parent, actor)).willReturn(true);
        given(repository.exists(parent, "folder")).willReturn(true);
        // WHEN
        Command<Output> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(ItemErrorCommand.class);
    }
}

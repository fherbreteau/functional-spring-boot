package io.github.fherbreteau.functional.domain.command.impl.check;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.mockito.BDDMockito.given;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.impl.error.ItemErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.CreateFolderCommand;
import io.github.fherbreteau.functional.domain.entities.Folder;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.ItemCommandType;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.rules.AccessChecker;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckCreateFolderCommandTest {
    private CheckCreateFolderCommand command;
    @Mock
    private ItemRepository repository;
    @Mock
    private AccessChecker accessChecker;
    @Mock
    private AccessUpdater accessUpdater;

    private Folder parent;
    private User actor;

    @BeforeEach
    void setup() {
        parent = Folder.builder()
                .withName("parent")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        actor = User.builder("actor").build();
        command = new CheckCreateFolderCommand(repository, accessChecker, accessUpdater, "folder", parent);
    }

    @Test
    void shouldGenerateCreateFolderCommandWhenCheckingSucceed() {
        // GIVEN
        given(accessChecker.canWrite(parent, actor)).willReturn(true);
        // WHEN
        Command<Output<Item>> result = command.execute(actor);
        // THEN
        assertThat(result).isInstanceOf(CreateFolderCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenCheckingFails() {
        // GIVEN
        given(accessChecker.canWrite(parent, actor)).willReturn(false);
        // WHEN
        Command<Output<Item>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(ItemErrorCommand.class)
                .extracting("reasons", list(String.class))
                .hasSize(1)
                .first().matches(s -> s.endsWith(" can't create folder in null"));
    }

    @Test
    void shouldGenerateErrorCommandWhenExistenceCheckingFails() {
        // GIVEN
        given(accessChecker.canWrite(parent, actor)).willReturn(true);
        given(repository.exists(parent, "folder")).willReturn(true);
        // WHEN
        Command<Output<Item>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(ItemErrorCommand.class)
                .extracting("reasons", list(String.class))
                .hasSize(1)
                .first().isEqualTo("folder already exists in null");
        assertThat(result).extracting("type")
                .isEqualTo(ItemCommandType.MKDIR);
    }
}

package io.github.fherbreteau.functional.domain.command.impl.check;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.entities.*;
import io.github.fherbreteau.functional.domain.command.impl.error.ItemErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.ListChildrenCommand;
import io.github.fherbreteau.functional.driven.rules.AccessChecker;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CheckListChildrenCommandTest {
    private CheckListChildrenCommand command;
    @Mock
    private ItemRepository repository;
    @Mock
    private AccessChecker accessChecker;
    private Folder parent;
    private User actor;

    @BeforeEach
    public void setup() {
        parent = Folder.builder()
                .withName("parent")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        actor = User.builder("actor").build();
        command = new CheckListChildrenCommand(repository, accessChecker, parent);
    }

    @Test
    void shouldGenerateListChildrenCommandWhenCheckingSucceed() {
        // GIVEN
        given(accessChecker.canRead(parent, actor)).willReturn(true);
        // WHEN
        Command<Output<List<Item>>> result = command.execute(actor);
        // THEN
        assertThat(result).isInstanceOf(ListChildrenCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenCheckingFails() {
        // GIVEN
        given(accessChecker.canRead(parent, actor)).willReturn(false);
        // WHEN
        Command<Output<List<Item>>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(ItemErrorCommand.class);
    }
}

package io.github.fherbreteau.functional.domain.command.impl.success;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.UUID;

import io.github.fherbreteau.functional.domain.entities.Folder;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListChildrenCommandTest {
    private ListChildrenCommand command;
    @Mock
    private ItemRepository repository;

    private Folder parent;
    private User actor;

    @BeforeEach
    void setup() {
        parent = Folder.builder()
                .withName("parent")
                .withHandle(UUID.randomUUID())
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        actor = User.builder("actor").build();
        command = new ListChildrenCommand(repository, parent);
    }

    @Test
    void shouldReadFolderContentWhenExecutingCommand() {
        // GIVEN
        given(repository.findByParentAndUser(parent, actor)).willReturn(List.of());
        // WHEN
        Output<List<Item>> result = command.execute(actor);
        //THEN
        assertThat(result).isNotNull()
                .extracting(Output::isSuccess, InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
    }
}

package io.github.fherbreteau.functional.domain.command.impl.success;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChangeGroupCommandTest {
    private ChangeGroupCommand command;
    @Mock
    private ItemRepository repository;
    @Mock
    private AccessUpdater accessUpdater;
    @Mock
    private User actor;

    private Group oldGroup;
    private Group newGroup;

    @Captor
    private ArgumentCaptor<Item> itemCaptor;
    @Captor
    private ArgumentCaptor<Group> groupCaptor;

    @BeforeEach
    void setup() {
        User user = User.builder("user").build();
        oldGroup = user.getGroup();
        Item item = File.builder().withName("name").withOwner(user).build();
        newGroup = Group.builder("group").build();
        command = new ChangeGroupCommand(repository, accessUpdater, item, newGroup);
    }

    @Test
    void shouldEditGroupWhenExecutingCommand() {
        // GIVEN
        given(repository.update(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(accessUpdater.updateGroup(any(), any())).willAnswer(invocation -> invocation.getArgument(0));
        // WHEN
        Output<Item> result = command.execute(actor);
        //THEN
        assertThat(result).isNotNull()
                .extracting(Output::isSuccess, InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
        verify(repository).update(itemCaptor.capture());
        assertThat(itemCaptor.getValue())
                .extracting(Item::getGroup)
                .isEqualTo(newGroup);
        verify(accessUpdater).updateGroup(any(), groupCaptor.capture());
        assertThat(groupCaptor.getValue())
                .isEqualTo(oldGroup);
    }
}

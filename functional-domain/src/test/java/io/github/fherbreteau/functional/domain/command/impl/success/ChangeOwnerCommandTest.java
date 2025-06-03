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
class ChangeOwnerCommandTest {
    private ChangeOwnerCommand command;
    @Mock
    private ItemRepository repository;
    @Mock
    private AccessUpdater accessUpdater;
    @Mock
    private User actor;

    private User oldUser;
    private User newUser;
    private Group group;

    @Captor
    private ArgumentCaptor<Item> itemCaptor;
    @Captor
    private ArgumentCaptor<User> ownerCaptor;

    @BeforeEach
    void setup() {
        oldUser = User.builder("user").build();
        group = Group.builder("group").build();
        Item item = File.builder()
                .withName("name")
                .withOwner(oldUser)
                .withGroup(group)
                .build();
        newUser = User.builder("newUser").build();
        command = new ChangeOwnerCommand(repository, accessUpdater, item, newUser);
    }

    @Test
    void shouldEditOwnerWhenExecutingCommand() {
        // GIVEN
        given(repository.update(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(accessUpdater.updateOwner(any(), any())).willAnswer(invocation -> invocation.getArgument(0));
        // WHEN
        Output<Item> result = command.execute(actor);
        //THEN
        assertThat(result).isNotNull()
                .extracting(Output::isSuccess, InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
        verify(repository).update(itemCaptor.capture());
        assertThat(itemCaptor.getValue())
                .extracting(Item::getOwner)
                .isEqualTo(newUser);
        assertThat(itemCaptor.getValue())
                .extracting(Item::getGroup)
                .isEqualTo(group);
        verify(accessUpdater).updateOwner(any(), ownerCaptor.capture());
        assertThat(ownerCaptor.getValue())
                .isEqualTo(oldUser);
    }
}

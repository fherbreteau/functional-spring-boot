package io.github.fherbreteau.functional.domain.command.impl.success;

import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.GroupRepository;
import io.github.fherbreteau.functional.driven.UserRepository;
import io.github.fherbreteau.functional.driven.UserUpdater;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeleteGroupCommandTest {
    private DeleteGroupCommand command;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserUpdater userUpdater;
    @Mock
    private User actor;

    private UUID groupId;

    @BeforeEach
    public void setup() {
        groupId = UUID.randomUUID();
        command = new DeleteGroupCommand(userRepository, groupRepository, userUpdater, "group", false);
    }

    @Test
    void shouldDeleteGroupWhenExecutingCommand() {
        // GIVEN
        Group group = Group.builder("group").withGroupId(groupId).build();
        given(groupRepository.findByName("group")).willReturn(group);
        given(groupRepository.delete(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(userUpdater.deleteGroup(any())).willAnswer(invocation -> invocation.getArgument(0));
        // WHEN
        Output result = command.execute(actor);
        //THEN
        assertThat(result).isNotNull()
                .extracting(Output::isSuccess, InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
        verify(groupRepository).delete(group);
        verify(userUpdater).deleteGroup(group);
    }
}

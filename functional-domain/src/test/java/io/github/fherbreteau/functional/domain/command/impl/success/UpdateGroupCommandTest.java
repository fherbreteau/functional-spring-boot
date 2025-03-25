package io.github.fherbreteau.functional.domain.command.impl.success;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.GroupRepository;
import io.github.fherbreteau.functional.driven.repository.UserRepository;
import io.github.fherbreteau.functional.driven.rules.UserUpdater;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateGroupCommandTest {
    private UpdateGroupCommand command;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserUpdater userUpdater;
    @Mock
    private User actor;

    private UUID groupId;

    @Captor
    private ArgumentCaptor<Group> oldGroupCaptor;
    @Captor
    private ArgumentCaptor<Group> newGroupCaptor;

    @BeforeEach
    void setup() {
        groupId = UUID.randomUUID();
        command = new UpdateGroupCommand(userRepository, groupRepository, userUpdater, "group", null, null);
    }

    @Test
    void shouldUpdateAddGroupWhenExecutingCommand() {
        // GIVEN
        Group group = Group.builder("group").withGroupId(groupId).build();
        given(groupRepository.findByName("group")).willReturn(group);
        given(groupRepository.update(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(userUpdater.updateGroup(any(), any())).willAnswer(invocation -> invocation.getArgument(1));
        // WHEN
        Output<Group> result = command.execute(actor);
        //THEN
        assertThat(result).isNotNull()
                .extracting(Output::isSuccess, InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
        verify(groupRepository).update(newGroupCaptor.capture());
        assertThat(newGroupCaptor.getValue())
                .extracting(Group::getGroupId)
                .isEqualTo(groupId);
        assertThat(newGroupCaptor.getValue())
                .extracting(Group::getName)
                .isEqualTo("group");
        verify(userUpdater).updateGroup(oldGroupCaptor.capture(), eq(newGroupCaptor.getValue()));
        assertThat(oldGroupCaptor.getValue())
                .isEqualTo(group);
    }
}

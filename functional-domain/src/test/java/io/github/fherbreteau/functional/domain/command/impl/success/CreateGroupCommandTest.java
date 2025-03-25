package io.github.fherbreteau.functional.domain.command.impl.success;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
class CreateGroupCommandTest {
    private CreateGroupCommand command;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserUpdater userUpdater;
    @Mock
    private User actor;

    @Captor
    private ArgumentCaptor<Group> groupCaptor;

    @BeforeEach
    void setup() {
        command = new CreateGroupCommand(userRepository, groupRepository, userUpdater, "group", null);
    }

    @Test
    void shouldUpdateAddGroupWhenExecutingCommand() {
        // GIVEN
        given(groupRepository.create(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(userUpdater.createGroup(any())).willAnswer(invocation -> invocation.getArgument(0));
        // WHEN
        Output<Group> result = command.execute(actor);
        //THEN
        assertThat(result).isNotNull()
                .extracting(Output::isSuccess, InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
        verify(groupRepository).create(groupCaptor.capture());
        assertThat(groupCaptor.getValue())
                .extracting(Group::getGroupId)
                .isNotNull();
        assertThat(groupCaptor.getValue())
                .extracting(Group::getName)
                .isEqualTo("group");
    }
}

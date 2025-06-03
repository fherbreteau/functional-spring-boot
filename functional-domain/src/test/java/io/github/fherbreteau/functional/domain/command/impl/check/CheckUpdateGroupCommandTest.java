package io.github.fherbreteau.functional.domain.command.impl.check;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.UUID;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.impl.error.UserErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.UpdateGroupCommand;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.GroupRepository;
import io.github.fherbreteau.functional.driven.repository.UserRepository;
import io.github.fherbreteau.functional.driven.rules.UserChecker;
import io.github.fherbreteau.functional.driven.rules.UserUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckUpdateGroupCommandTest {
    private CheckUpdateGroupCommand command;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserChecker userChecker;
    @Mock
    private UserUpdater userUpdater;
    @Mock
    private User actor;

    @BeforeEach
    void setup() {
        command = new CheckUpdateGroupCommand(userRepository, groupRepository, userChecker, userUpdater, "group",
                null, "group1");
    }

    @Test
    void shouldGenerateModifyGroupCommandWhenCheckingSucceed() {
        // GIVEN
        given(userChecker.canUpdateGroup("group", actor)).willReturn(true);
        given(groupRepository.exists("group")).willReturn(true);
        given(groupRepository.exists("group1")).willReturn(false);
        // WHEN
        Command<Output<Group>> result = command.execute(actor);
        // THEN
        assertThat(result).isInstanceOf(UpdateGroupCommand.class);
    }

    @Test
    void shouldGenerateModifyGroupCommandWhenCheckingSucceed1() {
        // GIVEN
        UUID groupId = UUID.randomUUID();
        given(userChecker.canUpdateGroup("group", actor)).willReturn(true);
        given(groupRepository.exists("group")).willReturn(true);
        given(groupRepository.exists(groupId)).willReturn(false);
        // WHEN
        command = new CheckUpdateGroupCommand(userRepository, groupRepository, userChecker, userUpdater, "group",
                groupId, null);
        Command<Output<Group>> result = command.execute(actor);
        // THEN
        assertThat(result).isInstanceOf(UpdateGroupCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenCheckingFails() {
        // GIVEN
        given(userChecker.canUpdateGroup("group", actor)).willReturn(false);
        // WHEN
        Command<Output<Group>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenGroupDoNotExists() {
        // GIVEN
        given(userChecker.canUpdateGroup("group", actor)).willReturn(true);
        given(groupRepository.exists("group")).willReturn(false);
        // WHEN
        Command<Output<Group>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenGroupNameExists() {
        // GIVEN
        given(userChecker.canUpdateGroup("group", actor)).willReturn(true);
        given(groupRepository.exists("group")).willReturn(true);
        given(groupRepository.exists("group1")).willReturn(true);
        // WHEN
        Command<Output<Group>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenGroupIdExists() {
        // GIVEN
        UUID groupId = UUID.randomUUID();
        given(userChecker.canUpdateGroup("group", actor)).willReturn(true);
        given(groupRepository.exists("group")).willReturn(true);
        given(groupRepository.exists(groupId)).willReturn(true);
        // WHEN
        command = new CheckUpdateGroupCommand(userRepository, groupRepository, userChecker, userUpdater, "group",
                groupId, null);
        Command<Output<Group>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }
}

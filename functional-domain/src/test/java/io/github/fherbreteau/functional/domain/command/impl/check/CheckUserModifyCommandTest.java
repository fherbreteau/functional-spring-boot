package io.github.fherbreteau.functional.domain.command.impl.check;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.impl.error.UserErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.UserModifyCommand;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.domain.entities.UserInput;
import io.github.fherbreteau.functional.driven.GroupRepository;
import io.github.fherbreteau.functional.driven.UserChecker;
import io.github.fherbreteau.functional.driven.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CheckUserModifyCommandTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserChecker userChecker;
    private CheckUserModifyCommand command;
    @Mock
    private User actor;

    @BeforeEach
    public void setup() {
        UserInput input = UserInput.builder("user").withNewName("user1").build();
        command = new CheckUserModifyCommand(userRepository, groupRepository, userChecker, input);
    }

    @Test
    void shouldGenerateModifyUserCommandWhenCheckingSucceed() {
        // GIVEN
        given(userChecker.canUpdateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(true);
        given(userRepository.exists("user1")).willReturn(false);
        // WHEN
        Command<Output> result = command.execute(actor);
        // THEN
        assertThat(result).isInstanceOf(UserModifyCommand.class);
    }

    @Test
    void shouldGenerateModifyUserCommandWhenCheckingSucceed1() {
        // GIVEN
        UUID groupId = UUID.randomUUID();
        given(userChecker.canUpdateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(true);
        given(groupRepository.exists(groupId, "group")).willReturn(true);
        // WHEN
        UserInput input = UserInput.builder("user").withGroupName("group").withGroupId(groupId).build();
        command = new CheckUserModifyCommand(userRepository, groupRepository, userChecker, input);
        Command<Output> result = command.execute(actor);
        // THEN
        assertThat(result).isInstanceOf(UserModifyCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenCheckingFails() {
        // GIVEN
        given(userChecker.canUpdateUser("user", actor)).willReturn(false);
        // WHEN
        Command<Output> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenUserNameNotExists() {
        // GIVEN
        given(userChecker.canUpdateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(false);
        // WHEN
        Command<Output> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenUserNewNameExists() {
        // GIVEN
        given(userChecker.canUpdateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(true);
        given(userRepository.exists("user1")).willReturn(true);
        // WHEN
        Command<Output> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenUserIdExists() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        given(userChecker.canUpdateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(true);
        given(userRepository.exists(userId)).willReturn(true);
        // WHEN
        UserInput input = UserInput.builder("user").withUserId(userId).build();
        command = new CheckUserModifyCommand(userRepository, groupRepository, userChecker, input);
        Command<Output> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenGroupNotExists() {
        // GIVEN
        UUID groupId = UUID.randomUUID();
        given(userChecker.canUpdateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(true);
        given(groupRepository.exists(groupId, "group")).willReturn(false);
        // WHEN
        UserInput input = UserInput.builder("user").withGroupName("group").withGroupId(groupId).build();
        command = new CheckUserModifyCommand(userRepository, groupRepository, userChecker, input);
        Command<Output> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenGroupIdNotExists() {
        // GIVEN
        UUID groupId = UUID.randomUUID();
        given(userChecker.canUpdateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(true);
        given(groupRepository.exists(groupId)).willReturn(false);
        // WHEN
        UserInput input = UserInput.builder("user").withGroupId(groupId).build();
        command = new CheckUserModifyCommand(userRepository, groupRepository, userChecker, input);
        Command<Output> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenGroupNameNotExists() {
        // GIVEN
        given(userChecker.canUpdateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(true);
        given(groupRepository.exists("group")).willReturn(false);
        // WHEN
        UserInput input = UserInput.builder("user").withGroupName("group").build();
        command = new CheckUserModifyCommand(userRepository, groupRepository, userChecker, input);
        Command<Output> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }
}

package io.github.fherbreteau.functional.domain.command.impl.check;

import static io.github.fherbreteau.functional.domain.entities.UserCommandType.USERMOD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.UUID;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.impl.error.UserErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.UpdateUserCommand;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.domain.entities.UserCommandType;
import io.github.fherbreteau.functional.domain.entities.UserInput;
import io.github.fherbreteau.functional.driven.PasswordProtector;
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
class CheckUpdateUserCommandTest {
    private final UserCommandType type = USERMOD;
    private CheckUpdateUserCommand command;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserChecker userChecker;
    @Mock
    private UserUpdater userUpdater;
    @Mock
    private PasswordProtector passwordProtector;
    @Mock
    private User actor;

    @BeforeEach
    void setup() {
        UserInput input = UserInput.builder("user").withNewName("user1").build();
        command = new CheckUpdateUserCommand(userRepository, groupRepository, userChecker, userUpdater,
                passwordProtector, type, input);
    }

    @Test
    void shouldGenerateModifyUserCommandWhenCheckingSucceed() {
        // GIVEN
        given(userChecker.canUpdateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(true);
        given(userRepository.exists("user1")).willReturn(false);
        // WHEN
        Command<Output<User>> result = command.execute(actor);
        // THEN
        assertThat(result).isInstanceOf(UpdateUserCommand.class);
    }

    @Test
    void shouldGenerateModifyUserCommandWhenCheckingSucceed1() {
        // GIVEN
        UUID groupId = UUID.randomUUID();
        given(userChecker.canUpdateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(true);
        given(groupRepository.exists(groupId)).willReturn(true);
        given(groupRepository.exists("group")).willReturn(true);
        // WHEN
        UserInput input = UserInput.builder("user").withGroups(List.of("group")).withGroupId(groupId).build();
        command = new CheckUpdateUserCommand(userRepository, groupRepository, userChecker, userUpdater,
                passwordProtector, type, input);
        Command<Output<User>> result = command.execute(actor);
        // THEN
        assertThat(result).isInstanceOf(UpdateUserCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenCheckingFails() {
        // GIVEN
        given(userChecker.canUpdateUser("user", actor)).willReturn(false);
        // WHEN
        Command<Output<User>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class)
                .extracting("type")
                .isEqualTo(type);
    }

    @Test
    void shouldGenerateErrorCommandWhenUserNameNotExists() {
        // GIVEN
        given(userChecker.canUpdateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(false);
        // WHEN
        Command<Output<User>> result = command.execute(actor);
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
        Command<Output<User>> result = command.execute(actor);
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
        command = new CheckUpdateUserCommand(userRepository, groupRepository, userChecker, userUpdater,
                passwordProtector, type, input);
        Command<Output<User>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenGroupNotExists() {
        // GIVEN
        UUID groupId = UUID.randomUUID();
        given(userChecker.canUpdateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(true);
        given(groupRepository.exists(groupId)).willReturn(true);
        given(groupRepository.exists("group")).willReturn(false);
        // WHEN
        UserInput input = UserInput.builder("user").withGroups(List.of("group")).withGroupId(groupId).build();
        command = new CheckUpdateUserCommand(userRepository, groupRepository, userChecker, userUpdater,
                passwordProtector, type, input);
        Command<Output<User>> result = command.execute(actor);
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
        command = new CheckUpdateUserCommand(userRepository, groupRepository, userChecker, userUpdater,
                passwordProtector, type, input);
        Command<Output<User>> result = command.execute(actor);
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
        UserInput input = UserInput.builder("user").withGroups(List.of("group")).build();
        command = new CheckUpdateUserCommand(userRepository, groupRepository, userChecker, userUpdater,
                passwordProtector, type, input);
        Command<Output<User>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }
}

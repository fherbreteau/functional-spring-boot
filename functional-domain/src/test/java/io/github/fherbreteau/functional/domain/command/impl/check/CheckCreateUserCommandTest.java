package io.github.fherbreteau.functional.domain.command.impl.check;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.UUID;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.impl.error.UserErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.CreateUserCommand;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
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
class CheckCreateUserCommandTest {
    private CheckCreateUserCommand command;
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
        UserInput input = UserInput.builder("user").build();
        command = new CheckCreateUserCommand(userRepository, groupRepository, userChecker, userUpdater, passwordProtector,
                input);
    }

    @Test
    void shouldGenerateCreateUserCommandWhenCheckingSucceed() {
        // GIVEN
        given(userChecker.canCreateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(false);
        given(userRepository.exists("user")).willReturn(false);
        // WHEN
        Command<Output<User>> result = command.execute(actor);
        // THEN
        assertThat(result).isInstanceOf(CreateUserCommand.class);
    }

    @Test
    void shouldGenerateCreateUserWithGroupsCommandWhenCheckingSucceed() {
        // GIVEN
        given(userChecker.canCreateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(false);
        given(userRepository.exists("user")).willReturn(false);
        given(groupRepository.exists("group")).willReturn(true);
        // WHEN
        UserInput input = UserInput.builder("user").withGroups(List.of("group")).build();
        command = new CheckCreateUserCommand(userRepository, groupRepository, userChecker, userUpdater, passwordProtector,
                input);
        Command<Output<User>> result = command.execute(actor);
        // THEN
        assertThat(result).isInstanceOf(CreateUserCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenCheckingFails() {
        // GIVEN
        given(userChecker.canCreateUser("user", actor)).willReturn(false);
        // WHEN
        Command<Output<User>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenUserNameExists() {
        // GIVEN
        given(userChecker.canCreateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(true);
        // WHEN
        Command<Output<User>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenUserNameExistsAsGroup() {
        // GIVEN
        given(userChecker.canCreateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(false);
        given(groupRepository.exists("user")).willReturn(true);
        // WHEN
        Command<Output<User>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenUserIdExists() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        given(userChecker.canCreateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(false);
        given(userRepository.exists(userId)).willReturn(true);
        // WHEN
        UserInput input = UserInput.builder("user").withUserId(userId).withGroupId(groupId).build();
        command = new CheckCreateUserCommand(userRepository, groupRepository, userChecker, userUpdater, passwordProtector,
                input);
        Command<Output<User>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenGroupNameExists() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        given(userChecker.canCreateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(false);
        given(userRepository.exists(userId)).willReturn(true);
        given(groupRepository.exists("group1")).willReturn(false);
        // WHEN
        UserInput input = UserInput.builder("user").withUserId(userId).withGroups(List.of("group1")).build();
        command = new CheckCreateUserCommand(userRepository, groupRepository, userChecker, userUpdater, passwordProtector,
                input);
        Command<Output<User>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenGroupIdNotExists() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        given(userChecker.canCreateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(false);
        given(userRepository.exists(userId)).willReturn(false);
        given(groupRepository.exists(groupId)).willReturn(false);
        // WHEN
        UserInput input = UserInput.builder("user").withUserId(userId).withGroupId(groupId).build();
        command = new CheckCreateUserCommand(userRepository, groupRepository, userChecker, userUpdater, passwordProtector,
                input);
        Command<Output<User>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }
}

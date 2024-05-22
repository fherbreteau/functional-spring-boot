package io.github.fherbreteau.functional.domain.command.impl.check;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.impl.error.UserErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.GroupGetCommand;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.GroupRepository;
import io.github.fherbreteau.functional.driven.PasswordProtector;
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
class CheckGroupGetCommandTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserChecker userChecker;
    @Mock
    private PasswordProtector passwordProtector;
    @Mock
    private User actor;

    @BeforeEach
    public void setup() {
    }

    @Test
    void shouldGenerateGetGroupCommandWhenCheckingSucceedForAUserId() {
        UUID userId = UUID.randomUUID();
        CheckGroupGetCommand command = new CheckGroupGetCommand(userRepository, groupRepository, userChecker,
                passwordProtector, null, userId);
        // GIVEN
        given(userRepository.exists(userId)).willReturn(true);
        // WHEN
        Command<Output> result = command.execute(actor);
        // THEN
        assertThat(result).isInstanceOf(GroupGetCommand.class);
    }

    @Test
    void shouldGenerateGetGroupCommandWhenCheckingSucceedForAUserName() {
        CheckGroupGetCommand command = new CheckGroupGetCommand(userRepository, groupRepository, userChecker,
                passwordProtector, "user", null);
        // GIVEN
        given(userRepository.exists("user")).willReturn(true);
        // WHEN
        Command<Output> result = command.execute(actor);
        // THEN
        assertThat(result).isInstanceOf(GroupGetCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenUserIdNotExists() {
        UUID userId = UUID.randomUUID();
        CheckGroupGetCommand command = new CheckGroupGetCommand(userRepository, groupRepository, userChecker,
                passwordProtector, null, userId);
        // GIVEN
        given(userRepository.exists(userId)).willReturn(false);
        // WHEN
        Command<Output> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenUserNameNotExists() {
        CheckGroupGetCommand command = new CheckGroupGetCommand(userRepository, groupRepository, userChecker,
                passwordProtector, "user", null);
        // GIVEN
        given(userRepository.exists("user")).willReturn(false);
        // WHEN
        Command<Output> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }
}
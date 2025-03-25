package io.github.fherbreteau.functional.domain.command.impl.check;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.impl.error.UserErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.DeleteUserCommand;
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
class CheckDeleteUserCommandTest {
    private CheckDeleteUserCommand command;
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
        command = new CheckDeleteUserCommand(userRepository, groupRepository, userChecker, userUpdater, "user");
    }

    @Test
    void shouldGenerateDeleteUserCommandWhenCheckingSucceed() {
        // GIVEN
        given(userChecker.canDeleteUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(true);
        // WHEN
        Command<Output<Void>> result = command.execute(actor);
        // THEN
        assertThat(result).isInstanceOf(DeleteUserCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenCheckingFails() {
        // GIVEN
        given(userChecker.canDeleteUser("user", actor)).willReturn(false);
        // WHEN
        Command<Output<Void>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenGroupNameExists() {
        // GIVEN
        given(userChecker.canDeleteUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(false);
        // WHEN
        Command<Output<Void>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }
}

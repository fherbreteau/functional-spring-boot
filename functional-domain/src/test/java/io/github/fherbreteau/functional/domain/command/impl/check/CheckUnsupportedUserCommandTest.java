package io.github.fherbreteau.functional.domain.command.impl.check;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.impl.error.UserErrorCommand;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CheckUnsupportedUserCommandTest {
    private CheckUnsupportedUserCommand command;
    private User actor;

    @BeforeEach
    public void setup() {
        actor = User.builder("actor").build();
        command = new CheckUnsupportedUserCommand(null, null, null, null, null);
    }

    @Test
    void shouldGenerateErrorCommandWhenCheckingSucceed() {
        // GIVEN
        // WHEN
        Command<Output> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(UserErrorCommand.class);
    }
}

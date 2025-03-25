package io.github.fherbreteau.functional.domain.command.impl.check;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.impl.error.ItemErrorCommand;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckUnsupportedItemCommandTest {
    private CheckUnsupportedItemCommand command;
    private User actor;

    @BeforeEach
    void setup() {
        actor = User.builder("actor").build();
        command = new CheckUnsupportedItemCommand(null, null, null, null);
    }

    @Test
    void shouldGenerateErrorCommandWhenCheckingSucceed() {
        // GIVEN
        // WHEN
        Command<Output<Void>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(ItemErrorCommand.class);
    }
}

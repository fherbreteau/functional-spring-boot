package io.github.fherbreteau.functional.domain.command.impl.check;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.command.impl.error.ItemErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.DownloadCommand;
import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.rules.AccessChecker;
import io.github.fherbreteau.functional.driven.repository.ContentRepository;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CheckDownloadCommandTest {
    private CheckDownloadCommand command;
    @Mock
    private ItemRepository repository;
    @Mock
    private AccessChecker accessChecker;
    @Mock
    private ContentRepository contentRepository;
    private File file;
    private User actor;

    @BeforeEach
    public void setup() {
        file = File.builder()
                .withName("file")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        actor = User.builder("actor").build();
        command = new CheckDownloadCommand(repository, accessChecker, contentRepository, file);
    }

    @Test
    void shouldGenerateDownloadCommandWhenCheckingSucceed() {
        // GIVEN
        given(accessChecker.canRead(file, actor)).willReturn(true);
        // WHEN
        Command<Output<InputStream>> result = command.execute(actor);
        // THEN
        assertThat(result).isInstanceOf(DownloadCommand.class);
    }

    @Test
    void shouldGenerateErrorCommandWhenCheckingFails() {
        // GIVEN
        given(accessChecker.canRead(file, actor)).willReturn(false);
        // WHEN
        Command<Output<InputStream>> result = command.execute(actor);
        //THEN
        assertThat(result).isInstanceOf(ItemErrorCommand.class);
    }
}

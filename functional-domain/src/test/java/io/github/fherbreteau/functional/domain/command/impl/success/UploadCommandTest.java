package io.github.fherbreteau.functional.domain.command.impl.success;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.InputStream;

import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.ContentRepository;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UploadCommandTest {
    private UploadCommand command;
    @Mock
    private ItemRepository repository;
    @Mock
    private ContentRepository contentRepository;
    @Mock
    private InputStream inputStream;

    private User actor;

    @Captor
    private ArgumentCaptor<File> itemCaptor;

    @BeforeEach
    void setup() {
        File file = File.builder()
                .withName("file")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        actor = User.builder("actor").build();
        command = new UploadCommand(repository, contentRepository, file, inputStream, "contentType");
    }

    @Test
    void shouldWriteContentWhenExecutingCommand() {
        // GIVEN
        given(repository.update(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(contentRepository.writeContent(any(), any()))
                .willAnswer(invocation -> Output.success(invocation.getArgument(0)));
        // WHEN
        Output<Item> result = command.execute(actor);
        //THEN
        assertThat(result).isNotNull()
                .extracting(Output::isSuccess, InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
        verify(contentRepository).writeContent(any(), eq(inputStream));
        verify(repository).update(itemCaptor.capture());
        assertThat(itemCaptor.getValue())
                .isInstanceOf(File.class)
                .extracting(File::getContentType)
                .isEqualTo("contentType");
    }
}

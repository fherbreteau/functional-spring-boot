package io.github.fherbreteau.functional.domain.command.impl.success;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.Folder;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.ContentRepository;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateFileCommandTest {
    private CreateFileCommand command;
    @Mock
    private ItemRepository repository;
    @Mock
    private ContentRepository contentRepository;
    @Mock
    private AccessUpdater accessUpdater;

    private Folder parent;
    private User actor;

    @Captor
    private ArgumentCaptor<File> itemCaptor;

    @BeforeEach
    void setup() {
        parent = Folder.builder()
                .withName("parent")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        actor = User.builder("actor").build();
        command = new CreateFileCommand(repository, contentRepository, accessUpdater, "file", parent);
    }

    @Test
    void shouldCreateFileWhenExecutingCommand() {
        // GIVEN
        given(repository.create(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(accessUpdater.createItem(any(File.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(contentRepository.initContent(any(File.class))).willAnswer(invocation -> Output.success(invocation.getArgument(0)));
        // WHEN
        Output<Item> result = command.execute(actor);
        //THEN
        assertThat(result).isNotNull()
                .extracting(Output::isSuccess, InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
        verify(repository).create(itemCaptor.capture());
        assertThat(itemCaptor.getValue())
                .isInstanceOf(File.class)
                .extracting(Item::getParent)
                .isEqualTo(parent);
        assertThat(itemCaptor.getValue())
                .extracting(Item::getName)
                .isEqualTo("file");
        assertThat(itemCaptor.getValue())
                .extracting(Item::getOwner)
                .isEqualTo(actor);
        assertThat(itemCaptor.getValue())
                .extracting(Item::getGroup)
                .isEqualTo(actor.getGroup());
    }
}

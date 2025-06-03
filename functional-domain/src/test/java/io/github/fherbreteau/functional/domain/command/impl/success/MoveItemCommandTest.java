package io.github.fherbreteau.functional.domain.command.impl.success;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.Folder;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MoveItemCommandTest {
    private MoveItemCommand command;
    @Mock
    private ItemRepository repository;
    @Mock
    private AccessUpdater accessUpdater;

    private Item source;
    private Item destination;

    private User actor;

    @Captor
    private ArgumentCaptor<File> fileCaptor;

    @Captor
    private ArgumentCaptor<Folder> folderCaptor;

    @Nested
    class MoveSourceFileToDestinationFile {

        @BeforeEach
        void setup() {

            source = File.builder()
                    .withName("source")
                    .withOwner(User.root())
                    .withGroup(Group.root())
                    .withParent(Folder.getRoot())
                    .build();
            destination = File.builder()
                    .withName("destination")
                    .withOwner(User.root())
                    .withGroup(Group.root())
                    .withParent(Folder.getRoot())
                    .build();
            actor = User.builder("actor").build();
            command = new MoveItemCommand(repository, accessUpdater, source, destination);
        }

        @Test
        void shouldRenameSourceToDestination() {
            // GIVEN
            given(repository.update(any())).willAnswer(invocation -> invocation.getArgument(0));
            given(repository.exists(Folder.getRoot())).willReturn(true);
            // WHEN
            Output<Item> result = command.execute(actor);
            //THEN
            assertThat(result).isNotNull()
                    .extracting(Output::isSuccess, BOOLEAN)
                    .isTrue();
            verify(repository).update(fileCaptor.capture());
            assertThat(fileCaptor.getValue())
                    .extracting(Item::getName)
                    .isEqualTo("destination");
            assertThat(fileCaptor.getValue())
                    .extracting(Item::getParent)
                    .isEqualTo(destination.getParent());
            assertThat(fileCaptor.getValue())
                    .extracting(Item::getOwner)
                    .isEqualTo(actor);
            assertThat(fileCaptor.getValue())
                    .extracting(Item::getGroup)
                    .isEqualTo(Group.root());
        }
    }

    @Nested
    class MoveSourceFileToDestinationFolder {

        @BeforeEach
        void setup() {

            source = File.builder()
                    .withName("source")
                    .withOwner(User.root())
                    .withGroup(Group.root())
                    .withParent(Folder.getRoot())
                    .build();
            destination = Folder.builder()
                    .withName("destination")
                    .withOwner(User.root())
                    .withGroup(Group.root())
                    .withParent(Folder.getRoot())
                    .build();
            actor = User.builder("actor").build();
            command = new MoveItemCommand(repository, accessUpdater, source, destination);
        }

        @Test
        void shouldChangeParentOfSourceFile() {
            // GIVEN
            given(repository.update(any())).willAnswer(invocation -> invocation.getArgument(0));
            given(repository.exists(destination)).willReturn(true);
            // WHEN
            Output<Item> result = command.execute(actor);
            //THEN
            assertThat(result).isNotNull()
                    .extracting(Output::isSuccess, BOOLEAN)
                    .isTrue();
            verify(repository).update(fileCaptor.capture());
            assertThat(fileCaptor.getValue())
                    .extracting(Item::getParent)
                    .isEqualTo(destination);
            assertThat(fileCaptor.getValue())
                    .extracting(Item::getName)
                    .isEqualTo("source");
            assertThat(fileCaptor.getValue())
                    .extracting(Item::getOwner)
                    .isEqualTo(actor);
            assertThat(fileCaptor.getValue())
                    .extracting(Item::getGroup)
                    .isEqualTo(destination.getGroup());
        }
    }

    @Nested
    class MoveSourceFolderToDestinationFolder {

        @BeforeEach
        void setup() {

            source = Folder.builder()
                    .withName("source")
                    .withOwner(User.root())
                    .withGroup(Group.root())
                    .withParent(Folder.getRoot())
                    .build();
            destination = Folder.builder()
                    .withName("destination")
                    .withOwner(User.root())
                    .withGroup(Group.root())
                    .withParent(Folder.getRoot())
                    .build();
            actor = User.builder("actor").build();
            command = new MoveItemCommand(repository, accessUpdater, source, destination);
        }

        @Test
        void shouldRenameFolder() {
            // GIVEN
            given(repository.update(any())).willAnswer(invocation -> invocation.getArgument(0));
            // WHEN
            Output<Item> result = command.execute(actor);
            //THEN
            assertThat(result).isNotNull()
                    .extracting(Output::isSuccess, BOOLEAN)
                    .isTrue();
            verify(repository).update(folderCaptor.capture());
            assertThat(folderCaptor.getValue())
                    .extracting(Item::getParent)
                    .isEqualTo(source.getParent());
            assertThat(folderCaptor.getValue())
                    .extracting(Item::getName)
                    .isEqualTo("source");
            assertThat(folderCaptor.getValue())
                    .extracting(Item::getOwner)
                    .isEqualTo(actor);
            assertThat(folderCaptor.getValue())
                    .extracting(Item::getGroup)
                    .isEqualTo(Group.root());
        }
    }
}

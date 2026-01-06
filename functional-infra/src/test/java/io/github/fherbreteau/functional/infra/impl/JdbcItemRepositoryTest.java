package io.github.fherbreteau.functional.infra.impl;

import static io.github.fherbreteau.functional.domain.entities.AccessRight.full;
import static io.github.fherbreteau.functional.domain.entities.AccessRight.none;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.Folder;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@JdbcTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {JdbcItemRepository.class, JdbcAccessRightRepository.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JdbcItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void shouldCheckExistenceOfGivenFolderInRoot() {
        assertTrue(itemRepository.exists(Folder.getRoot(), "folder"));
    }

    @Test
    void shouldCheckExistenceOfRoot() {
        assertTrue(itemRepository.exists(Folder.getRoot()));
    }

    @Test
    void shouldCheckExistenceOfGivenFolder() {
        Folder folder = Folder.builder()
                .withName("folder")
                .withOwner(User.root())
                .withParent(Folder.getRoot())
                .build();
        assertTrue(itemRepository.exists(folder));
    }

    @Test
    void shouldCheckAbsenceOfGivenFile() {
        UUID handle = UUID.fromString("bc321002-b703-424b-9c3f-d47bf15be632");
        File file = File.builder()
                .withHandle(handle)
                .withName("user")
                .withOwner(User.root())
                .withParent(Folder.getRoot())
                .build();
        assertFalse(itemRepository.exists(file));
    }

    @Test
    void shouldCheckAbsenceOfGivenFileWithoutHandle() {
        File file = File.builder()
                .withName("user")
                .withOwner(User.root())
                .withParent(Folder.getRoot())
                .build();
        assertFalse(itemRepository.exists(file));
    }

    @Test
    void shouldCreateItemInDatabase() {
        File file = File.builder()
                .withName("file")
                .withOwner(User.root())
                .withParent(Folder.getRoot())
                .withOwnerAccess(full())
                .withGroupAccess(full())
                .withOtherAccess(full())
                .withContentType("content-type")
                .build();
        File createdFile = itemRepository.create(file);
        assertThat(createdFile)
                .extracting(File::getHandle)
                .isNotNull();
        assertThat(itemRepository.exists(Folder.getRoot(), "file"))
                .isTrue();
        Optional<Item> item = itemRepository.findByNameAndParentAndUser("file", Folder.getRoot(), User.root());
        assertThat(item).isPresent()
                .get()
                .isInstanceOf(File.class)
                .usingRecursiveComparison()
                .ignoringFields("created", "lastModified", "lastAccessed")
                .isEqualTo(createdFile);
    }

    @Test
    void shouldUpdateItemInDatabase() {
        Folder folder = itemRepository.findByNameAndParentAndUser("folder", Folder.getRoot(), User.root())
                .map(Folder.class::cast).orElseThrow();
        folder = folder.copyBuilder().withName("folder2").build();
        assertThat(itemRepository.update(folder)).isNotNull();
        assertThat(itemRepository.exists(Folder.getRoot(), "folder"))
                .isFalse();
        Optional<Item> item = itemRepository.findByNameAndParentAndUser("folder2", Folder.getRoot(), User.root());
        assertThat(item).isPresent().hasValue(folder);
    }

    @Test
    void shouldUpdateItemAccessRight() {
        Folder folder = itemRepository.findByNameAndParentAndUser("folder", Folder.getRoot(), User.root())
                .map(Folder.class::cast).orElseThrow();
        folder = folder.copyBuilder()
                .withOwnerAccess(none())
                .withGroupAccess(none())
                .withOtherAccess(none())
                .build();
        assertThat(itemRepository.update(folder)).isNotNull();
        assertThat(itemRepository.exists(Folder.getRoot(), "folder"))
                .isTrue();
        assertThat(itemRepository.findByNameAndParentAndUser("folder", Folder.getRoot(), User.root()))
                .isPresent().get()
                .isEqualTo(folder);
    }

    @Test
    void shouldDeleteItemInDatabase() {
        Folder folder = itemRepository.findByNameAndParentAndUser("folder", Folder.getRoot(), User.root())
                .map(Folder.class::cast).orElseThrow();
        File file = itemRepository.findByNameAndParentAndUser("to_delete", folder, User.root())
                .map(File.class::cast).orElseThrow();
        itemRepository.delete(file);
        assertThat(itemRepository.exists(folder, "to_delete"))
                .isFalse();
    }

    @Test
    void shouldListItemsInFolder() {
        List<Item> children = itemRepository.findByParentAndUser(Folder.getRoot(), User.root());
        assertThat(children).hasSizeGreaterThanOrEqualTo(1)
                .extracting(Item::getName)
                .containsOnlyOnce("folder");
    }

    @Test
    void shouldListItemsInFolderForSpecificUser() {
        UUID userId = UUID.fromString("22bdb905-73d4-479e-99fc-62d46ad27d67");
        User user = User.builder("Test").withUserId(userId).build();
        Folder folder = (Folder) itemRepository.findByNameAndParentAndUser("folder", Folder.getRoot(), user).orElseThrow();
        List<Item> children = itemRepository.findByParentAndUser(folder, user);
        assertThat(children).hasSizeGreaterThanOrEqualTo(1)
                .extracting(Item::getName)
                .containsOnlyOnce("to_select");
    }

    @Test
    void shouldFindItemInRootFolder() {
        Optional<Item> found = itemRepository.findByNameAndParentAndUser("folder", Folder.getRoot(), User.root());
        assertThat(found).isPresent()
                .map(Item::getName)
                .hasValue("folder");
    }

    @Test
    void shouldFindItemInFolder() {
        Folder found = (Folder) itemRepository.findByNameAndParentAndUser("folder", Folder.getRoot(), User.root()).orElseThrow();
        Optional<Item> item = itemRepository.findByNameAndParentAndUser("to_select", found, User.root());

        assertThat(item).isPresent();
        assertThat(item.get())
                .asInstanceOf(type(File.class))
                .extracting(Item::getName, File::getContentType, Item::getOwnerAccess, Item::getGroupAccess,
                        Item::getOtherAccess)
                .containsExactly("to_select", "content-type", none(), none(), none());
    }
}

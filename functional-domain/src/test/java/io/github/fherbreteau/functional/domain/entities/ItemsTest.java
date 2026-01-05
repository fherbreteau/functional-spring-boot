package io.github.fherbreteau.functional.domain.entities;

import static org.assertj.core.api.Assertions.BOOLEAN;
import static org.assertj.core.api.Assertions.LOCAL_DATE_TIME;
import static org.assertj.core.api.Assertions.STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.ThrowableAssert.ThrowingCallable;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ItemsTest {

    @Test
    void testFilesHaveTheGoodTypes() {
        File file = File.builder()
                .withName("file")
                .withContentType("contentType")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        assertThat(file).extracting(Item::isFile, BOOLEAN).isTrue();
        assertThat(file).extracting(Item::isFolder, BOOLEAN).isFalse();
        assertThat(file).extracting(File::getContentType, STRING).isEqualTo("contentType");
        assertThat(file).extracting(Item::getType).isEqualTo(File.TYPE);
        assertThat(file.copyBuilder().build())
                .isEqualTo(file)
                .hasSameHashCodeAs(file);

        assertThat(file).extracting(Item::getCreated, LOCAL_DATE_TIME).isNotNull().isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(file).extracting(Item::getLastAccessed, LOCAL_DATE_TIME).isNotNull().isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(file).extracting(Item::getLastModified, LOCAL_DATE_TIME).isNotNull().isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void testFoldersHaveTheGoodTypes() {
        Folder folder = Folder.builder()
                .withName("folder")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        assertThat(folder).extracting(Item::isFile, BOOLEAN).isFalse();
        assertThat(folder).extracting(Item::isFolder, BOOLEAN).isTrue();
        assertThat(folder).extracting(Item::getType).isEqualTo(Folder.TYPE);
        assertThat(folder.copyBuilder().build())
                .isEqualTo(folder)
                .hasSameHashCodeAs(folder);

        assertThat(folder)
                .extracting(Item::getCreated, LOCAL_DATE_TIME)
                .isNotNull()
                .isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(folder)
                .extracting(Item::getLastAccessed, LOCAL_DATE_TIME)
                .isNotNull()
                .isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(folder)
                .extracting(Item::getLastModified, LOCAL_DATE_TIME)
                .isNotNull()
                .isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void testItemsHaveTheGoodTypes() {
        File file = File.builder()
                .withName("file")
                .withContentType("contentType")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        Folder folder = Folder.builder()
                .withName("folder")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();

        assertThat((Item) file).isNotEqualTo(folder);
        assertThat((Item) folder).isNotEqualTo(file);
        assertThat((Object) file).isNotEqualTo(new Object());
        assertThat((Object) folder).isNotEqualTo(new Object());

        assertThat(File.builder().withHandle(UUID.randomUUID()).withName("").withOwner(User.root()).build())
                .extracting(File::getHandle)
                .isNotNull();
    }

    @Test
    void testThatAllItemHaveAPath() {
        Folder folder = Folder.builder()
                .withHandle(UUID.randomUUID())
                .withName("folder")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        File file = File.builder()
                .withHandle(UUID.randomUUID())
                .withName("file")
                .withParent(folder)
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        assertThat(file).extracting(Item::getPath, STRING).isEqualTo("/folder/file");
        folder = Folder.builder()
                .withHandle(UUID.randomUUID())
                .withOwner(User.root())
                .withGroup(Group.root())
                .withName("folder")
                .build();
        assertThat(folder).extracting(Item::getPath, STRING).isEqualTo("folder");
    }

    @Test
    void testThatHashcodeIsCorrectlyChecked() {
        Group group = Group.builder("group").build();
        User user = User.builder("user").withGroup(group).build();
        File file1 = File.builder()
                .withName("file")
                .withHandle(UUID.randomUUID())
                .withParent(Folder.getRoot())
                .withOwner(user)
                .withGroup(Group.root())
                .withContentType("test")
                .build();

        File file2 = file1.copyBuilder().withOwnerAccess(AccessRight.full()).build();
        assertThat(file1).isNotEqualTo(file2).doesNotHaveSameHashCodeAs(file2);
        file2 = file1.copyBuilder().withContentType("file").build();
        assertThat(file1).isNotEqualTo(file2).doesNotHaveSameHashCodeAs(file2);
        file2 = file1.copyBuilder().build();
        assertThat(file1).isEqualTo(file2).hasSameHashCodeAs(file2);
        file2 = file1.copyBuilder().withGroupAccess(AccessRight.full()).build();
        assertThat(file1).isNotEqualTo(file2);
        file2 = file1.copyBuilder().withOtherAccess(AccessRight.full()).build();
        assertThat(file1).isNotEqualTo(file2);
        Folder parent = Folder.builder().withName("").withOwner(User.root()).withGroup(Group.root()).build();
        file2 = file1.copyBuilder().withParent(parent).build();
        assertThat(file1).isNotEqualTo(file2);
        file2 = file1.copyBuilder().withCreated(LocalDateTime.now()).build();
        assertThat(file1).isNotEqualTo(file2);
        file2 = file1.copyBuilder().withLastModified(LocalDateTime.now()).build();
        assertThat(file1).isNotEqualTo(file2);
        file2 = file1.copyBuilder().withLastAccessed(LocalDateTime.now()).build();
        assertThat(file1).isNotEqualTo(file2);
        file2 = file1.copyBuilder().withOwner(User.builder("user").build()).build();
        assertThat(file1).isNotEqualTo(file2);
        file2 = file1.copyBuilder().withGroup(Group.builder("group").build()).build();
        assertThat(file1).isNotEqualTo(file2);

        LocalDateTime time = LocalDateTime.MIN;
        File file = File.builder()
                .withName("")
                .withGroup(Group.root())
                .withOwner(User.root())
                .withContentType("file")
                .withCreated(time)
                .withLastAccessed(time)
                .withLastModified(time)
                .build();
        assertThat(file).extracting(File::hashCode).isEqualTo(1287996204);
    }

    @Test
    void shouldCheckItemValidity() {
        ThrowingCallable itemBuilder = () -> File.builder().build();
        assertThatCode(itemBuilder)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name is required");
        itemBuilder = () -> File.builder().withName("").build();
        assertThatCode(itemBuilder)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("owner is required");

        itemBuilder = () -> Folder.builder().build();
        assertThatCode(itemBuilder)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name is required");
        itemBuilder = () -> Folder.builder().withName("").build();
        assertThatCode(itemBuilder)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("owner is required");
    }
}

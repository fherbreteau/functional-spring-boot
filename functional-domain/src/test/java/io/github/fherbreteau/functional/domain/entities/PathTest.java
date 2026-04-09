package io.github.fherbreteau.functional.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class PathTest {

    @Test
    void shouldBeAValidPath() {
        UUID folderId = UUID.randomUUID();
        Folder folder = Folder.builder()
                .withName("folder")
                .withOwner(User.root())
                .withGroup(Group.root())
                .withHandle(folderId)
                .build();
        Path path = Path.success(folder);
        assertThat(path.getName()).isEqualTo("folder");
        assertThat(path.getHandle()).isEqualTo(folderId);

        File file = File.builder()
                .withName("")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        path = Path.success(file);
        assertThat(path).extracting(Path::getAsFile).isEqualTo(file);
        assertThat(Path.success(file)).isEqualTo(path);

        Failure failure = Failure.failure("error");
        path = Path.error(failure);
        assertThat(Path.error(failure)).isEqualTo(path);
        assertThat(path.getError()).isEqualTo(failure).hasSameHashCodeAs(failure);

        assertThat(Path.error(Failure.failure("err"))).isNotEqualTo(path).doesNotHaveSameHashCodeAs(path);
        assertThat(path.getError()).isNotEqualTo(new Object());
        assertThat(Failure.failure("error")).isEqualTo(failure);
    }

    @Test
    void allPathShouldHaveAParentExceptRoot() {
        Folder folder = Folder.builder()
                .withName("folder")
                .withOwner(User.root())
                .withGroup(Group.root())
                .withParent(Folder.getRoot())
                .build();
        File file = File.builder()
                .withName("file")
                .withOwner(User.root())
                .withGroup(Group.root())
                .withParent(folder)
                .build();
        Path path = Path.success(file);
        assertThat(path.getParent())
                .isEqualTo(Path.success(folder))
                .extracting(Path::getParent)
                .isEqualTo(Path.ROOT)
                .extracting(Path::getParent)
                .extracting(Path::getError)
                .extracting(Failure::getMessage)
                .isEqualTo("Root path has no parent");

        path = Path.error(Failure.failure("error"));
        assertThat(path.getParent())
                .isEqualTo(path)
                .hasSameHashCodeAs(path)
                .isNotEqualTo(Path.ROOT)
                .doesNotHaveSameHashCodeAs(Path.ROOT);

        assertThat((Object) path).isNotEqualTo(new Object());
    }

    @Test
    void testPathHasRequiredInfoInToString() {
        Path path = Path.success(File.builder()
                .withName("")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build());
        assertThat(path).hasToString("Path{item=' root(00000000-0000-0000-0000-000000000000):root(00000000-0000-0000-0000-000000000000) --------- null'}");
        path = Path.error(Failure.failure("error"));
        assertThat(path).hasToString("Path{error=Error{message='error', reasons=[]}}");
    }

    @Test
    void testPathCanHaveAContentType() {
        Path path = Path.success(File.builder()
                .withName("")
                .withOwner(User.root())
                .withGroup(Group.root())
                .withContentType("Content-Type")
                .build());
        assertThat(path.getContentType()).isEqualTo("Content-Type");
        path = Path.success(Folder.builder()
                .withName("")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build());
        assertThat(path.getContentType()).isNull();
    }
}

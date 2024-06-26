package io.github.fherbreteau.functional.domain.entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PathTest {

    @Test
    void shouldBeAValidPath() {
        Path path = Path.success(Folder.builder()
                .withName("folder")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build());
        assertThat(path.getName()).isEqualTo("folder");

        File file = File.builder()
                .withName("")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build();
        path = Path.success(file);
        assertThat(path).extracting(Path::getAsFile).isEqualTo(file);
        assertThat(Path.success(file)).isEqualTo(path);

        Error error = Error.error("error");
        path = Path.error(error);
        assertThat(Path.error(error)).isEqualTo(path);
        assertThat(path.getError()).isEqualTo(error).hasSameHashCodeAs(error);

        assertThat(Path.error(Error.error("err"))).isNotEqualTo(path).doesNotHaveSameHashCodeAs(path);
        assertThat(path.getError()).isNotEqualTo(new Object());
        assertThat(Error.error("error")).isEqualTo(error);
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
                .extracting(Error::getMessage)
                .isEqualTo("Root path has no parent");

        path = Path.error(Error.error("error"));
        assertThat(path.getParent())
                .isEqualTo(path)
                .hasSameHashCodeAs(path)
                .isNotEqualTo(Path.ROOT)
                .doesNotHaveSameHashCodeAs(Path.ROOT);

        assertThat((Object) path).isNotEqualTo(file);
    }

    @Test
    void testPathHasRequiredInfoInToString() {
        Path path = Path.success(File.builder()
                .withName("")
                .withOwner(User.root())
                .withGroup(Group.root())
                .build());
        assertThat(path).hasToString("Path{item=' root(00000000-0000-0000-0000-000000000000):root(00000000-0000-0000-0000-000000000000) --------- null'}");
        path = Path.error(Error.error("error"));
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

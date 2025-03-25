package io.github.fherbreteau.functional.infra.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.INPUT_STREAM;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.common.jimfs.Jimfs;
import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.ContentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.InitializingBean;

@ExtendWith(MockitoExtension.class)
class FSContentRepositoryTest {
    private static final String ROOT_PATH = "/data";
    private final UUID fakeUUID = UUID.randomUUID();
    private final FileSystem fs = Jimfs.newFileSystem();
    private final File file = File.builder()
            .withName("name")
            .withHandle(fakeUUID)
            .withOwner(User.root())
            .build();
    private ContentRepository repository;

    @BeforeEach
    void setup() throws Exception {
        Path basePath = fs.getPath(ROOT_PATH);
        Files.createDirectory(basePath);
        repository = new FSContentRepository(ROOT_PATH, fs);
        ((InitializingBean) repository).afterPropertiesSet();
    }

    @AfterEach
    void tearDown() throws IOException {
        Path basePath = fs.getPath(ROOT_PATH);
        if (Files.notExists(basePath)) {
            return;
        }
        try (Stream<Path> stream = Files.list(basePath)) {
            stream.forEach(this::delete);
        }
    }

    private void delete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void shouldCreateAFileWithTheExpectedNameOnTheFileSystemWhenInitializingContent() {
        Output<Item> result = repository.initContent(file);
        assertThat(result).extracting(Output::isSuccess, BOOLEAN)
                .isTrue();
        Path filePath = fs.getPath(ROOT_PATH, fakeUUID + ".dat");
        assertThat(filePath).exists();
    }

    @Test
    void shouldReadAFileWithTheExpectedNameOnFileSystemWhenReadingContent() throws IOException {
        Path filePath = fs.getPath(ROOT_PATH, fakeUUID + ".dat");
        Files.copy(new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)), filePath);
        Output<InputStream> result = repository.readContent(file);
        assertThat(result).extracting(Output::isSuccess, BOOLEAN)
                .isTrue();
        assertThat(result).extracting(Output::getValue, INPUT_STREAM)
                .asString(StandardCharsets.UTF_8)
                .isEqualTo("content");
    }

    @Test
    void shouldWriteContentToFileWithExpectedNameOnFileSystemWhenWritingContent() throws IOException {
        Path filePath = Files.createFile(fs.getPath(ROOT_PATH, fakeUUID + ".dat"));
        InputStream input = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));
        Output<Item> result = repository.writeContent(file, input);
        assertThat(result).extracting(Output::isSuccess, BOOLEAN)
                .isTrue();
        assertThat(result).extracting(Output::getValue)
                .isEqualTo(file);
        assertThat(filePath).content(StandardCharsets.UTF_8)
                .isEqualTo("content");
    }

    @Test
    void shouldDeleteAFileWithExpectedNameOnFileSystemWhenDeletingContent() throws IOException {
        Path filePath = Files.createFile(fs.getPath(ROOT_PATH, fakeUUID + ".dat"));
        Output<Void> result = repository.deleteContent(file);
        assertThat(result).extracting(Output::isSuccess, BOOLEAN)
                .isTrue();
        assertThat(filePath).doesNotExist();
    }

    @Test
    void shouldFailCreateAsRootContainerDoesNotExists() throws IOException {
        Files.createFile(fs.getPath(ROOT_PATH, fakeUUID + ".dat"));
        Output<Item> result = repository.initContent(file);
        assertThat(result).extracting(Output::isFailure, BOOLEAN)
                .isTrue();
    }

    @Test
    void shouldFailReadAsRootContainerDoesNotExists() {
        Output<InputStream> result = repository.readContent(file);
        assertThat(result).extracting(Output::isFailure, BOOLEAN)
                .isTrue();
    }

    @Test
    void shouldFailWriteAsRootContainerDoesNotExists() {
        InputStream input = new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));
        Output<Item> result = repository.writeContent(file, input);
        assertThat(result).extracting(Output::isFailure, BOOLEAN)
                .isTrue();
    }

    @Test
    void shouldFailDeleteAsRootContainerDoesNotExists() {
        Output<Void> result = repository.deleteContent(file);
        assertThat(result).extracting(Output::isFailure, BOOLEAN)
                .isTrue();
    }

    @Test
    void shouldNotCreateExistingRootDir() throws Exception {
        Path basePath = fs.getPath(ROOT_PATH);
        Files.delete(basePath);
        ((InitializingBean) repository).afterPropertiesSet();
        basePath = fs.getPath(ROOT_PATH);
        assertThat(basePath)
                .exists();
    }
}

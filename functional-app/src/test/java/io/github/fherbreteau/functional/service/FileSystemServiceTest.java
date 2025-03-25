package io.github.fherbreteau.functional.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import io.github.fherbreteau.functional.driving.AccessParserService;
import io.github.fherbreteau.functional.driving.FileService;
import io.github.fherbreteau.functional.driving.UserService;
import io.github.fherbreteau.functional.exception.CommandException;
import io.github.fherbreteau.functional.mapper.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileSystemServiceTest {

    private FileSystemService service;
    @Mock
    private FileService fileService;
    @Mock
    private EntityMapper entityMapper;
    @Mock
    private AccessParserService accessParserService;
    @Mock
    private UserService userService;

    @BeforeEach
    void setup() {
        service = new FileSystemService(fileService, entityMapper, accessParserService, userService);
    }

    @Test
    void shouldThrowACommandExceptionWhenAnExceptionIfEmitted() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        given(file.getInputStream()).willThrow(IOException.class);
        assertThatThrownBy(() -> service.upload("/path", file, "username"))
                .isInstanceOf(CommandException.class);
    }
}

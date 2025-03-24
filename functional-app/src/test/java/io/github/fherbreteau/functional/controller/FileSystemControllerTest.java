package io.github.fherbreteau.functional.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.SchemaServiceGrpc.SchemaServiceBlockingStub;
import io.github.fherbreteau.functional.FunctionalApplication;
import io.github.fherbreteau.functional.domain.entities.AccessRight;
import io.github.fherbreteau.functional.domain.entities.Failure;
import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.Folder;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.ItemCommandType;
import io.github.fherbreteau.functional.domain.entities.ItemInput;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.Path;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driving.AccessParserService;
import io.github.fherbreteau.functional.driving.FileService;
import io.github.fherbreteau.functional.driving.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = FunctionalApplication.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@WithMockUser
class FileSystemControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private FileService fileService;
    @MockitoBean
    private AccessParserService accessParserService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private PermissionsServiceBlockingStub permissionsService;
    @MockitoBean
    private SchemaServiceBlockingStub schemaService;

    private MockMvc mvc;

    private File file;
    private Folder folder;
    private LocalDateTime now;
    private User actor;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        now = LocalDateTime.now();
        file = File.builder()
                .withName("file")
                .withParent(Folder.getRoot())
                .withOwner(User.builder("user").build())
                .withCreated(now)
                .withLastModified(now)
                .withLastAccessed(now)
                .withOwnerAccess(AccessRight.full())
                .withGroupAccess(AccessRight.readOnly())
                .withOtherAccess(AccessRight.none())
                .withContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .build();
        folder = Folder.builder()
                .withName("folder")
                .withParent(Folder.getRoot())
                .withOwner(User.builder("user").build())
                .withCreated(now)
                .withLastModified(now)
                .withLastAccessed(now)
                .withOwnerAccess(AccessRight.full())
                .withGroupAccess(AccessRight.readOnly())
                .withOtherAccess(AccessRight.none())
                .build();
        actor = User.builder("user").build();
        given(userService.findUserByName("user")).willReturn(Output.success(actor));
    }

    @Test
    void shouldReturnAListOfItemWhenFileServiceCanListElement() throws Exception {
        given(fileService.getPath("/", actor)).willReturn(Path.ROOT);
        given(fileService.processCommand(eq(ItemCommandType.LIST), eq(actor), argThat(argument -> Objects.equals(argument.getItem(), Folder.getRoot()))))
                .willReturn(Output.success(List.of(file, folder)));

        mvc.perform(get("/files")
                        .param("path", "/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("file"))
                .andExpect(jsonPath("$[0].created").value(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$[0].modified").value(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$[0].accessed").value(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$[0].content-type").value(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(jsonPath("$[1].name").value("folder"))
                .andExpect(jsonPath("$[1].created").value(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$[1].modified").value(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$[1].accessed").value(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

        given(fileService.getPath("/folder", actor)).willReturn(Path.success(folder));
        given(fileService.processCommand(eq(ItemCommandType.LIST), eq(actor), argThat(argument -> Objects.equals(argument.getItem(), folder))))
                .willReturn(Output.success(List.of()));

        mvc.perform(get("/files")
                        .param("path", "/folder"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());

    }

    @Test
    void shouldCreateFileWhenFileServiceCanCreateFile() throws Exception {
        given(fileService.getPath("/", actor)).willReturn(Path.ROOT);
        given(fileService.processCommand(eq(ItemCommandType.TOUCH), eq(actor), argThat(argument ->
                Objects.equals(argument.getItem(), Folder.getRoot())
                        && Objects.equals(argument.getName(), "file"))))
                .willReturn(Output.success(file));

        mvc.perform(post("/files/file").with(csrf())
                        .param("path", "/")
                        .param("name", "file"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("file"));

    }

    @Test
    void shouldCreateFolderWhenFileServiceCanCreateFolder() throws Exception {
        given(fileService.getPath("/", actor)).willReturn(Path.ROOT);
        given(fileService.processCommand(eq(ItemCommandType.MKDIR), eq(actor), argThat(argument ->
                Objects.equals(argument.getItem(), Folder.getRoot())
                        && Objects.equals(argument.getName(), "folder"))))
                .willReturn(Output.success(folder));

        mvc.perform(post("/files/folder").with(csrf())
                        .param("path", "/")
                        .param("name", "folder"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("folder"));

    }

    @Test
    void shouldChangeOwnerWhenFileServiceCanChangeOwner() throws Exception {
        User user2 = User.builder("user2").build();
        given(userService.findUserByName("user2")).willReturn(Output.success(user2));
        given(fileService.getPath("/folder", actor)).willReturn(Path.success(folder));
        Folder updated = folder.copyBuilder().withOwner(user2).build();
        given(fileService.processCommand(eq(ItemCommandType.CHOWN), eq(actor), argThat(argument ->
                Objects.equals(argument.getItem(), folder) && Objects.equals(argument.getUser(), user2))))
                .willReturn(Output.success(updated));

        mvc.perform(patch("/files/owner").with(csrf())
                        .param("path", "/folder")
                        .param("name", "user2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.owner").value("user2"));
    }

    @Test
    void shouldChangeGroupWhenFileServiceCanChangeGroup() throws Exception {
        Group group2 = Group.builder("group2").build();
        given(userService.findGroupByName("group2")).willReturn(Output.success(group2));
        given(fileService.getPath("/folder", actor)).willReturn(Path.success(folder));
        Folder updated = folder.copyBuilder().withGroup(group2).build();
        given(fileService.processCommand(eq(ItemCommandType.CHGRP), eq(actor), argThat(argument ->
                Objects.equals(argument.getItem(), folder) && Objects.equals(argument.getGroup(), group2))))
                .willReturn(Output.success(updated));

        mvc.perform(patch("/files/group").with(csrf())
                        .param("path", "/folder")
                        .param("name", "group2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.group").value("group2"));
    }

    @Test
    void shouldChangeRightWhenFileServiceCanChangeMode() throws Exception {
        given(fileService.getPath("/folder", actor)).willReturn(Path.success(folder));
        ItemInput input = ItemInput.builder(folder).withOwnerAccess(AccessRight.writeOnly().addExecute()).build();
        given(accessParserService.parseAccessRights("-wx", folder)).willReturn(input);
        Folder updated = folder.copyBuilder().withOwnerAccess(AccessRight.readOnly()).build();
        given(fileService.processCommand(ItemCommandType.CHMOD, actor, input))
                .willReturn(Output.success(updated));

        mvc.perform(patch("/files/mode").with(csrf())
                        .param("path", "/folder")
                        .param("right", "-wx"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access").value("dr--r-----"));
    }

    @Test
    void shouldDownloadContentWhenFileServiceCanReadFile() throws Exception {
        given(fileService.getPath("/file", actor)).willReturn(Path.success(file));
        given(fileService.processCommand(eq(ItemCommandType.DOWNLOAD), eq(actor), argThat(argument ->
                Objects.equals(argument.getItem(), file) && Objects.equals(argument.getContentType(), file.getContentType()))))
                .willReturn(Output.success(new ByteArrayInputStream("content".getBytes())));

        mvc.perform(get("/files/download").with(csrf())
                        .param("path", "/file"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(content().bytes("content".getBytes()));
    }

    @Test
    void shouldUploadContentWhenFileServiceCanWriteFile() throws Exception {
        given(fileService.getPath("/file", actor)).willReturn(Path.success(file));
        given(fileService.processCommand(eq(ItemCommandType.UPLOAD), eq(actor), argThat(argument ->
                Objects.equals(argument.getItem(), file))))
                .willReturn(Output.success(file));

        mvc.perform(multipart("/files/upload")
                        .file("file", "content".getBytes())
                        .with(csrf())
                        .param("path", "/file"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content-type").value(MediaType.APPLICATION_OCTET_STREAM_VALUE));
    }

    @Test
    void shouldDeleteItemWhenFileServiceCanDeleteFile() throws Exception {
        given(fileService.getPath("/file", actor)).willReturn(Path.success(file));
        given(fileService.processCommand(eq(ItemCommandType.DELETE), eq(actor), argThat(argument ->
                Objects.equals(argument.getItem(), file))))
                .willReturn(Output.success(file));
        mvc.perform(delete("/files")
                        .with(csrf())
                        .param("path", "/file"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnAnErrorWhenUserDoesNotExists() throws Exception {
        given(userService.findUserByName("user")).willReturn(Output.failure("user not found"));

        mvc.perform(get("/files")
                        .param("path", "/path"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));

        mvc.perform(post("/files/folder").with(csrf())
                        .param("path", "/path")
                        .param("name", "folder"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));

        mvc.perform(post("/files/file").with(csrf())
                        .param("path", "/path")
                        .param("name", "file"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));

        mvc.perform(patch("/files/owner").with(csrf())
                        .param("path", "/path")
                        .param("name", "user2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));

        mvc.perform(patch("/files/group").with(csrf())
                        .param("path", "/path")
                        .param("name", "group2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));

        mvc.perform(patch("/files/mode").with(csrf())
                        .param("path", "/path")
                        .param("right", "-wx"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));

        mvc.perform(get("/files/download").with(csrf())
                        .param("path", "/path"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));

        mvc.perform(multipart("/files/upload")
                        .file("file", "content".getBytes())
                        .with(csrf())
                        .param("path", "/path"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));
        mvc.perform(delete("/files")
                        .with(csrf())
                        .param("path", "/file"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));
    }

    @Test
    void shouldReturnAnErrorWhenPathDoesNotExists() throws Exception {
        given(fileService.getPath("/path", actor)).willReturn(Path.error(Failure.failure("path not found")));

        mvc.perform(get("/files")
                        .param("path", "/path"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("PathException"))
                .andExpect(jsonPath("$.message").value("path not found"));

        mvc.perform(post("/files/folder").with(csrf())
                        .param("path", "/path")
                        .param("name", "folder"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("PathException"))
                .andExpect(jsonPath("$.message").value("path not found"));

        mvc.perform(post("/files/file").with(csrf())
                        .param("path", "/path")
                        .param("name", "file"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("PathException"))
                .andExpect(jsonPath("$.message").value("path not found"));

        mvc.perform(patch("/files/owner").with(csrf())
                        .param("path", "/path")
                        .param("name", "user2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("PathException"))
                .andExpect(jsonPath("$.message").value("path not found"));

        mvc.perform(patch("/files/group").with(csrf())
                        .param("path", "/path")
                        .param("name", "group2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("PathException"))
                .andExpect(jsonPath("$.message").value("path not found"));

        mvc.perform(patch("/files/mode").with(csrf())
                        .param("path", "/path")
                        .param("right", "-wx"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("PathException"))
                .andExpect(jsonPath("$.message").value("path not found"));

        mvc.perform(get("/files/download").with(csrf())
                        .param("path", "/path"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("PathException"))
                .andExpect(jsonPath("$.message").value("path not found"));

        mvc.perform(multipart("/files/upload")
                        .file("file", "content".getBytes())
                        .with(csrf())
                        .param("path", "/path"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("PathException"))
                .andExpect(jsonPath("$.message").value("path not found"));
        mvc.perform(delete("/files")
                        .with(csrf())
                        .param("path", "/path"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("PathException"))
                .andExpect(jsonPath("$.message").value("path not found"));
    }

    @Test
    void shouldReturnAnErrorWhenParamDoesNotExists() throws Exception {
        Folder path = Folder.builder()
                .withName("path")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .build();
        given(fileService.getPath("/path", actor)).willReturn(Path.success(path));
        given(userService.findUserByName("user2")).willReturn(Output.failure("user2 not found"));
        given(userService.findGroupByName("group2")).willReturn(Output.failure("group2 not found"));

        mvc.perform(patch("/files/owner").with(csrf())
                        .param("path", "/path")
                        .param("name", "user2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user2 not found"));

        mvc.perform(patch("/files/group").with(csrf())
                        .param("path", "/path")
                        .param("name", "group2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("GroupException"))
                .andExpect(jsonPath("$.message").value("group2 not found"));

    }

    @Test
    void shouldReturnAnErrorWhenCommandFails() throws Exception {
        Folder path = Folder.builder()
                .withName("path")
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .build();
        given(fileService.getPath("/path", actor)).willReturn(Path.success(path));
        ItemInput input = ItemInput.builder(path).withOwnerAccess(AccessRight.writeOnly().addExecute()).build();
        given(accessParserService.parseAccessRights("-wx", path)).willReturn(input);
        User user2 = User.builder("user2").build();
        given(userService.findUserByName("user2")).willReturn(Output.success(user2));
        Group group2 = Group.builder("group2").build();
        given(userService.findGroupByName("group2")).willReturn(Output.success(group2));
        given(fileService.processCommand(any(), eq(actor), any())).willAnswer(invocation ->
                Output.failure("Command Failed", List.of("Error")));

        mvc.perform(get("/files")
                        .param("path", "/path"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"))
                .andExpect(jsonPath("$.reasons").isArray())
                .andExpect(jsonPath("$.reasons", hasSize(1)))
                .andExpect(jsonPath("$.reasons[0]").value("Error"));

        mvc.perform(post("/files/folder").with(csrf())
                        .param("path", "/path")
                        .param("name", "folder"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"))
                .andExpect(jsonPath("$.reasons").isArray())
                .andExpect(jsonPath("$.reasons", hasSize(1)))
                .andExpect(jsonPath("$.reasons[0]").value("Error"));

        mvc.perform(post("/files/file").with(csrf())
                        .param("path", "/path")
                        .param("name", "file"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"))
                .andExpect(jsonPath("$.reasons").isArray())
                .andExpect(jsonPath("$.reasons", hasSize(1)))
                .andExpect(jsonPath("$.reasons[0]").value("Error"));

        mvc.perform(patch("/files/owner").with(csrf())
                        .param("path", "/path")
                        .param("name", "user2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"))
                .andExpect(jsonPath("$.reasons").isArray())
                .andExpect(jsonPath("$.reasons", hasSize(1)))
                .andExpect(jsonPath("$.reasons[0]").value("Error"));

        mvc.perform(patch("/files/group").with(csrf())
                        .param("path", "/path")
                        .param("name", "group2"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"))
                .andExpect(jsonPath("$.reasons").isArray())
                .andExpect(jsonPath("$.reasons", hasSize(1)))
                .andExpect(jsonPath("$.reasons[0]").value("Error"));

        mvc.perform(patch("/files/mode").with(csrf())
                        .param("path", "/path")
                        .param("right", "-wx"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"))
                .andExpect(jsonPath("$.reasons").isArray())
                .andExpect(jsonPath("$.reasons", hasSize(1)))
                .andExpect(jsonPath("$.reasons[0]").value("Error"));

        mvc.perform(get("/files/download").with(csrf())
                        .param("path", "/path"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"))
                .andExpect(jsonPath("$.reasons").isArray())
                .andExpect(jsonPath("$.reasons", hasSize(1)))
                .andExpect(jsonPath("$.reasons[0]").value("Error"));

        mvc.perform(multipart("/files/upload")
                        .file("file", "content".getBytes())
                        .with(csrf())
                        .param("path", "/path"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"))
                .andExpect(jsonPath("$.reasons").isArray())
                .andExpect(jsonPath("$.reasons", hasSize(1)))
                .andExpect(jsonPath("$.reasons[0]").value("Error"));
        mvc.perform(delete("/files")
                        .with(csrf())
                        .param("path", "/path"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"))
                .andExpect(jsonPath("$.reasons").isArray())
                .andExpect(jsonPath("$.reasons", hasSize(1)))
                .andExpect(jsonPath("$.reasons[0]").value("Error"));
    }
}

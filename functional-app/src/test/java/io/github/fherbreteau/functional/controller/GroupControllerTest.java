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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Objects;
import java.util.UUID;

import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.SchemaServiceGrpc.SchemaServiceBlockingStub;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fherbreteau.functional.FunctionalApplication;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.domain.entities.UserCommandType;
import io.github.fherbreteau.functional.driving.UserService;
import io.github.fherbreteau.functional.model.GroupDTO;
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
class GroupControllerTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private PermissionsServiceBlockingStub permissionsService;
    @MockitoBean
    private SchemaServiceBlockingStub schemaService;

    private MockMvc mvc;

    private User actor;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        actor = User.builder("user").build();
        given(userService.findUserByName("user")).willReturn(Output.success(actor));
    }

    @Test
    void shouldReturnCurrentUserWithGivenName() throws Exception {
        given(userService.processCommand(eq(UserCommandType.GROUPS), eq(actor),
                argThat(argument -> Objects.isNull(argument.getName()) && Objects.isNull(argument.getUserId()))))
                .willReturn(Output.success(actor.getGroups()));
        mvc.perform(get("/groups").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].gid").value(actor.getUserId().toString()))
                .andExpect(jsonPath("$[0].name").value("user"));
    }

    @Test
    void shouldCreateGroupWithGivenParameters() throws Exception {
        UUID groupId = UUID.randomUUID();
        given(userService.processCommand(eq(UserCommandType.GROUPADD), eq(actor), argThat(argument ->
                Objects.equals(argument.getGroupId(), groupId) && Objects.equals(argument.getName(), "group"))))
                .willReturn(Output.success(Group.builder("group").withGroupId(groupId).build()));

        GroupDTO dto = GroupDTO.builder()
                .withName("group")
                .withGid(groupId)
                .build();
        mvc.perform(post("/groups").with(csrf())
                        .content(mapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("group"))
                .andExpect(jsonPath("$.gid").value(groupId.toString()));
    }

    @Test
    void shouldModifyGroupWithGivenParameters() throws Exception {
        UUID groupId = UUID.randomUUID();
        Group group = Group.builder("group").withGroupId(groupId).build();
        given(userService.processCommand(eq(UserCommandType.GROUPMOD), eq(actor), argThat(argument ->
                Objects.equals(argument.getGroupId(), groupId) && Objects.equals(argument.getName(), "group"))))
                .willReturn(Output.success(group));

        GroupDTO dto = GroupDTO.builder()
                .withGid(groupId)
                .build();
        mvc.perform(patch("/groups/group").with(csrf())
                        .content(mapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("group"))
                .andExpect(jsonPath("$.gid").value(groupId.toString()));
    }

    @Test
    void shouldDeleteGroup() throws Exception {
        UUID groupId = UUID.randomUUID();
        Group group = Group.builder("group").withGroupId(groupId).build();
        given(userService.processCommand(eq(UserCommandType.GROUPDEL), eq(actor), argThat(argument ->
                Objects.equals(argument.getName(), "group"))))
                .willReturn(Output.success(group));

        mvc.perform(delete("/groups/group").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnAnErrorWhenConnectedUserDoesNotExists() throws Exception {
        given(userService.findUserByName("user")).willReturn(Output.failure("user not found"));

        mvc.perform(get("/groups").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));

        GroupDTO dto = GroupDTO.builder()
                .withName("group")
                .build();
        mvc.perform(post("/groups").with(csrf())
                        .content(mapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));

        mvc.perform(patch("/groups/group").with(csrf())
                        .content(mapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));

        mvc.perform(delete("/groups/group").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));
    }

    @Test
    void shouldReturnAnErrorWhenCommandFails() throws Exception {
        given(userService.processCommand(any(), eq(actor), any()))
                .willReturn(Output.failure("Command Failed"));

        mvc.perform(get("/groups").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"));

        GroupDTO dto = GroupDTO.builder()
                .withName("group")
                .build();
        mvc.perform(post("/groups").with(csrf())
                        .content(mapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"));

        mvc.perform(patch("/groups/group").with(csrf())
                        .content(mapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"));

        mvc.perform(delete("/groups/group").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"));
    }
}

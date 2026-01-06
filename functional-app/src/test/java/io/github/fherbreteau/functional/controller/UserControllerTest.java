package io.github.fherbreteau.functional.controller;

import static org.apache.commons.collections4.CollectionUtils.isEqualCollection;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.SchemaServiceGrpc.SchemaServiceBlockingStub;
import io.github.fherbreteau.functional.FunctionalApplication;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.domain.entities.UserCommandType;
import io.github.fherbreteau.functional.driving.UserService;
import io.github.fherbreteau.functional.model.InputUserDTO;
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
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = FunctionalApplication.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@WithMockUser
class UserControllerTest {

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
    void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        actor = User.builder("user").build();
        given(userService.findUserByName("user")).willReturn(Output.success(actor));
    }

    @Test
    void shouldReturnCurrentUserWithGivenName() throws Exception {
        given(userService.processCommand(eq(UserCommandType.ID), eq(actor),
                argThat(argument -> Objects.isNull(argument.getName()) && Objects.isNull(argument.getUserId()))))
                .willReturn(Output.success(actor));
        mvc.perform(get("/users").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uid").value(actor.getUserId().toString()))
                .andExpect(jsonPath("$.name").value("user"))
                .andExpect(jsonPath("$.groups").isArray())
                .andExpect(jsonPath("$.groups", hasSize(1)))
                .andExpect(jsonPath("$.groups[0].gid").value(actor.getUserId().toString()))
                .andExpect(jsonPath("$.groups[0].name").value("user"));
    }

    @Test
    void shouldCreateUserWithGivenParameters() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(userService.processCommand(eq(UserCommandType.USERADD), eq(actor),
                argThat(argument -> Objects.equals(argument.getName(), "user1")
                        && Objects.equals(argument.getUserId(), userId)
                        && Objects.equals(argument.getGroupId(), groupId)
                        && Objects.equals(argument.getPassword(), "Password")
                        && isEqualCollection(argument.getGroups(), List.of("group1", "group2")))))
                .willReturn(Output.success(User.builder("user1").withUserId(userId).build()));

        InputUserDTO dto = InputUserDTO.builder()
                .withName("user1")
                .withUid(userId)
                .withGid(groupId)
                .withPassword("Password")
                .withGroups(List.of("group1", "group2"))
                .build();
        mvc.perform(post("/users").with(csrf())
                        .content(mapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uid").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("user1"))
                .andExpect(jsonPath("$.groups").isArray())
                .andExpect(jsonPath("$.groups", hasSize(1)))
                .andExpect(jsonPath("$.groups[0].gid").value(userId.toString()))
                .andExpect(jsonPath("$.groups[0].name").value("user1"));
    }

    @Test
    void shouldModifyUserWithGivenParameters() throws Exception {
        UUID groupId = UUID.randomUUID();
        Group group = Group.builder("group").withGroupId(groupId).build();
        given(userService.processCommand(eq(UserCommandType.USERMOD), eq(actor),
                argThat(argument -> Objects.equals(argument.getName(), "user1") &&
                        Objects.equals(argument.getGroupId(), groupId))))
                .willReturn(Output.success(User.builder("user1").withGroup(group).build()));

        InputUserDTO dto = InputUserDTO.builder()
                .withGid(groupId)
                .build();
        mvc.perform(patch("/users/user1").with(csrf())
                        .content(mapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("user1"))
                .andExpect(jsonPath("$.groups").isArray())
                .andExpect(jsonPath("$.groups", hasSize(1)))
                .andExpect(jsonPath("$.groups[0].name").value("group"));
    }

    @Test
    void shouldModifyUserPassword() throws Exception {
        given(userService.processCommand(eq(UserCommandType.PASSWD), eq(actor),
                argThat(argument -> Objects.equals(argument.getName(), "user1") &&
                        Objects.equals(argument.getPassword(), "Pa$sw0rd"))))
                .willReturn(Output.success(User.builder("user1").build()));

        mvc.perform(put("/users/user1/password").with(csrf())
                        .content("Pa$sw0rd")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("user1"))
                .andExpect(jsonPath("$.groups").isArray())
                .andExpect(jsonPath("$.groups", hasSize(1)))
                .andExpect(jsonPath("$.groups[0].name").value("user1"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        given(userService.processCommand(eq(UserCommandType.USERDEL), eq(actor),
                argThat(argument -> Objects.equals(argument.getName(), "user1"))))
                .willReturn(Output.success(User.builder("user1").build()));

        mvc.perform(delete("/users/user1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnAnErrorWhenConnectedUserDoesNotExists() throws Exception {
        given(userService.findUserByName("user")).willReturn(Output.failure("user not found"));

        mvc.perform(get("/users").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));

        InputUserDTO dto = InputUserDTO.builder().withName("user1").build();
        mvc.perform(post("/users").with(csrf())
                        .content(mapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));

        mvc.perform(patch("/users/user1").with(csrf())
                        .content(mapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));

        mvc.perform(put("/users/user1/password").with(csrf())
                        .content("Pa$sw0rd")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));

        mvc.perform(delete("/users/user1").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));
    }

    @Test
    void shouldReturnAnErrorWhenCommandFails() throws Exception {
        given(userService.processCommand(any(), eq(actor), any()))
                .willReturn(Output.failure("Command Failed"));

        mvc.perform(get("/users").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"));

        InputUserDTO dto = InputUserDTO.builder().withName("user1").build();
        mvc.perform(post("/users").with(csrf())
                        .content(mapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"));

        mvc.perform(patch("/users/user1").with(csrf())
                        .content(mapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"));

        mvc.perform(put("/users/user1/password").with(csrf())
                        .content("Pa$sw0rd")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"));

        mvc.perform(delete("/users/user1").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("CommandException"))
                .andExpect(jsonPath("$.message").value("Command Failed"));
    }
}

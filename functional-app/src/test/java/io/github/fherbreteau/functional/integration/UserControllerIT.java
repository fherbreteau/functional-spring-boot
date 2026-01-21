package io.github.fherbreteau.functional.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.fherbreteau.functional.FunctionalApplication;
import io.github.fherbreteau.functional.domain.entities.UserInput;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = FunctionalApplication.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerIT {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper mapper;

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @WithMockUser
    @Test
    @Order(1)
    void listUsersAsNotExistingUser() throws Exception {
        mvc.perform(get("/users").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));
    }

    @WithMockUser("root")
    @Test
    @Order(2)
    void listUserAsExistingUser() throws Exception {
        mvc.perform(get("/users").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("root"))
                .andExpect(jsonPath("$.uid").value("00000000-0000-0000-0000-000000000000"))
                .andExpect(jsonPath("$.groups").isArray())
                .andExpect(jsonPath("$.groups[0].name").value("root"))
                .andExpect(jsonPath("$.groups[0].gid").value("00000000-0000-0000-0000-000000000000"));
    }

    @WithMockUser
    @Test
    @Order(3)
    void createUserAsNonExistingUser() throws Exception {
        UserInput input = UserInput.builder("user1").build();
        mvc.perform(post("/users").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(input)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UserException"))
                .andExpect(jsonPath("$.message").value("user not found"));
    }

    @WithMockUser("root")
    @Test
    @Order(4)
    void createUserAsExistingUser() throws Exception {
        UserInput input = UserInput.builder("user1").build();
        mvc.perform(post("/users").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(input)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("user1"))
                .andExpect(jsonPath("$.uid").isNotEmpty())
                .andExpect(jsonPath("$.groups").isArray())
                .andExpect(jsonPath("$.groups[0].name").value("root"))
                .andExpect(jsonPath("$.groups[0].gid").isNotEmpty());
    }
}

package io.github.fherbreteau.functional.integration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.SchemaServiceGrpc.SchemaServiceBlockingStub;
import io.github.fherbreteau.functional.FunctionalApplication;
import io.github.fherbreteau.functional.driven.repository.UserRepository;
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
class UserControllerIT {

    @Autowired
    private WebApplicationContext context;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private PermissionsServiceBlockingStub permissionsService;
    @MockitoBean
    private SchemaServiceBlockingStub schemaService;

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
    void fakeTest() throws Exception {
        when(userRepository.exists(anyString())).thenThrow(new UnsupportedOperationException("Not Implemented Yet"));
        mvc.perform(get("/users").with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("UnsupportedOperationException"))
                .andExpect(jsonPath("$.message").value("Not Implemented Yet"));

    }
}

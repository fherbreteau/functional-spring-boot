package io.github.fherbreteau.functional.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.authzed.api.v1.PermissionsServiceGrpc;
import com.authzed.api.v1.SchemaServiceGrpc;
import io.github.fherbreteau.functional.driven.rules.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = { CheckConfiguration.class })
@ActiveProfiles("test")
class CheckConfigurationTest {

    @MockitoBean
    private PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsServiceBlockingStub;
    @MockitoBean
    private SchemaServiceGrpc.SchemaServiceBlockingStub schemaServiceBlockingStub;

    @Autowired(required = false)
    private AccessChecker accessChecker;

    @Autowired(required = false)
    private AccessUpdater accessUpdater;

    @Autowired(required = false)
    private UserChecker userChecker;

    @Autowired(required = false)
    private UserUpdater userUpdater;

    @Autowired(required = false)
    private RuleLoader ruleLoader;

    @Test
    void testAccessCheckerCreation() {
        assertThat(accessChecker).isNotNull();
    }

    @Test
    void testAccessUpdaterCreation() {
        assertThat(accessUpdater).isNotNull();
    }

    @Test
    void testUserChecker() {
        assertThat(userChecker).isNotNull();
    }

    @Test
    void testUserUpdater() {
        assertThat(userUpdater).isNotNull();
    }

    @Test
    void testRuleLoader() {
        assertThat(ruleLoader).isNotNull();
    }
}

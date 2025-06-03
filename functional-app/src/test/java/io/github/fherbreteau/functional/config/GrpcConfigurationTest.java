package io.github.fherbreteau.functional.config;


import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.CallCredentials;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = { GrpcConfiguration.class })
@ActiveProfiles("test")
class GrpcConfigurationTest {

    @Autowired(required = false)
    private CallCredentials bearerToken;

    @Test
    void testBearerTokenCreation() {
        assertThat(bearerToken).isNotNull();
    }
}

package io.github.fherbreteau.functional.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fherbreteau.functional.driven.repository.ContentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = { InfrastructureConfiguration.class })
@ActiveProfiles("test")
class InfrastructureConfigurationTest {

    @Autowired(required = false)
    private ContentRepository contentRepository;

    @Test
    void testContentRepositoryCreation() {
        assertThat(contentRepository).isNotNull();
    }
}

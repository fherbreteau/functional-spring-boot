package io.github.fherbreteau.functional.service;

import static org.mockito.Mockito.verify;

import io.github.fherbreteau.functional.driving.RuleConfigurator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationStartListenerTest {

    @Mock
    private RuleConfigurator configurator;

    @InjectMocks
    private ApplicationStartListener listener;

    @Test
    void shouldDefineRulesOnApplicationStart() {
        // Arrange
        // Act
        listener.onApplicationStarted();
        // Assert
        verify(configurator).defineRules();
    }

    @Test
    void shouldInitializeDefaultUserOnApplicationStart() {
        // Arrange
        // Act
        listener.onApplicationStarted();
        // Assert
        verify(configurator).initializeDefaultUser();
    }
}

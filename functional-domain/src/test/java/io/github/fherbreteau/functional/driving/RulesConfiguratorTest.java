package io.github.fherbreteau.functional.driving;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.domain.rules.RuleProvider;
import io.github.fherbreteau.functional.domain.user.UserManager;
import io.github.fherbreteau.functional.driving.impl.RuleConfiguratorImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RulesConfiguratorTest {
    @Mock
    private RuleProvider ruleProvider;
    @Mock
    private UserManager userManager;

    @InjectMocks
    private RuleConfiguratorImpl configurator;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void ruleConfigurationShouldDelegateToTheProvider() {
        // Arrange
        // Act
        configurator.defineRules();
        // Assert
        verify(ruleProvider).defineRules();
    }

    @Test
    void defaultUserInitShouldDelegateToProvider() {
        // Arrange
        // Act
        configurator.initializeDefaultUser();
        // Assert
        verify(userManager).createRootUser(userCaptor.capture());

        assertThat(userCaptor.getValue())
                .isEqualTo(User.root());
    }
}

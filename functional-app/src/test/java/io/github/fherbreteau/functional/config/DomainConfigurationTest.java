package io.github.fherbreteau.functional.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fherbreteau.functional.domain.access.CompositeAccessParserFactory;
import io.github.fherbreteau.functional.domain.command.CompositeItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.CompositeUserCommandFactory;
import io.github.fherbreteau.functional.domain.path.CompositePathParserFactory;
import io.github.fherbreteau.functional.domain.rules.RuleProvider;
import io.github.fherbreteau.functional.domain.user.UserManager;
import io.github.fherbreteau.functional.driven.PasswordProtector;
import io.github.fherbreteau.functional.driven.repository.ContentRepository;
import io.github.fherbreteau.functional.driven.repository.GroupRepository;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.repository.UserRepository;
import io.github.fherbreteau.functional.driven.rules.*;
import io.github.fherbreteau.functional.driving.AccessParserService;
import io.github.fherbreteau.functional.driving.FileService;
import io.github.fherbreteau.functional.driving.RuleConfigurator;
import io.github.fherbreteau.functional.driving.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = { CommandFactoryConfiguration.class, ParserFactoryConfiguration.class,
    PathFactoryConfiguration.class, DomainConfiguration.class })
@ActiveProfiles("test")
class DomainConfigurationTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private GroupRepository groupRepository;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private ContentRepository contentRepository;

    @MockitoBean
    private AccessChecker accessChecker;

    @MockitoBean
    private AccessUpdater accessUpdater;

    @MockitoBean
    private UserChecker userChecker;

    @MockitoBean
    private UserUpdater userUpdater;

    @MockitoBean
    private PasswordProtector passwordProtector;

    @MockitoBean
    private RuleLoader ruleLoader;

    @Autowired
    private FileService fileService;

    @Autowired
    private AccessParserService accessParserService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserManager userManager;

    @Autowired
    private CompositeItemCommandFactory compositeCommandFactory;

    @Autowired
    private CompositePathParserFactory compositePathFactory;

    @Autowired
    private CompositeAccessParserFactory compositeAccessParserFactory;

    @Autowired
    private CompositeUserCommandFactory compositeUserCommandFactory;

    @Autowired
    private RuleProvider ruleProvider;

    @Autowired
    private RuleConfigurator ruleConfigurator;

    @Test
    void testFileServiceCreation() {
        assertThat(fileService).isNotNull();
    }

    @Test
    void testAccessParserServiceCreation() {
        assertThat(accessParserService).isNotNull();
    }

    @Test
    void testUserServiceCreation() {
        assertThat(userService).isNotNull();
    }

    @Test
    void testUserManagerCreation() {
        assertThat(userService).isNotNull();
    }

    @Test
    void testCompositeItemCommandFactoryCreation() {
        assertThat(compositeCommandFactory).isNotNull();
    }

    @Test
    void testCompositePathParserFactoryCreation() {
        assertThat(compositePathFactory).isNotNull();
    }

    @Test
    void testCompositeAccessParserFactoryCreation() {
        assertThat(compositeAccessParserFactory).isNotNull();
    }

    @Test
    void testCompositeUserCommandFactoryCreation() {
        assertThat(compositeUserCommandFactory).isNotNull();
    }

    @Test
    void testRuleProviderCreation() {
        assertThat(ruleProvider).isNotNull();
    }

    @Test
    void testRuleConfiguratorCreation() {
        assertThat(ruleConfigurator).isNotNull();
    }
}

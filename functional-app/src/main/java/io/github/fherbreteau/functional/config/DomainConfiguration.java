package io.github.fherbreteau.functional.config;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.List;

import io.github.fherbreteau.functional.domain.access.CompositeAccessParserFactory;
import io.github.fherbreteau.functional.domain.access.factory.AccessParserFactory;
import io.github.fherbreteau.functional.domain.command.CompositeItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.CompositeUserCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.ItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.UserCommandFactory;
import io.github.fherbreteau.functional.domain.entities.Rules;
import io.github.fherbreteau.functional.domain.path.CompositePathParserFactory;
import io.github.fherbreteau.functional.domain.path.factory.PathParserFactory;
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
import io.github.fherbreteau.functional.driving.impl.AccessParserServiceImpl;
import io.github.fherbreteau.functional.driving.impl.FileServiceImpl;
import io.github.fherbreteau.functional.driving.impl.RuleConfiguratorImpl;
import io.github.fherbreteau.functional.driving.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class DomainConfiguration {

    @Bean
    public FileService fileService(CompositeItemCommandFactory compositeItemCommandFactory,
                                   CompositePathParserFactory compositePathParserFactory) {
        return new FileServiceImpl(compositeItemCommandFactory, compositePathParserFactory);
    }

    @Bean
    public AccessParserService accessParserService(CompositeAccessParserFactory compositeAccessParserFactory) {
        return new AccessParserServiceImpl(compositeAccessParserFactory);
    }

    @Bean
    public UserService userService(UserManager userManager, CompositeUserCommandFactory compositeUserCommandFactory) {
        return new UserServiceImpl(userManager, compositeUserCommandFactory);
    }

    @Bean
    public UserManager userManager(UserRepository userRepository, UserUpdater userUpdater, GroupRepository groupRepository) {
        return new UserManager(userRepository, userUpdater, groupRepository);
    }

    @Bean
    public CompositeItemCommandFactory compositeCommandFactory(ItemRepository itemRepository,
                                                               ContentRepository contentRepository,
                                                               AccessChecker accessChecker,
                                                               AccessUpdater accessUpdater,
                                                               List<ItemCommandFactory<?>> commandFactories) {
        return new CompositeItemCommandFactory(itemRepository, contentRepository, accessChecker, accessUpdater,
                commandFactories);
    }

    @Bean
    public CompositePathParserFactory compositePathFactory(ItemRepository itemRepository, AccessChecker accessChecker,
                                                           List<PathParserFactory> pathFactories) {
        CompositePathParserFactory factory = new CompositePathParserFactory(itemRepository, accessChecker, pathFactories);
        factory.configureRecursive();
        return factory;
    }

    @Bean
    public CompositeAccessParserFactory compositeAccessParserFactory(List<AccessParserFactory> accessParserFactories) {
        CompositeAccessParserFactory factory = new CompositeAccessParserFactory(accessParserFactories);
        factory.configureRecursive();
        return factory;
    }

    @Bean
    public CompositeUserCommandFactory compositeUserCommandFactory(UserRepository userRepository,
                                                                   GroupRepository groupRepository,
                                                                   UserChecker userChecker,
                                                                   UserUpdater userUpdater,
                                                                   PasswordProtector passwordProtector,
                                                                   List<UserCommandFactory<?>> userFactories) {
        return new CompositeUserCommandFactory(userRepository, groupRepository, userChecker, userUpdater,
                passwordProtector, userFactories);
    }

    @Bean
    public RuleProvider ruleProvider(RuleLoader ruleLoader, @Value("${spicedb.rules}") Resource ruleResource)
            throws IOException {
        Rules rules = new Rules(ruleResource.getContentAsString(UTF_8));
        return new RuleProvider(ruleLoader, rules);
    }

    @Bean
    public RuleConfigurator ruleConfigurator(RuleProvider ruleProvider, UserManager userManager) {
        return new RuleConfiguratorImpl(ruleProvider, userManager);
    }
}

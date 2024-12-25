package io.github.fherbreteau.functional.config;

import java.util.List;

import io.github.fherbreteau.functional.domain.access.CompositeAccessParserFactory;
import io.github.fherbreteau.functional.domain.access.factory.AccessParserFactory;
import io.github.fherbreteau.functional.domain.command.CompositeItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.CompositeUserCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.ItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.factory.UserCommandFactory;
import io.github.fherbreteau.functional.domain.path.CompositePathParserFactory;
import io.github.fherbreteau.functional.domain.path.factory.PathParserFactory;
import io.github.fherbreteau.functional.domain.user.UserManager;
import io.github.fherbreteau.functional.driven.PasswordProtector;
import io.github.fherbreteau.functional.driven.repository.ContentRepository;
import io.github.fherbreteau.functional.driven.repository.GroupRepository;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.repository.UserRepository;
import io.github.fherbreteau.functional.driven.rules.AccessChecker;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;
import io.github.fherbreteau.functional.driven.rules.UserChecker;
import io.github.fherbreteau.functional.driven.rules.UserUpdater;
import io.github.fherbreteau.functional.driving.AccessParserService;
import io.github.fherbreteau.functional.driving.FileService;
import io.github.fherbreteau.functional.driving.UserService;
import io.github.fherbreteau.functional.driving.impl.AccessParserServiceImpl;
import io.github.fherbreteau.functional.driving.impl.FileServiceImpl;
import io.github.fherbreteau.functional.driving.impl.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public UserManager userManager(UserRepository userRepository, GroupRepository groupRepository) {
        return new UserManager(userRepository, groupRepository);
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
}

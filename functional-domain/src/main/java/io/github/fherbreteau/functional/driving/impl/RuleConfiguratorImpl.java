package io.github.fherbreteau.functional.driving.impl;

import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.domain.rules.RuleProvider;
import io.github.fherbreteau.functional.domain.user.UserManager;
import io.github.fherbreteau.functional.driving.RuleConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleConfiguratorImpl implements RuleConfigurator {

    private final Logger logger = LoggerFactory.getLogger(RuleConfigurator.class.getSimpleName());
    private final RuleProvider provider;
    private final UserManager userManager;

    public RuleConfiguratorImpl(RuleProvider provider, UserManager userManager) {
        this.provider = provider;
        this.userManager = userManager;
    }

    @Override
    public void defineRules() {
        logger.debug("Define checking rules");
        provider.defineRules();
    }

    @Override
    public void initializeDefaultUser() {
        logger.debug("Define checking rules");
        userManager.createRootUser(User.root());
    }
}

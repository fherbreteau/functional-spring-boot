package io.github.fherbreteau.functional.service;

import io.github.fherbreteau.functional.driving.RuleConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class ApplicationStartListener {

    private final Logger logger = LoggerFactory.getLogger(ApplicationStartListener.class);
    private final RuleConfigurator configurator;

    public ApplicationStartListener(RuleConfigurator configurator) {
        this.configurator = configurator;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted() {
        logger.info("Load rules in the system");
        configurator.defineRules();
        configurator.initializeDefaultUser();
    }
}

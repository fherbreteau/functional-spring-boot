package io.github.fherbreteau.functional.domain.access;

import java.util.Comparator;
import java.util.List;

import io.github.fherbreteau.functional.domain.access.factory.AccessParserFactory;
import io.github.fherbreteau.functional.domain.access.factory.CompositeAccessFactory;
import io.github.fherbreteau.functional.domain.access.factory.RecursiveAccessFactory;
import io.github.fherbreteau.functional.domain.entities.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeAccessParserFactory implements CompositeAccessFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());
    private final List<AccessParserFactory> accessRightParserFactories;

    public CompositeAccessParserFactory(List<AccessParserFactory> accessRightParserFactories) {
        this.accessRightParserFactories = accessRightParserFactories.stream()
                .sorted(Comparator.comparing(AccessParserFactory::order))
                .toList();
    }

    public void configureRecursive() {
        accessRightParserFactories.stream().filter(RecursiveAccessFactory.class::isInstance)
                .map(RecursiveAccessFactory.class::cast)
                .forEach(f -> f.setCompositeFactory(this));
    }

    @Override
    public AccessParser createParser(AccessContext context, String rights, Item item) {
        logger.debug("Create access parsing for item {}", item);
        return accessRightParserFactories.stream()
                .filter(f -> f.supports(context, rights, item))
                .map(f -> f.createAccessRightParser(context, rights, item))
                .findFirst()
                .orElseThrow();
    }
}

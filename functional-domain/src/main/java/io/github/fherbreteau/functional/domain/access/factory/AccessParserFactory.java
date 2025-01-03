package io.github.fherbreteau.functional.domain.access.factory;

import io.github.fherbreteau.functional.domain.access.AccessContext;
import io.github.fherbreteau.functional.domain.access.AccessParser;
import io.github.fherbreteau.functional.domain.entities.Item;

public interface AccessParserFactory {

    boolean supports(AccessContext context, String rights, Item item);

    AccessParser createAccessRightParser(AccessContext context, String rights, Item item);

    default int order() {
        return 0;
    }
}

package io.github.fherbreteau.functional.domain.command.factory.impl;

import io.github.fherbreteau.functional.domain.command.CheckCommand;
import io.github.fherbreteau.functional.domain.command.factory.ItemCommandFactory;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckCopyItemCommand;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.ItemCommandType;
import io.github.fherbreteau.functional.domain.entities.ItemInput;
import io.github.fherbreteau.functional.driven.repository.ContentRepository;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.rules.AccessChecker;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;

import java.util.Objects;

import static java.util.Objects.nonNull;

public class CopyItemCommandFactory implements ItemCommandFactory<Item> {
    @Override
    public boolean supports(ItemCommandType type, ItemInput itemInput) {
        return type == ItemCommandType.COPY && isValid(itemInput);
    }

    private boolean isValid(ItemInput itemInput) {
        return nonNull(itemInput.getItem()) && nonNull(itemInput.getDestination()) &&
                !Objects.equals(itemInput.getItem(), itemInput.getDestination());
    }

    @Override
    public CheckCommand<Item> createCommand(ItemRepository repository, ContentRepository contentRepository,
                                            AccessChecker accessChecker, AccessUpdater accessUpdater,
                                            ItemCommandType type, ItemInput itemInput) {
        return new CheckCopyItemCommand(repository, contentRepository, accessChecker, accessUpdater,
                itemInput.getItem(), itemInput.getDestination());
    }
}

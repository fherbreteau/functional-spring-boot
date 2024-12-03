package io.github.fherbreteau.functional.domain.command.impl.check;

import io.github.fherbreteau.functional.domain.command.impl.error.ItemErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.AbstractModifyItemCommand;
import io.github.fherbreteau.functional.domain.entities.*;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.rules.AccessChecker;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class AbstractCheckLocationItemCommand<C extends AbstractModifyItemCommand<Item>> extends AbstractCheckItemCommand<Item, C> {
    protected final AccessUpdater accessUpdater;
    protected final Item source;
    protected final Item destination;

    protected AbstractCheckLocationItemCommand(ItemRepository repository, AccessChecker accessChecker, AccessUpdater accessUpdater,
                                               Item source, Item destination) {
        super(repository, accessChecker);
        this.accessUpdater = accessUpdater;
        this.source = source;
        this.destination = destination;
    }

    protected abstract ItemCommandType getCommand();

    private String getAction() {
        return getCommand().name().toLowerCase(Locale.ROOT);
    }

    @Override
    protected final List<String> checkAccess(User actor) {
        List<String> errors = new ArrayList<>();
        if (source.isFolder() && destination.isFile()) {
            errors.add(String.format("%s can't %s %s to a file", actor, getAction(), source));
        }
        Folder destFolder = getDestinationFolder();
        if (!accessChecker.canWrite(destFolder, actor)) {
            errors.add(String.format("%s can't %s file in %s", actor, getAction(), destFolder));
        }
        String name = getDestinationName();
        if (repository.exists(destFolder, name)) {
            errors.add(String.format("%s already exists in  %s", name, destFolder));
        }
        return errors;
    }

    private Folder getDestinationFolder() {
        return destination.isFile() ? destination.getParent() : (Folder) destination;
    }

    private String getDestinationName() {
        return  destination.isFile() ? destination.getName() : source.getName();
    }

    @Override
    protected final ItemErrorCommand<Item> createError(List<String> reasons) {
        ItemInput itemInput = ItemInput.builder(source).withDestination(destination).build();
        return new ItemErrorCommand<>(getCommand(), itemInput, reasons);
    }
}

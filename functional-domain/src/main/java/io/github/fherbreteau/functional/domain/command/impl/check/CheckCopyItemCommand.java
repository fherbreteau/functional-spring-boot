package io.github.fherbreteau.functional.domain.command.impl.check;

import io.github.fherbreteau.functional.domain.command.impl.error.ItemErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.CopyItemCommand;
import io.github.fherbreteau.functional.domain.entities.*;
import io.github.fherbreteau.functional.driven.repository.ContentRepository;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.rules.AccessChecker;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;

import java.util.ArrayList;
import java.util.List;

public class CheckCopyItemCommand extends AbstractCheckItemCommand<Item, CopyItemCommand> {

    private final ContentRepository contentRepository;
    private final AccessUpdater accessUpdater;
    private final Item source;
    private final Item destination;

    public CheckCopyItemCommand(ItemRepository repository, ContentRepository contentRepository,
                                AccessChecker accessChecker, AccessUpdater accessUpdater, Item source,
                                Item destination) {
        super(repository, accessChecker);
        this.contentRepository = contentRepository;
        this.accessUpdater = accessUpdater;
        this.source = source;
        this.destination = destination;
    }

    @Override
    protected List<String> checkAccess(User actor) {
        List<String> errors = new ArrayList<>();
        if (source.isFolder() && destination.isFile()) {
            errors.add(String.format("%s can't copy %s to a file", actor, source));
        }
        Folder destFolder = getDestinationFolder();
        if (!accessChecker.canWrite(destFolder, actor)) {
            errors.add(String.format("%s can't copy file in %s", actor, destFolder));
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
    protected CopyItemCommand createSuccess() {
        return new CopyItemCommand(repository, contentRepository, accessUpdater, source, destination);
    }

    @Override
    protected ItemErrorCommand<Item> createError(List<String> reasons) {
        ItemInput itemInput = ItemInput.builder(source).withDestination(destination).build();
        return new ItemErrorCommand<>(ItemCommandType.COPY, itemInput, reasons);
    }
}

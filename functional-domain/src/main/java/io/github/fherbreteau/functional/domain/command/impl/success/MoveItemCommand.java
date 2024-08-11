package io.github.fherbreteau.functional.domain.command.impl.success;

import io.github.fherbreteau.functional.domain.entities.Folder;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;

public class MoveItemCommand extends AbstractModifyItemCommand<Item> {
    private final Item source;
    private final Item destination;

    public MoveItemCommand(ItemRepository repository, AccessUpdater accessUpdater,
                           Item source, Item destination) {
        super(repository, accessUpdater);
        this.source = source;
        this.destination = destination;
    }

    @Override
    public Output<Item> execute(User actor) {
        Item newItem = null;
        return Output.success(accessUpdater.createItem(repository.create(newItem)));
    }

    private Folder getDestinationFolder() {
        return destination.isFile() ? destination.getParent() : (Folder) destination;
    }

    private String getDestinationName() {
        return  destination.isFile() ? destination.getName() : source.getName();
    }
}

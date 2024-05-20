package io.github.fherbreteau.functional.domain.command.impl.check;

import io.github.fherbreteau.functional.domain.entities.ItemCommandType;
import io.github.fherbreteau.functional.domain.entities.ItemInput;
import io.github.fherbreteau.functional.domain.command.impl.error.ItemErrorCommand;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.AccessChecker;
import io.github.fherbreteau.functional.driven.FileRepository;

public class CheckUnsupportedItemCommand extends AbstractCheckItemCommand<ItemErrorCommand> {

    private final ItemCommandType itemCommandType;

    private final ItemInput itemInput;

    public CheckUnsupportedItemCommand(FileRepository repository, AccessChecker accessChecker, ItemCommandType itemCommandType, ItemInput itemInput) {
        super(repository, accessChecker);
        this.itemCommandType = itemCommandType;
        this.itemInput = itemInput;
    }

    @Override
    protected boolean checkAccess(User actor) {
        return true;
    }

    @Override
    protected ItemErrorCommand createSuccess() {
        return new ItemErrorCommand(itemCommandType, itemInput);
    }
}

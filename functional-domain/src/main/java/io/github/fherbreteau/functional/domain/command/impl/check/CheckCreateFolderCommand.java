package io.github.fherbreteau.functional.domain.command.impl.check;

import io.github.fherbreteau.functional.domain.entities.ItemCommandType;
import io.github.fherbreteau.functional.domain.entities.ItemInput;
import io.github.fherbreteau.functional.domain.command.impl.success.CreateFolderCommand;
import io.github.fherbreteau.functional.domain.command.impl.error.ItemErrorCommand;
import io.github.fherbreteau.functional.domain.entities.Folder;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.AccessChecker;
import io.github.fherbreteau.functional.driven.FileRepository;

public class CheckCreateFolderCommand extends AbstractCheckItemCommand<CreateFolderCommand> {
    private final String name;
    private final Folder parent;

    public CheckCreateFolderCommand(FileRepository repository, AccessChecker accessChecker, String name, Folder parent) {
        super(repository, accessChecker);
        this.name = name;
        this.parent = parent;
    }

    @Override
    protected boolean checkAccess(User actor) {
        return accessChecker.canWrite(parent, actor) && !repository.exists(parent, name);
    }

    @Override
    protected CreateFolderCommand createSuccess() {
        return new CreateFolderCommand(repository, name, parent);
    }

    @Override
    protected ItemErrorCommand createError() {
        ItemInput itemInput = ItemInput.builder(parent)
                .withName(name)
                .build();
        return new ItemErrorCommand(ItemCommandType.MKDIR, itemInput);
    }
}

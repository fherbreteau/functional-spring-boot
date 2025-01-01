package io.github.fherbreteau.functional.domain.command.impl.check;

import static io.github.fherbreteau.functional.domain.Logging.debug;

import io.github.fherbreteau.functional.domain.command.impl.success.CopyItemCommand;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.ItemCommandType;
import io.github.fherbreteau.functional.driven.repository.ContentRepository;
import io.github.fherbreteau.functional.driven.repository.ItemRepository;
import io.github.fherbreteau.functional.driven.rules.AccessChecker;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;

public class CheckCopyItemCommand extends AbstractCheckLocationItemCommand<CopyItemCommand> {

    private final ContentRepository contentRepository;

    public CheckCopyItemCommand(ItemRepository repository, ContentRepository contentRepository,
                                AccessChecker accessChecker, AccessUpdater accessUpdater, Item source,
                                Item destination) {
        super(repository, accessChecker, accessUpdater, source, destination);
        this.contentRepository = contentRepository;
    }

    @Override
    protected ItemCommandType getCommand() {
        return ItemCommandType.COPY;
    }

    @Override
    protected CopyItemCommand createSuccess() {
        debug(logger,  "Creating execute command");
        return new CopyItemCommand(repository, contentRepository, accessUpdater, source, destination);
    }
}

package io.github.fherbreteau.functional.domain.command.impl.check;

import io.github.fherbreteau.functional.domain.command.CheckCommand;
import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.impl.error.ItemErrorCommand;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.AccessChecker;
import io.github.fherbreteau.functional.driven.FileRepository;

import java.util.List;

public abstract class AbstractCheckItemCommand<C extends Command<Output>> implements CheckCommand<Output> {

    protected final FileRepository repository;

    protected final AccessChecker accessChecker;

    protected AbstractCheckItemCommand(FileRepository repository, AccessChecker accessChecker) {
        this.repository = repository;
        this.accessChecker = accessChecker;
    }

    @Override
    public final Command<Output> execute(User actor) {
        List<String> reasons = checkAccess(actor);
        if (!reasons.isEmpty()) {
            return createError(reasons);
        }
        return createSuccess();
    }

    protected abstract List<String> checkAccess(User actor);

    protected abstract C createSuccess();

    protected ItemErrorCommand createError(List<String> reason) {
        throw new UnsupportedOperationException("Unsupported Command always succeed");
    }
}
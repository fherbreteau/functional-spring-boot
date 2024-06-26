package io.github.fherbreteau.functional.domain.command.impl.error;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.entities.ItemCommandType;
import io.github.fherbreteau.functional.domain.entities.ItemInput;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;

import java.util.List;

public class ItemErrorCommand<T> implements Command<Output<T>> {

    private final ItemCommandType type;
    private final ItemInput itemInput;
    private final List<String> reasons;

    public ItemErrorCommand(ItemCommandType type, ItemInput itemInput) {
        this(type, itemInput, List.of());
    }

    public ItemErrorCommand(ItemCommandType type, ItemInput itemInput, List<String> reasons) {
        this.type = type;
        this.itemInput = itemInput;
        this.reasons = reasons;
    }

    @Override
    public Output<T> execute(User actor) {
        return Output.error(String.format("%s with arguments %s failed for %s", type, itemInput, actor), reasons);
    }
}

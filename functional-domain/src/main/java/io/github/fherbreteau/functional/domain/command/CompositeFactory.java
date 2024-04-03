package io.github.fherbreteau.functional.domain.command;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.factory.CommandFactory;
import io.github.fherbreteau.functional.domain.command.CommandType;
import io.github.fherbreteau.functional.domain.command.Input;
import io.github.fherbreteau.functional.domain.command.impl.UnsupportedCommand;
import io.github.fherbreteau.functional.driven.AccessChecker;
import io.github.fherbreteau.functional.driven.FileRepository;

import java.util.List;

public class CompositeFactory {

    private final FileRepository repository;

    private final AccessChecker accessChecker;
    private final List<CommandFactory> factories;

    public CompositeFactory(FileRepository repository, AccessChecker accessChecker, List<CommandFactory> factories) {
        this.repository = repository;
        this.accessChecker = accessChecker;
        this.factories = factories;
    }

    @SuppressWarnings("unchecked")
    public <T> Command<T> createCommand(CommandType type, Input input) {
        return (Command<T>) factories.stream()
                .filter(f -> f.supports(type, input))
                .map(f -> f.createCommand(repository, accessChecker, type, input))
                .findFirst()
                .orElseThrow();
    }

    private Command<?> unsupportedCommand(CommandType type, Input input) {
        return new UnsupportedCommand(type, input);
    }
}

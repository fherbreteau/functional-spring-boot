package io.github.fherbreteau.functional.exception;

import java.io.IOException;
import java.io.Serial;
import java.util.List;

import io.github.fherbreteau.functional.domain.entities.Failure;

public class CommandException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5103743186680944314L;
    private final String[] reasons;

    public CommandException(Failure failure) {
        super(failure.getMessage() + " with reasons: " + failure.getReasons());
        reasons = failure.getReasons().toArray(String[]::new);
    }

    public CommandException(IOException exception) {
        super(exception.getMessage());
        reasons = new String[0];
    }

    public List<String> getReasons() {
        return List.of(reasons);
    }
}

package io.github.fherbreteau.functional.controller;

import io.github.fherbreteau.functional.exception.CommandException;
import io.github.fherbreteau.functional.exception.GroupException;
import io.github.fherbreteau.functional.exception.PathException;
import io.github.fherbreteau.functional.exception.UserException;
import io.github.fherbreteau.functional.model.ErrorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class FunctionalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(FunctionalExceptionHandler.class);

    @ExceptionHandler(CommandException.class)
    public ResponseEntity<ErrorDTO> handleCommandException(CommandException e) {
        log.info("handleCommandException", e);
        ErrorDTO error = ErrorDTO.builder()
                .withType(e.getClass().getSimpleName())
                .withMessage(e.getMessage())
                .withReasons(e.getReasons())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(PathException.class)
    public ResponseEntity<ErrorDTO> handlePathException(PathException e) {
        log.info("handlePathException", e);
        ErrorDTO error = ErrorDTO.builder()
                .withType(e.getClass().getSimpleName())
                .withMessage(e.getMessage())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorDTO> handleUserException(UserException e) {
        log.info("handleUserException", e);
        ErrorDTO error = ErrorDTO.builder()
                .withType(e.getClass().getSimpleName())
                .withMessage(e.getMessage())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(GroupException.class)
    public ResponseEntity<ErrorDTO> handlePathException(GroupException e) {
        log.info("handlePathException", e);
        ErrorDTO error = ErrorDTO.builder()
                .withType(e.getClass().getSimpleName())
                .withMessage(e.getMessage())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorDTO> handleUnsupportedOperationException(UnsupportedOperationException e) {
        log.info("handleUnsupportedOperationException", e);
        ErrorDTO error = ErrorDTO.builder()
                .withType(e.getClass().getSimpleName())
                .withMessage(e.getMessage())
                .build();
        return ResponseEntity.internalServerError().body(error);
    }
}

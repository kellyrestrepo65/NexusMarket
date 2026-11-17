package com.nexusmarket.application.adapters.in.rest.handlers;

import com.nexusmarket.domain.exceptions.DomainException;
import com.nexusmarket.domain.exceptions.DuplicateDataException;
import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.exceptions.InvalidArgumentException;
import com.nexusmarket.domain.exceptions.UnauthenticatedException;
import com.nexusmarket.application.adapters.in.rest.responses.Responses.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Traduce las excepciones del dominio a HTTP: 400 datos invalidos, 401 sin
 * usuario identificado, 403 rol o propiedad no permitidos, 404 no encontrado,
 * 409 duplicado o cambio concurrente, 422 cualquier otra regla de negocio
 * rota (stock, estado...).
 */
@RestControllerAdvice(basePackages = "com.nexusmarket.application.adapters.in.rest")
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ErrorResponse> unauthenticated(UnauthenticatedException e) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponse> forbidden(ForbiddenOperationException e) {
        return error(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(EntityNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DuplicateDataException.class)
    public ResponseEntity<ErrorResponse> duplicate(DuplicateDataException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> conflict(RuntimeException e) {
        return error(HttpStatus.CONFLICT, "The data was changed by another operation, please retry");
    }

    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<ErrorResponse> invalidArgument(InvalidArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> invalidBody(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "The request body is malformed");
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> businessRule(DomainException e) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), status.name(), message));
    }
}

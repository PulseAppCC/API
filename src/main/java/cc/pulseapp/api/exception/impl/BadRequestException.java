package cc.pulseapp.api.exception.impl;

import cc.pulseapp.api.model.IGenericResponse;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is raised
 * when a bad request is made.
 *
 * @author Braydon
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public final class BadRequestException extends RuntimeException {
    public BadRequestException(@NonNull IGenericResponse error) {
        super(error.name());
    }
}
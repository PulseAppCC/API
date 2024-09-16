package cc.pulseapp.api.model;

import lombok.NonNull;

/**
 * Represents a generic response.
 *
 * @author Braydon
 */
public interface IGenericResponse {
    /**
     * Get the name of this response.
     *
     * @return the response name
     */
    @NonNull String name();
}
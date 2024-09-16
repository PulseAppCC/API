package cc.pulseapp.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Information about this app.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter @ToString
public final class AppInformation {
    /**
     * The version of the app.
     */
    @NonNull private final String version;

    /**
     * The environment of the app.
     */
    @NonNull private final String environment;
}
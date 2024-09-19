package cc.pulseapp.api.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

/**
 * The two-factor authentication
 * profile of a {@link User}.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter @ToString
public final class TFAProfile {
    /**
     * The TFA secret of the user.
     */
    @NonNull private final String secret;

    /**
     * The sale for the user's backup codes.
     */
    @NonNull private final String backupCodesSalt;

    /**
     * The (encrypted) backup codes of the user.
     */
    @NonNull private final List<String> backupCodes;
}
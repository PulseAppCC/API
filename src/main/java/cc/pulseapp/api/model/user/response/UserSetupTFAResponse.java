package cc.pulseapp.api.model.user.response;

import cc.pulseapp.api.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * The response for when a {@link User}
 * initializes the setup of two-factor
 * authentication.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter @ToString
public final class UserSetupTFAResponse {
    /**
     * The TFA secret.
     */
    @NonNull private final String secret;

    /**
     * The URL to the QR code.
     */
    @NonNull private final String qrCodeUrl;
}
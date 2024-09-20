package cc.pulseapp.api.model.user.input;

import cc.pulseapp.api.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * The input to disable TFA for a {@link User}.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter @ToString
public final class DisableTFAInput {
    /**
     * The TFA pin to validate.
     */
    private final String pin;

    /**
     * Check if this input is valid.
     *
     * @return whether this input is valid
     */
    public boolean isValid() {
        return pin != null && (pin.length() == 6);
    }
}
package cc.pulseapp.api.model.user.input;

import cc.pulseapp.api.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * The input to check if
 * a {@link User} exists.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter @ToString
public final class UserExistsInput {
    /**
     * The email of the user to check.
     */
    private final String email;

    /**
     * The captcha response token to validate.
     */
    private final String captchaResponse;

    /**
     * Check if this input is valid.
     *
     * @return whether this input is valid
     */
    public boolean isValid() {
        return email != null && (!email.isBlank())
                && captchaResponse != null && (!captchaResponse.isBlank());
    }
}
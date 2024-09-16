package cc.pulseapp.api.model.user.input;

import cc.pulseapp.api.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Input to register a new {@link User}.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter @ToString
public final class UserRegistrationInput {
    /**
     * The name of the user to create.
     */
    private final String email;

    /**
     * The username of the user to create.
     */
    private final String username;

    /**
     * The password of the user to create.
     */
    private final String password;

    /**
     * The confirmation password of the user to create.
     */
    private final String passwordConfirmation;

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
                && password != null && (!password.isBlank())
                && passwordConfirmation != null && (!passwordConfirmation.isBlank())
                && captchaResponse != null && (!captchaResponse.isBlank());
    }
}
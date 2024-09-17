package cc.pulseapp.api.model.user.input;

import cc.pulseapp.api.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Input to login a {@link User}.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter @ToString
public final class UserLoginInput {
    /**
     * The email of the user to login with.
     */
    private final String email;

    /**
     * The username of the user to login with.
     */
    private final String username;

    /**
     * The password of the user to login with.
     */
    private final String password;

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
        return (email != null && (!email.isBlank()) || username != null && (!username.isBlank()))
                && password != null && (!password.isBlank())
                && captchaResponse != null && (!captchaResponse.isBlank());
    }
}
package cc.pulseapp.api.model.user.response;

import cc.pulseapp.api.model.user.Session;
import cc.pulseapp.api.model.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * The response for successfully logging in.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter @ToString
public final class UserAuthResponse {
    /**
     * The created session for the user.
     */
    @NonNull private final Session session;

    /**
     * The user logging in.
     */
    @NonNull private final UserDTO user;
}
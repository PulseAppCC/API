package cc.pulseapp.api.model;

import lombok.*;

import java.util.Date;

/**
 * @author Braydon
 */
@AllArgsConstructor @Getter @EqualsAndHashCode(onlyExplicitlyIncluded = true) @ToString
public final class User {
    /**
     * The snowflake id of this user.
     */
    @EqualsAndHashCode.Include private final long id;

    /**
     * This user's username.
     */
    @NonNull private final String username;

    /**
     * The password for this user.
     */
    @NonNull private final String password;

    /**
     * The salt for this user's password.
     */
    @NonNull private final String passwordSalt;

    /**
     * The date this user last logged in.
     */
    @NonNull private final Date lastLogin;
}
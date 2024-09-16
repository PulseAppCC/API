package cc.pulseapp.api.model.user;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author Braydon
 */
@AllArgsConstructor @Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true) @ToString
@Document("users")
public final class User {
    /**
     * The snowflake id of this user.
     */
    @Id @EqualsAndHashCode.Include private final long id;

    /**
     * This user's email.
     */
    @Indexed @NonNull private final String email;

    /**
     * This user's username.
     */
    @Indexed @NonNull private final String username;

    /**
     * The password for this user.
     */
    @NonNull private final String password;

    /**
     * The salt for this user's password.
     */
    @NonNull private final String passwordSalt;

    /**
     * The tier of this user.
     */
    @NonNull private final UserTier tier;

    /**
     * The flags for this user.
     */
    private final int flags;

    /**
     * The date this user last logged in.
     */
    @NonNull private final Date lastLogin;

    /**
     * Check if this user has a given flag.
     *
     * @param flag the flag to check
     * @return whether this user has the flag
     */
    public boolean hasFlag(@NonNull UserFlag flag) {
        return (flags & flag.ordinal()) != 0;
    }
}
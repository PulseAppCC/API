package cc.pulseapp.api.model.user;

import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

/**
 * The DTO for a {@link User}.
 *
 * @author Braydon
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE) @Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true) @ToString
public final class UserDTO {
    /**
     * The snowflake id of this user.
     */
    @EqualsAndHashCode.Include private final long snowflake;

    /**
     * This user's email.
     */
    @Indexed @NonNull private final String email;

    /**
     * This user's username.
     */
    @Indexed @NonNull private final String username;

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
     * The date this user was created.
     */
    @NonNull private final Date created;

    /**
     * Create a DTO from the given user.
     *
     * @param user         the user
     * @param creationTime the user's creation time
     * @return the user dto
     */
    @NonNull
    public static UserDTO asDTO(@NonNull User user, @NonNull Date creationTime) {
        return new UserDTO(user.getSnowflake(), user.getEmail(), user.getUsername(),
                user.getTier(), user.getFlags(), user.getLastLogin(), creationTime
        );
    }
}
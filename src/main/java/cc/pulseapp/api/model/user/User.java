package cc.pulseapp.api.model.user;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author Braydon
 */
@AllArgsConstructor @Setter @Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true) @ToString
@Document("users")
public final class User {
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    public static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9_.]*$");

    /**
     * The snowflake id of this user.
     */
    @Id @EqualsAndHashCode.Include private final long snowflake;

    /**
     * This user's email.
     */
    @Indexed @NonNull private final String email;

    /**
     * This user's username.
     */
    @Indexed @NonNull private final String username;

    /**
     * The (encrypted) password for this user.
     */
    @NonNull private final String password;

    /**
     * The salt for this user's password.
     */
    @NonNull private final String passwordSalt;

    /**
     * The hash to the avatar of this user, if any.
     */
    private final String avatar;

    /**
     * The tier of this user.
     */
    @NonNull private final UserTier tier;

    /**
     * The TFA profile of this
     * user, present if TFA is enabled.
     */
    private TFAProfile tfa;

    /**
     * The flags for this user.
     */
    private int flags;

    /**
     * The date this user last logged in.
     */
    @NonNull private Date lastLogin;

    /**
     * Add a flag to this user.
     *
     * @param flag the flag to add
     */
    public void addFlag(@NonNull UserFlag flag) {
        flags |= flag.bitwise();
    }

    /**
     * Check if this user has a given flag.
     *
     * @param flag the flag to check
     * @return whether this user has the flag
     */
    public boolean hasFlag(@NonNull UserFlag flag) {
        int bitwise = flag.bitwise();
        return (flags & bitwise) == bitwise;
    }
}
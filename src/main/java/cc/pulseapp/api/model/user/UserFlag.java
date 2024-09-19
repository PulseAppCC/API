package cc.pulseapp.api.model.user;

/**
 * Flags for a {@link User}.
 *
 * @author Braydon
 */
public enum UserFlag {
    /**
     * The user is disabled.
     */
    DISABLED,

    /**
     * The user's email has been verified.
     */
    EMAIL_VERIFIED,

    /**
     * The user completed the onboarding process.
     */
    COMPLETED_ONBOARDING,

    /**
     * The user has two-factor auth enabled.
     */
    TFA_ENABLED,

    /**
     * The user is an administrator.
     */
    ADMINISTRATOR;

    /**
     * Get the bitwise value of this flag.
     *
     * @return the bitwise value
     */
    public int bitwise() {
        return 1 << ordinal();
    }
}
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
     * The user completed the onboarding process.
     */
    COMPLETED_ONBOARDING
}
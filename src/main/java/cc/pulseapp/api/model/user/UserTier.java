package cc.pulseapp.api.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * The tier of a {@link User}.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter @ToString
public enum UserTier {
    FREE(1, 2, false, 10);

    /**
     * The maximum number of organizations a user can have.
     */
    private final int maxOrganizations;

    /**
     * The maximum number of status pages a user can have.
     */
    private final int maxStatusPages;

    /**
     * Whether this tier has banners on status pages.
     */
    private final boolean statusPageBanners;

    /**
     * The maximum number of components a user can have on a status page.
     */
    private final int maxStatusPageComponents;
}
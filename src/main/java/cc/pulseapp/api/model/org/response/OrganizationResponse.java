package cc.pulseapp.api.model.org.response;

import cc.pulseapp.api.model.page.StatusPage;
import cc.pulseapp.api.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

/**
 * The response to return when fetching
 * a {@link User}'s {@link Organization}'s.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter @ToString
public final class OrganizationResponse {
    /**
     * The organizations in this response.
     */
    @NonNull private final List<Organization> organizations;

    /**
     * An organization wrapper that includes the owned status pages.
     */
    @Getter
    public static class Organization extends cc.pulseapp.api.model.org.Organization {
        /**
         * The status pages owned by this organization.
         */
        @NonNull private final List<StatusPage> statusPages;

        public Organization(@NonNull cc.pulseapp.api.model.org.Organization origin, @NonNull List<StatusPage> statusPages) {
            super(origin.getSnowflake(), origin.getName(), origin.getSlug(), origin.getOwnerSnowflake());
            this.statusPages = statusPages;
        }
    }
}
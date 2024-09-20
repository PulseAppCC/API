package cc.pulseapp.api.model.org;

import cc.pulseapp.api.model.page.StatusPage;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

/**
 * A "detailed" {@link Organization}, or in
 * other words, an {@link Organization} with
 * owned {@link StatusPage}'s.
 *
 * @author Braydon
 */
@Getter
public final class DetailedOrganization extends cc.pulseapp.api.model.org.Organization {
    /**
     * The status pages owned by this organization.
     */
    @NonNull private final List<StatusPage> statusPages;

    public DetailedOrganization(@NonNull cc.pulseapp.api.model.org.Organization origin, @NonNull List<StatusPage> statusPages) {
        super(origin.getSnowflake(), origin.getName(), origin.getSlug(), origin.getLogo(), origin.getMembers(), origin.getOwnerSnowflake());
        this.statusPages = statusPages;
    }
}
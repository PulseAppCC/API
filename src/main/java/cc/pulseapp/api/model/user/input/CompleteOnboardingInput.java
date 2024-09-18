package cc.pulseapp.api.model.user.input;

import cc.pulseapp.api.model.org.Organization;
import cc.pulseapp.api.model.page.StatusPage;
import cc.pulseapp.api.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * The input to complete onboarding for a {@link User}.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter @ToString
public final class CompleteOnboardingInput {
    /**
     * The name of the {@link Organization} to create.
     */
    private final String organizationName;

    /**
     * The name of the {@link StatusPage} to create.
     */
    private final String statusPageName;

    /**
     * Check if this input is valid.
     *
     * @return whether this input is valid
     */
    public boolean isValid() {
        return organizationName != null && (!organizationName.isBlank())
                && statusPageName != null && (!statusPageName.isBlank());
    }
}
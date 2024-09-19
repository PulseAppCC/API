package cc.pulseapp.api.model;

import lombok.*;

import java.util.Objects;

/**
 * A feature flag.
 *
 * @author Braydon
 */
@RequiredArgsConstructor @Getter @ToString
public enum Feature {
    USER_REGISTRATION_ENABLED("user-registration"),
    ORG_CREATION_ENABLED("org-creation"),
    STATUS_PAGE_CREATION_ENABLED("status-page-creation");

    public static final Feature[] VALUES = values();

    /**
     * The name of this feature.
     */
    @NonNull private final String id;

    /**
     * Whether this feature is enabled.
     */
    @Setter private boolean enabled;

    /**
     * The value of this feature, if any.
     */
    @Setter private Object value;


    /**
     * Get a feature by its id.
     *
     * @param id the feature id
     * @return the feature, null if none
     */
    public static Feature getById(@NonNull String id) {
        for (Feature feature : VALUES) {
            if (feature.getId().equals(id)) {
                return feature;
            }
        }
        return null;
    }

    /**
     * Get the combined hash
     * code of all features.
     *
     * @return the combined hash code
     */
    public static int hash() {
        int hash = 0;
        for (Feature feature : VALUES) {
            hash += Objects.hash(feature.isEnabled(), feature.getValue());
        }
        return hash;
    }
}
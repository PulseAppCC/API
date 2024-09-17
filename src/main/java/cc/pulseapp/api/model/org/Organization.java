package cc.pulseapp.api.model.org;

import cc.pulseapp.api.model.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * An organization owned by a {@link User}.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true) @ToString
@Document("organizations")
public final class Organization {
    /**
     * The snowflake id of this organization.
     */
    @Id @EqualsAndHashCode.Include private final long snowflake;

    /**
     * The name of this organization.
     */
    @Indexed @NonNull private final String name;

    /**
     * The snowflake of the {@link User}
     * that owns this organization.
     */
    @Indexed private final long ownerSnowflake;
}
package cc.pulseapp.api.model.org;

import cc.pulseapp.api.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * A member of an {@link Organization}.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter @ToString
public final class OrganizationMember {
    /**
     * The snowflake of the {@link User}
     * this member belongs to.
     */
    private final long userSnowflake;

    /**
     * The bitwise permissions of this member.
     */
    private final int permissions;
}
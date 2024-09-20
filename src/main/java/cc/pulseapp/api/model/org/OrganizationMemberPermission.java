package cc.pulseapp.api.model.org;

import cc.pulseapp.api.model.page.StatusPage;

/**
 * The permissions of a {@link OrganizationMember}.
 *
 * @author Braydon
 */
public enum OrganizationMemberPermission {
    /**
     * The member can create, edit, and delete
     * {@link StatusPage}s for the organization.
     */
    MANAGE_STATUS_PAGES,

    /**
     * The member can create, edit, and
     * delete automations for the organization.
     */
    MANAGE_AUTOMATIONS,

    /**
     * The member can create, edit, and
     * delete incidents for the organization.
     */
    MANAGE_INCIDENTS,

    /**
     * The member can view insights of the organization.
     */
    VIEW_INSIGHTS,

    /**
     * The member can view audit logs of the organization.
     */
    VIEW_AUDIT_LOGS,

    /**
     * The member can edit the organization.
     */
    MANAGE_ORGANIZATION;

    /**
     * Get the bitwise value
     * of this permission.
     *
     * @return the bitwise value
     */
    public int bitwise() {
        return 1 << ordinal();
    }
}
package cc.pulseapp.api.service;

import cc.pulseapp.api.exception.impl.BadRequestException;
import cc.pulseapp.api.model.Feature;
import cc.pulseapp.api.model.IGenericResponse;
import cc.pulseapp.api.model.org.Organization;
import cc.pulseapp.api.model.org.response.OrganizationResponse;
import cc.pulseapp.api.model.user.User;
import cc.pulseapp.api.repository.OrganizationRepository;
import cc.pulseapp.api.repository.StatusPageRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Braydon
 */
@Service
public final class OrganizationService {
    /**
     * The auth service to use for retrieving the user.
     */
    @NonNull private final AuthService authService;

    /**
     * The service to use for snowflake generation.
     */
    @NonNull private final SnowflakeService snowflakeService;

    /**
     * The repository to store and retrieve organizations.
     */
    @NonNull private final OrganizationRepository orgRepository;

    /**
     * The repository to retrieve status pages from.
     */
    @Nonnull private final StatusPageRepository statusPageRepository;

    @Autowired
    public OrganizationService(@NonNull AuthService authService, @NonNull SnowflakeService snowflakeService,
                               @NonNull OrganizationRepository orgRepository, @Nonnull StatusPageRepository statusPageRepository) {
        this.authService = authService;
        this.snowflakeService = snowflakeService;
        this.orgRepository = orgRepository;
        this.statusPageRepository = statusPageRepository;
    }

    /**
     * Create a new organization.
     *
     * @param name  the org name
     * @param slug  the org slug
     * @param owner the owner of the org
     * @return the created org
     * @throws BadRequestException if the org creation fails
     */
    @NonNull
    public Organization createOrganization(@Nonnull String name, @Nonnull String slug, @NonNull User owner) throws BadRequestException {
        // Ensure org creation is enabled
        if (!Feature.ORG_CREATION_ENABLED.isEnabled()) {
            throw new BadRequestException(Error.ORG_CREATION_DISABLED);
        }
        // Ensure the org name isn't taken
        if (orgRepository.findByNameIgnoreCase(name) != null) {
            throw new BadRequestException(Error.ORG_NAME_TAKEN);
        }
        // Create the org and return it
        return orgRepository.save(new Organization(snowflakeService.generateSnowflake(), name, slug, owner.getSnowflake()));
    }

    @NonNull
    public OrganizationResponse getOrganizations() {
        User user = authService.getAuthenticatedUser();
        List<OrganizationResponse.Organization> organizations = new ArrayList<>();
        for (Organization org : orgRepository.findByOwnerSnowflake(user.getSnowflake())) {
            organizations.add(new OrganizationResponse.Organization(org, statusPageRepository.findByOrgSnowflake(org.getSnowflake())));
        }
        return new OrganizationResponse(organizations);
    }

    /**
     * Organization errors.
     */
    private enum Error implements IGenericResponse {
        ORG_CREATION_DISABLED,
        ORG_NAME_TAKEN
    }
}
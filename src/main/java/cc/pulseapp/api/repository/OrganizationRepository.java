package cc.pulseapp.api.repository;

import cc.pulseapp.api.model.org.Organization;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * The repository for interacting with {@link Organization}'s.
 *
 * @author Braydon
 */
public interface OrganizationRepository extends MongoRepository<Organization, Long> {
    /**
     * Find an organization by its name.
     *
     * @param name the name of the org
     * @return the org with the name
     */
    Organization findByNameIgnoreCase(@NonNull String name);

    /**
     * Get the organizations that
     * are owned by the given user.
     *
     * @param ownerSnowflake the user snowflake
     * @return the owned organizations
     */
    List<Organization> findByOwnerSnowflake(long ownerSnowflake);
}
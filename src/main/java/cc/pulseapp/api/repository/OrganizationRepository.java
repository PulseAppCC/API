package cc.pulseapp.api.repository;

import cc.pulseapp.api.model.org.Organization;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The repository for interacting with {@link Organization}'s.
 *
 * @author Braydon
 */
@Repository
public interface OrganizationRepository extends MongoRepository<Organization, Long> {
    /**
     * Get the organization that has the given slug.
     *
     * @param slug the organization slug
     * @return the organization, null if none
     */
    Organization findBySlug(@NonNull String slug);

    /**
     * Get the organizations that
     * are owned by the given user.
     *
     * @param ownerSnowflake the user snowflake
     * @return the owned organizations
     */
    List<Organization> findByOwnerSnowflake(long ownerSnowflake);

    /**
     * Get the organizations that the user
     * either owns or is a member of.
     *
     * @param userSnowflake the user's snowflake
     * @return the organizations the user has access to
     */
    @Query("{ '$or': [ { 'ownerSnowflake': ?0 }, { 'members.userSnowflake': ?0 } ] }")
    List<Organization> findByUserAccess(long userSnowflake);
}
package cc.pulseapp.api.repository;

import cc.pulseapp.api.model.page.StatusPage;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The repository for interacting with {@link StatusPage}'s.
 *
 * @author Braydon
 */
public interface StatusPageRepository extends MongoRepository<StatusPage, Long> {
    /**
     * Find a status page by its name.
     *
     * @param name the name of the status page
     * @return the status page with the name
     */
    StatusPage findByNameIgnoreCase(@NonNull String name);
}
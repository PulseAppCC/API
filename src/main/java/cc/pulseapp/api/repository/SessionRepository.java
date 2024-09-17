package cc.pulseapp.api.repository;

import cc.pulseapp.api.model.user.Session;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;

/**
 * The repository for {@link Session}'s.
 *
 * @author Braydon
 */
public interface SessionRepository extends CrudRepository<Session, String> {
    /**
     * Find a session by the access token.
     *
     * @param accessToken the access token to search by
     * @return the session, null if none
     */
    Session findByAccessToken(@NonNull String accessToken);
}
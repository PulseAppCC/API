package cc.pulseapp.api.repository;

import cc.pulseapp.api.model.user.session.Session;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The repository for {@link Session}'s.
 *
 * @author Braydon
 */
@Repository
public interface SessionRepository extends CrudRepository<Session, String> {
    /**
     * Find a session by the access token.
     *
     * @param accessToken the access token to search by
     * @return the session, null if none
     */
    Session findByAccessToken(@NonNull String accessToken);

    /**
     * Get all sessions for a user.
     *
     * @param userSnowflake the user's snowflake
     * @return the sessions
     */
    List<Session> findAllByUserSnowflake(long userSnowflake);
}
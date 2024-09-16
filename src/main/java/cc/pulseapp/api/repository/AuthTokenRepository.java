package cc.pulseapp.api.repository;

import cc.pulseapp.api.model.user.AuthToken;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;

/**
 * The repository for {@link AuthToken}'s.
 *
 * @author Braydon
 */
public interface AuthTokenRepository extends CrudRepository<AuthToken, String> {
    /**
     * Find an auth token by the access token.
     *
     * @param accessToken the access token to search by
     * @return the auth token, null if none
     */
    AuthToken findByAccessToken(@NonNull String accessToken);
}
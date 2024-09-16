package cc.pulseapp.api.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.util.UUID;

/**
 * An authentication token for a {@link User}.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter
@RedisHash(value = "auth_token", timeToLive = 30 * 24 * 60 * 60) // Expire in 30 days (days, hours, mins, secs)
public final class AuthToken {
    /**
     * The ID of this token.
     */
    @Id @JsonIgnore @NonNull private final UUID id;

    /**
     * The snowflake of the user this token is for.
     */
    @JsonIgnore private final long userSnowflake;

    /**
     * The access token for the user.
     */
    @Indexed @NonNull private final String accessToken;

    /**
     * The refresh token for the user.
     */
    @Indexed @NonNull private final String refreshToken;

    /**
     * The unix timestamp of when this token expires.
     */
    private final long expires;
}
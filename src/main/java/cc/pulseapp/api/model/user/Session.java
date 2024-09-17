package cc.pulseapp.api.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

/**
 * A session for a {@link User}.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter
@RedisHash(value = "sessions", timeToLive = 30 * 24 * 60 * 60) // Expire in 30 days (days, hours, mins, secs)
public final class Session {
    /**
     * The snowflake of this session.
     */
    @Id @JsonIgnore private final long snowflake;

    /**
     * The snowflake of the user this session is for.
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
     * The IP address of the user that created this session.
     */
    @NonNull @JsonIgnore private final String ipAddress;

    /**
     * The user agent of the user that created this session.
     */
    @NonNull @JsonIgnore private final String userAgent;

    /**
     * The unix timestamp of when this token expires.
     */
    private final long expires;
}
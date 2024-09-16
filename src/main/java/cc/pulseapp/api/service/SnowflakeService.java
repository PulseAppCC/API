package cc.pulseapp.api.service;

import com.relops.snowflake.Snowflake;
import lombok.NonNull;
import org.springframework.stereotype.Service;

/**
 * The service responsible for generating snowflakes!
 *
 * @author Braydon
 */
@Service
public final class SnowflakeService {
    private static final long TIMESTAMP_SHIFT = Snowflake.NODE_SHIFT + Snowflake.SEQ_SHIFT;

    /**
     * The snowflake instance to use.
     */
    @NonNull private final Snowflake snowflake;

    private SnowflakeService() {
        snowflake = new Snowflake(7);
    }

    /**
     * Generate a new snowflake.
     *
     * @return the generated snowflake
     */
    public long generateSnowflake() {
        return snowflake.next();
    }

    /**
     * Extract the creation time of the given
     * snowflake by reversing the snowflake algorithm.
     * <p>
     * The snowflake is right-shifted by 22
     * bits to get the original timestamp.
     * </p>
     *
     * @param snowflake the snowflake
     * @return the snowflake unix creation time
     */
    public long extractCreationTime(long snowflake) {
        return snowflake >>> TIMESTAMP_SHIFT;
    }
}
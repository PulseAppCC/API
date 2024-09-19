package cc.pulseapp.api.model.user.session;

import cc.pulseapp.api.common.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;

/**
 * The location a {@link Session} originated from.
 *
 * @author Braydon
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE) @Getter @ToString
public final class SessionLocation {
    /**
     * The IP address that created the session.
     */
    @NonNull private final String ip;

    /**
     * The country of the person that
     * created the session, if known.
     */
    private final String country;

    /**
     * The region of the person that
     * created the session, if known.
     */
    private final String region;

    /**
     * The city of the person that
     * created the session, if known.
     */
    private final String city;

    /**
     * The user agent of the person
     * that created the session.
     */
    @NonNull private final String userAgent;

    /**
     * Build a location from the given request.
     *
     * @param request the request to build from
     * @return the session location
     */
    @NonNull
    public static SessionLocation buildFromRequest(@NonNull HttpServletRequest request) {
        return new SessionLocation(
                RequestUtils.getRealIp(request), request.getHeader("CF-IPCountry"),
                request.getHeader("CF-Region"), request.getHeader("CF-IPCity"),
                RequestUtils.getUserAgent(request)
        );
    }
}
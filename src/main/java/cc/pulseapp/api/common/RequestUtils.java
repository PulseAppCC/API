package cc.pulseapp.api.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;

/**
 * @author Braydon
 */
@UtilityClass
public final class RequestUtils {
    private static final String[] IP_HEADERS = new String[] {
            "CF-Connecting-IP",
            "X-Forwarded-For"
    };

    /**
     * Get the real IP from the given request.
     *
     * @param request the request
     * @return the real IP
     */
    @NonNull
    public static String getRealIp(@NonNull HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        for (String headerName : IP_HEADERS) {
            String header = request.getHeader(headerName);
            if (header == null) {
                continue;
            }
            if (!header.contains(",")) { // Handle single IP
                ip = header;
                break;
            }
            // Handle multiple IPs
            String[] ips = header.split(",");
            for (String ipHeader : ips) {
                ip = ipHeader;
                break;
            }
        }
        return ip;
    }

    /**
     * Get the user agent from the given request.
     *
     * @param request the request to get from
     * @return the user agent
     */
    @NonNull
    public static String getUserAgent(@NonNull HttpServletRequest request) {
        return request.getHeader(HttpHeaders.USER_AGENT);
    }
}

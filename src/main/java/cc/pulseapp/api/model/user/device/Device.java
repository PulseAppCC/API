package cc.pulseapp.api.model.user.device;

import cc.pulseapp.api.model.user.User;
import cc.pulseapp.api.model.user.session.Session;
import cc.pulseapp.api.model.user.session.SessionLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Date;

/**
 * A device logged into a
 * {@link User}'s account.
 *
 * @author Braydon
 */
@AllArgsConstructor @Getter @ToString
public final class Device {
    /**
     * The type of this device.
     */
    @NonNull private final DeviceType type;

    /**
     * The browser type of this device.
     */
    @NonNull private final BrowserType browserType;

    /**
     * The IP address of this device.
     */
    @NonNull private final String ip;

    /**
     * The location of this device, if known.
     */
    private final String location;

    /**
     * The user agent of this device.
     */
    @NonNull private final String userAgent;

    /**
     * The session snowflake associated with this device.
     */
    private final long sessionSnowflake;

    /**
     * The date this device first logged into the account.
     */
    private final Date firstLogin;

    /**
     * Construct a device from a session.
     *
     * @param session     the session
     * @param deviceType  the device type
     * @param browserType the device browser type
     * @param firstLogin  the sessions first login time
     * @return the constructed device
     */
    @NonNull
    public static Device fromSession(@NonNull Session session, @NonNull DeviceType deviceType,
                                     @NonNull BrowserType browserType, @NonNull Date firstLogin) {
        SessionLocation rawLocation = session.getLocation();
        String location = rawLocation.getCountry() == null ? null
                : rawLocation.getCity() + ", " + rawLocation.getRegion() + ", " + rawLocation.getCountry();
        return new Device(deviceType, browserType, rawLocation.getIp(), location,
                rawLocation.getUserAgent(), session.getSnowflake(), firstLogin);
    }
}
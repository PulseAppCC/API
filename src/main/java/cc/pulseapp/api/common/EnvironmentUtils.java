package cc.pulseapp.api.common;

import lombok.Getter;
import lombok.experimental.UtilityClass;

/**
 * @author Braydon
 */
@UtilityClass
public final class EnvironmentUtils {
    /**
     * Is the app running in a production environment?
     */
    @Getter private static final boolean production;

    /**
     * Is the app running in a "cloud" environment?
     */
    @Getter private static final boolean cloud;

    static {
        String appEnv = System.getenv("APP_ENV");
        String cloudEnv = System.getenv("APP_CLOUD");
        production = appEnv != null && (appEnv.equals("production"));
        cloud = cloudEnv != null && (cloudEnv.equals("true"));
    }
}
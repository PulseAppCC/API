package cc.pulseapp.api.model.page;

/**
 * The theme of a {@link StatusPage}.
 *
 * @author Braydon
 */
public enum StatusPageTheme {
    /**
     * The theme is automatically chosen based on the user's OS.
     */
    AUTO,

    /**
     * The theme is forced to be dark.
     */
    DARK,

    /**
     * The theme is forced to be light.
     */
    LIGHT
}
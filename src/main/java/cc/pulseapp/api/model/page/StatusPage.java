package cc.pulseapp.api.model.page;

import cc.pulseapp.api.model.org.Organization;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Braydon
 */
@AllArgsConstructor @Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true) @ToString
@Document("pages")
public final class StatusPage {
    /**
     * The snowflake id of this status page.
     */
    @Id @EqualsAndHashCode.Include private final long snowflake;

    /**
     * The name of this status page.
     */
    @Indexed @NonNull private final String name;

    /**
     * The description of this status page, if any.
     */
    private final String description;

    /**
     * The slug of this status page.
     */
    @NonNull private final String slug;

    /**
     * The hash to the logo of this status page, if any.
     */
    private final String logo;

    /**
     * The hash to the banner of this status page, if any.
     */
    private final String banner;

    /**
     * The theme of this status page.
     */
    @NonNull private final StatusPageTheme theme;

    /**
     * Whether this status page is visible in search engines.
     */
    private final boolean visibleInSearchEngines;

    /**
     * The snowflake of the {@link Organization}
     * that owns this status page.
     */
    private final long orgSnowflake;
}
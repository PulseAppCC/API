package cc.pulseapp.api.service;

import cc.pulseapp.api.common.EnvironmentUtils;
import cc.pulseapp.api.exception.impl.BadRequestException;
import cc.pulseapp.api.model.Feature;
import cc.pulseapp.api.model.IGenericResponse;
import cc.pulseapp.api.model.org.Organization;
import cc.pulseapp.api.model.page.StatusPage;
import cc.pulseapp.api.model.page.StatusPageTheme;
import cc.pulseapp.api.repository.StatusPageRepository;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Braydon
 */
@Service
public final class StatusPageService {
    /**
     * The service to use for snowflake generation.
     */
    @NonNull private final SnowflakeService snowflakeService;

    /**
     * The repository to store and retrieve status pages.
     */
    @NonNull private final StatusPageRepository pageRepository;

    @Autowired
    public StatusPageService(@NonNull SnowflakeService snowflakeService, @NonNull StatusPageRepository pageRepository) {
        this.snowflakeService = snowflakeService;
        this.pageRepository = pageRepository;
    }

    /**
     * Create a new status page.
     *
     * @param name  the status page name
     * @param owner the owner of the status page
     * @return the created status page
     * @throws BadRequestException if the status page creation fails
     */
    @NonNull
    public StatusPage createStatusPage(@Nonnull String name, @NonNull Organization owner) throws BadRequestException {
        // Ensure status page creation is enabled
        if (!Feature.STATUS_PAGE_CREATION_ENABLED.isEnabled()) {
            throw new BadRequestException(Error.STATUS_PAGE_CREATION_DISABLED);
        }
        // Ensure the status page name isn't taken
        if (pageRepository.findByNameIgnoreCase(name) != null) {
            throw new BadRequestException(Error.STATUS_PAGE_NAME_TAKEN);
        }
        // Handle cloud environment checks
        if (EnvironmentUtils.isCloud()) {
            // TODO: do UserTier#maxStatusPages check
        }
        // Create the status page and return it
        String slug = name.replace(" ", "-") +
                "-" + ThreadLocalRandom.current().nextInt(10000, 99999);
        return pageRepository.save(new StatusPage(
                snowflakeService.generateSnowflake(), name, slug, null, null,
                null, StatusPageTheme.AUTO, true, owner.getSnowflake())
        );
    }

    /**
     * Organization errors.
     */
    private enum Error implements IGenericResponse {
        STATUS_PAGE_CREATION_DISABLED,
        STATUS_PAGE_NAME_TAKEN
    }
}
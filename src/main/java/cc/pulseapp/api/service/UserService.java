package cc.pulseapp.api.service;

import cc.pulseapp.api.common.StringUtils;
import cc.pulseapp.api.exception.impl.BadRequestException;
import cc.pulseapp.api.model.IGenericResponse;
import cc.pulseapp.api.model.org.Organization;
import cc.pulseapp.api.model.user.User;
import cc.pulseapp.api.model.user.UserDTO;
import cc.pulseapp.api.model.user.UserFlag;
import cc.pulseapp.api.model.user.input.CompleteOnboardingInput;
import cc.pulseapp.api.repository.UserRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Braydon
 */
@Service
public final class UserService {
    /**
     * The auth service to use.
     */
    @NonNull private final AuthService authService;

    /**
     * The service to use for snowflake timestamp extraction.
     */
    @NonNull private final SnowflakeService snowflakeService;

    /**
     * The organization service to use.
     */
    @NonNull private final OrganizationService orgService;

    /**
     * The status page service to use.
     */
    @NonNull private final StatusPageService statusPageService;

    /**
     * The user repository to use.
     */
    @NonNull private final UserRepository userRepository;

    @Autowired
    public UserService(@NonNull AuthService authService, @NonNull SnowflakeService snowflakeService,
                       @NonNull OrganizationService orgService, @NonNull StatusPageService statusPageService,
                       @NonNull UserRepository userRepository) {
        this.authService = authService;
        this.snowflakeService = snowflakeService;
        this.orgService = orgService;
        this.statusPageService = statusPageService;
        this.userRepository = userRepository;
    }

    /**
     * Get the currently authenticated user.
     *
     * @return the authenticated user
     */
    @NonNull
    public UserDTO getUser() {
        User user = authService.getAuthenticatedUser();
        return UserDTO.asDTO(user, new Date(snowflakeService.extractCreationTime(user.getSnowflake())));
    }

    /**
     * Check if the user with the given email exists.
     *
     * @param email the email to check
     * @return whether the user exists
     */
    public boolean doesUserExist(@NonNull String email) {
        return StringUtils.isValidEmail(email) && userRepository.findByEmailIgnoreCase(email) != null;
    }

    /**
     * Complete the onboarding
     * process for a user.
     *
     * @param input the input to process
     * @throws BadRequestException if onboarding fails
     */
    public void completeOnboarding(CompleteOnboardingInput input) throws BadRequestException {
        // Ensure the input was provided
        if (input == null || (!input.isValid())) {
            throw new BadRequestException(Error.MALFORMED_ONBOARDING_INPUT);
        }
        // Ensure the org slug is valid
        if (!StringUtils.isValidOrgSlug(input.getOrganizationSlug())) {
            throw new BadRequestException(Error.ORGANIZATION_SLUG_INVALID);
        }
        User user = authService.getAuthenticatedUser();
        if (user.hasFlag(UserFlag.COMPLETED_ONBOARDING)) { // Already completed
            throw new BadRequestException(Error.ALREADY_ONBOARDED);
        }
        Organization org = orgService.createOrganization(input.getOrganizationName(), input.getOrganizationSlug(), user); // Create the org
        statusPageService.createStatusPage(input.getStatusPageName(), org); // Create the status page
        user.addFlag(UserFlag.COMPLETED_ONBOARDING); // Flag completed onboarding
        userRepository.save(user);
    }

    /**
     * User errors.
     */
    private enum Error implements IGenericResponse {
        MALFORMED_ONBOARDING_INPUT,
        ORGANIZATION_SLUG_INVALID,
        ALREADY_ONBOARDED,
    }
}
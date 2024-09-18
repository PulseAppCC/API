package cc.pulseapp.api.service;

import cc.pulseapp.api.common.StringUtils;
import cc.pulseapp.api.exception.impl.BadRequestException;
import cc.pulseapp.api.model.IGenericResponse;
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
     * The user repository to use.
     */
    @NonNull private final UserRepository userRepository;

    @Autowired
    public UserService(@NonNull AuthService authService, @NonNull SnowflakeService snowflakeService,
                       @NonNull OrganizationService orgService, @NonNull UserRepository userRepository) {
        this.authService = authService;
        this.snowflakeService = snowflakeService;
        this.orgService = orgService;
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

    public void completeOnboarding(CompleteOnboardingInput input) {
        // Ensure the input was provided
        if (input == null || (!input.isValid())) {
            throw new BadRequestException(Error.MALFORMED_ONBOARDING_INPUT);
        }
        User user = authService.getAuthenticatedUser();
        if (user.hasFlag(UserFlag.COMPLETED_ONBOARDING)) { // Already completed
            throw new BadRequestException(Error.ALREADY_ONBOARDED);
        }
        orgService.createOrganization(input.getOrganizationName(), user); // Create the org
        user.addFlag(UserFlag.COMPLETED_ONBOARDING); // Flag completed onboarding
        userRepository.save(user);
    }

    /**
     * User errors.
     */
    private enum Error implements IGenericResponse {
        MALFORMED_ONBOARDING_INPUT,
        ALREADY_ONBOARDED,
    }
}
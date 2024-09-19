package cc.pulseapp.api.service;

import cc.pulseapp.api.common.HashUtils;
import cc.pulseapp.api.common.StringUtils;
import cc.pulseapp.api.exception.impl.BadRequestException;
import cc.pulseapp.api.model.IGenericResponse;
import cc.pulseapp.api.model.org.Organization;
import cc.pulseapp.api.model.user.TFAProfile;
import cc.pulseapp.api.model.user.User;
import cc.pulseapp.api.model.user.UserDTO;
import cc.pulseapp.api.model.user.UserFlag;
import cc.pulseapp.api.model.user.input.CompleteOnboardingInput;
import cc.pulseapp.api.model.user.input.EnableTFAInput;
import cc.pulseapp.api.model.user.response.UserSetupTFAResponse;
import cc.pulseapp.api.repository.SessionRepository;
import cc.pulseapp.api.repository.UserRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
     * Thw two-factor auth service to use.
     */
    @NonNull private final TFAService tfaService;

    /**
     * The user repository to use.
     */
    @NonNull private final UserRepository userRepository;

    /**
     * The session repository to use.
     */
    @NonNull private final SessionRepository sessionRepository;

    /**
     * A map of users who are setting up two-factor auth.
     * <p>
     * The key kis the user's snowflake
     * and the value is the secret.
     * </p>
     */
    private final Cache<Long, String> settingUpTfa = Caffeine.newBuilder()
            .expireAfterWrite(5L, TimeUnit.MINUTES)
            .build();

    @Autowired
    public UserService(@NonNull AuthService authService, @NonNull SnowflakeService snowflakeService,
                       @NonNull OrganizationService orgService, @NonNull StatusPageService statusPageService,
                       @NonNull TFAService tfaService, @NonNull UserRepository userRepository,
                       @NonNull SessionRepository sessionRepository) {
        this.authService = authService;
        this.snowflakeService = snowflakeService;
        this.orgService = orgService;
        this.statusPageService = statusPageService;
        this.tfaService = tfaService;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
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
     * Start setting up TFA for a user.
     *
     * @return the setup response
     * @throws BadRequestException if the setup fails
     */
    @NonNull
    public UserSetupTFAResponse setupTwoFactor() throws BadRequestException {
        User user = authService.getAuthenticatedUser();
        if (user.hasFlag(UserFlag.TFA_ENABLED)) { // Ensure TFA isn't already on
            throw new BadRequestException(Error.TFA_ALREADY_ENABLED);
        }
        String secret = tfaService.generateSecretKey();
        settingUpTfa.put(user.getSnowflake(), secret); // Store temporarily
        return new UserSetupTFAResponse(secret, tfaService.generateQrCodeUrl(user.getUsername(), secret));
    }

    @NonNull
    public List<String> enableTwoFactor(EnableTFAInput input) throws BadRequestException {
        if (input == null || (!input.isValid())) { // Ensure the input was provided
            throw new BadRequestException(Error.MALFORMED_ENABLE_TFA_INPUT);
        }
        User user = authService.getAuthenticatedUser();
        if (user.hasFlag(UserFlag.TFA_ENABLED)) { // Ensure TFA isn't already on
            throw new BadRequestException(Error.TFA_ALREADY_ENABLED);
        }
        String secret = settingUpTfa.getIfPresent(user.getSnowflake()); // Get the setup TFA secret
        if (secret == null) { // No secret, creation session timed out
            throw new BadRequestException(Error.TFA_SETUP_TIMED_OUT);
        }
        if (!secret.equals(input.getSecret())) { // Ensure the original and received secrets are the same
            throw new BadRequestException(Error.TFA_SETUP_SECRET_MISMATCH);
        }
        if (!tfaService.getPin(secret).equals(input.getPin())) { // Ensure the pin is valid
            throw new BadRequestException(Error.TFA_SETUP_PIN_INVALID);
        }
        // Enable TFA for the user
        byte[] salt = HashUtils.generateSalt();
        List<String> originalBackupCodes = new ArrayList<>();
        for (int i = 0; i < 8; i++) { // Generate 8 backup codes
            originalBackupCodes.add(StringUtils.generateRandom(6, false, true, false));
        }

        // Encrypt the stored backup codes
        List<String> storedBackupCodes = originalBackupCodes.stream()
                .map(backupCode -> HashUtils.hash(salt, backupCode))
                .toList();
        user.setTfa(new TFAProfile(secret, Base64.getEncoder().encodeToString(salt), storedBackupCodes));
        user.addFlag(UserFlag.TFA_ENABLED);
        userRepository.save(user);

        // And finally invalidate all of the sessions for the user
        sessionRepository.deleteAll(sessionRepository.findAllByUserSnowflake(user.getSnowflake()));

        return originalBackupCodes;
    }

    /**
     * User errors.
     */
    private enum Error implements IGenericResponse {
        MALFORMED_ONBOARDING_INPUT,
        MALFORMED_ENABLE_TFA_INPUT,
        ORGANIZATION_SLUG_INVALID,
        ALREADY_ONBOARDED,
        TFA_ALREADY_ENABLED,
        TFA_SETUP_TIMED_OUT,
        TFA_SETUP_SECRET_MISMATCH,
        TFA_SETUP_PIN_INVALID,
    }
}
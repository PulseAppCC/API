package cc.pulseapp.api.service;

import cc.pulseapp.api.common.HashUtils;
import cc.pulseapp.api.common.StringUtils;
import cc.pulseapp.api.common.Tuple;
import cc.pulseapp.api.exception.impl.BadRequestException;
import cc.pulseapp.api.model.IGenericResponse;
import cc.pulseapp.api.model.org.Organization;
import cc.pulseapp.api.model.user.TFAProfile;
import cc.pulseapp.api.model.user.User;
import cc.pulseapp.api.model.user.UserDTO;
import cc.pulseapp.api.model.user.UserFlag;
import cc.pulseapp.api.model.user.device.BrowserType;
import cc.pulseapp.api.model.user.device.Device;
import cc.pulseapp.api.model.user.device.DeviceType;
import cc.pulseapp.api.model.user.input.CompleteOnboardingInput;
import cc.pulseapp.api.model.user.input.DisableTFAInput;
import cc.pulseapp.api.model.user.input.EnableTFAInput;
import cc.pulseapp.api.model.user.input.UserExistsInput;
import cc.pulseapp.api.model.user.response.UserSetupTFAResponse;
import cc.pulseapp.api.model.user.session.Session;
import cc.pulseapp.api.repository.SessionRepository;
import cc.pulseapp.api.repository.UserRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.NonNull;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.commons.lang3.EnumUtils;
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
    private static final UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzer
            .newBuilder()
            .useJava8CompatibleCaching()
            .withCache(10000)
            .build();

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
     * Check if a user exists
     * with the given email.
     *
     * @param input the input to check
     * @return whether the user exists
     */
    public boolean doesUserExist(UserExistsInput input) {
        if (input == null || (!input.isValid())) { // Ensure the input was provided
            throw new BadRequestException(Error.MALFORMED_USER_EXISTS_INPUT);
        }
        return StringUtils.isValidEmail(input.getEmail()) && userRepository.findByEmailIgnoreCase(input.getEmail()) != null;
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
     * Start setting up TFA for the
     * currently authenticated user.
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

    /**
     * Enable two-factor auth for the
     * currently authenticated user.
     *
     * @param input the input to process
     * @return the raw backup codes
     * @throws BadRequestException if enabling fails
     */
    @NonNull
    public List<String> enableTwoFactor(EnableTFAInput input) throws BadRequestException {
        if (input == null || (!input.isValid())) { // Ensure the input was provided
            throw new BadRequestException(Error.MALFORMED_ENABLE_TFA_INPUT);
        }
        Tuple<Session, User> sessionAndUser = authService.getSessionAndUser();
        Session session = sessionAndUser.getLeft();
        User user = sessionAndUser.getRight();
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
            throw new BadRequestException(Error.TFA_PIN_INVALID);
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
        List<Session> sessions = sessionRepository.findAllByUserSnowflake(user.getSnowflake());
        sessions.removeIf(activeSession -> activeSession.equals(session));
        sessionRepository.deleteAll(sessions);

        return originalBackupCodes;
    }

    /**
     * Disable two-factor auth for the
     * currently authenticated user.
     *
     * @param input the input to process
     * @throws BadRequestException if disabling fails
     */
    public void disableTwoFactor(DisableTFAInput input) throws BadRequestException {
        if (input == null || (!input.isValid())) { // Ensure the input was provided
            throw new BadRequestException(Error.MALFORMED_DISABLE_TFA_INPUT);
        }
        User user = authService.getAuthenticatedUser();
        if (!user.hasFlag(UserFlag.TFA_ENABLED)) { // Ensure TFA is already on
            throw new BadRequestException(Error.TFA_NOT_ENABLED);
        }
        authService.useTfaPin(user, input.getPin()); // Ensure the pin is valid

        // Disable TFA for the user
        user.setTfa(null);
        user.removeFlag(UserFlag.TFA_ENABLED);
        userRepository.save(user);
    }

    /**
     * Get the devices logged into
     * the authenticated user.
     *
     * @return the devices
     */
    @NonNull
    public List<Device> getDevices() {
        List<Device> devices = new ArrayList<>();
        User user = authService.getAuthenticatedUser();
        for (Session session : sessionRepository.findAllByUserSnowflake(user.getSnowflake())) {
            UserAgent.ImmutableUserAgent userAgent = userAgentAnalyzer.parse(session.getLocation().getUserAgent());
            DeviceType deviceType = EnumUtils.getEnum(DeviceType.class, userAgent.get("DeviceClass").getValue().toUpperCase());
            BrowserType browserType = EnumUtils.getEnum(BrowserType.class, userAgent.get("AgentName").getValue().toUpperCase());
            if (deviceType == null) {
                deviceType = DeviceType.UNKNOWN;
            }
            if (browserType == null) {
                browserType = BrowserType.UNKNOWN;
            }
            devices.add(Device.fromSession(session, deviceType, browserType, new Date(snowflakeService.extractCreationTime(session.getSnowflake()))));
        }
        return devices;
    }

    /**
     * Logout the user.
     */
    public void logout() {
        sessionRepository.delete(authService.getSessionAndUser().getLeft());
    }

    /**
     * User errors.
     */
    private enum Error implements IGenericResponse {
        MALFORMED_USER_EXISTS_INPUT,
        MALFORMED_ONBOARDING_INPUT,
        MALFORMED_ENABLE_TFA_INPUT,
        MALFORMED_DISABLE_TFA_INPUT,
        ORGANIZATION_SLUG_INVALID,
        ALREADY_ONBOARDED,
        TFA_ALREADY_ENABLED,
        TFA_NOT_ENABLED,
        TFA_SETUP_TIMED_OUT,
        TFA_SETUP_SECRET_MISMATCH,
        TFA_PIN_INVALID,
    }
}
package cc.pulseapp.api.service;

import cc.pulseapp.api.common.HashUtils;
import cc.pulseapp.api.common.StringUtils;
import cc.pulseapp.api.common.Tuple;
import cc.pulseapp.api.exception.impl.BadRequestException;
import cc.pulseapp.api.exception.impl.ResourceNotFoundException;
import cc.pulseapp.api.model.Feature;
import cc.pulseapp.api.model.IGenericResponse;
import cc.pulseapp.api.model.user.User;
import cc.pulseapp.api.model.user.UserDTO;
import cc.pulseapp.api.model.user.UserFlag;
import cc.pulseapp.api.model.user.UserTier;
import cc.pulseapp.api.model.user.input.UserLoginInput;
import cc.pulseapp.api.model.user.input.UserRegistrationInput;
import cc.pulseapp.api.model.user.response.UserAuthResponse;
import cc.pulseapp.api.model.user.session.Session;
import cc.pulseapp.api.model.user.session.SessionLocation;
import cc.pulseapp.api.repository.SessionRepository;
import cc.pulseapp.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Braydon
 */
@Service
public final class AuthService {
    /**
     * The service to use for captcha validation.
     */
    @NonNull private final CaptchaService captchaService;

    /**
     * The service to use for snowflake generation and timestamp extraction.
     */
    @NonNull private final SnowflakeService snowflakeService;

    /**
     * Thw two-factor auth service to use.
     */
    @NonNull private final TFAService tfaService;

    /**
     * The repository to store and retrieve users.
     */
    @NonNull private final UserRepository userRepository;

    /**
     * The auth token repository for generating and validating auth tokens.
     */
    @NonNull private final SessionRepository sessionRepository;

    @Autowired
    public AuthService(@NonNull CaptchaService captchaService, @NonNull SnowflakeService snowflakeService,
                       @NonNull TFAService tfaService, @NonNull UserRepository userRepository,
                       @NonNull SessionRepository sessionRepository) {
        this.captchaService = captchaService;
        this.snowflakeService = snowflakeService;
        this.tfaService = tfaService;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Register a new user.
     *
     * @param request the http request
     * @param input   the registration input
     * @return the user auth response
     * @throws BadRequestException if the input has an error
     */
    @NonNull
    public UserAuthResponse registerUser(@NonNull HttpServletRequest request, UserRegistrationInput input) throws BadRequestException {
        // Ensure user registration is enabled
        if (!Feature.USER_REGISTRATION_ENABLED.isEnabled()) {
            throw new BadRequestException(Error.REGISTRATION_DISABLED);
        }
        validateRegistrationInput(input); // Ensure the input is valid

        // Ensure the given email hasn't been used before
        if (userRepository.findByEmailIgnoreCase(input.getEmail()) != null) {
            throw new BadRequestException(Error.EMAIL_ALREADY_USED);
        }

        // Create the user and return it
        byte[] salt = HashUtils.generateSalt();
        Date now = new Date();
        User user = userRepository.save(new User(
                snowflakeService.generateSnowflake(), input.getEmail(), input.getUsername().toLowerCase(),
                HashUtils.hash(salt, input.getPassword()), Base64.getEncoder().encodeToString(salt),
                null, UserTier.FREE, null, 0, now
        ));
        return new UserAuthResponse(generateSession(request, user), UserDTO.asDTO(user, now));
    }

    /**
     * Login a user.
     *
     * @param request the http request
     * @param input   the login input
     * @return the user auth response
     * @throws BadRequestException if the input has an error
     */
    @NonNull
    public UserAuthResponse loginUser(@NonNull HttpServletRequest request, UserLoginInput input) throws BadRequestException {
        validateLoginInput(input); // Ensure the input is valid

        // Lookup the user by the email or username and ensure the user exists
        User user = userRepository.findByEmailIgnoreCase(input.getEmail());
        if (user == null) {
            throw new BadRequestException(Error.USER_NOT_FOUND);
        }
        // Ensure the password matches
        if (!HashUtils.hash(Base64.getDecoder().decode(user.getPasswordSalt()), input.getPassword()).equals(user.getPassword())) {
            throw new BadRequestException(Error.PASSWORDS_DO_NOT_MATCH);
        }
        // Handle the two-factor auth pin if the user has it enabled
        if (user.hasFlag(UserFlag.TFA_ENABLED)) {
            String pin = input.getTfaPin();
            if (pin == null || (pin.isBlank())) { // No TFA pin received
                throw new BadRequestException(Error.BORDER_CROSSING);
            }
            useTfaPin(user, pin); // Attempt to use the pin
        }
        user.setLastLogin(new Date());
        user = userRepository.save(user);
        return new UserAuthResponse(generateSession(request, user),
                UserDTO.asDTO(user, new Date(snowflakeService.extractCreationTime(user.getSnowflake()))));
    }

    /**
     * Use a TFA pin for a user.
     *
     * @param user the user to use TFA for
     * @param pin  the pin to use
     * @throws BadRequestException if using TFA fails
     */
    public void useTfaPin(@NonNull User user, @NonNull String pin) throws BadRequestException {
        if (pin.length() != 6) { // Ensure the pin is the correct length
            throw new BadRequestException(Error.TFA_PIN_INVALID);
        }
        if (!user.hasFlag(UserFlag.TFA_ENABLED)) { // Ensure TFA is already on
            throw new BadRequestException(Error.TFA_NOT_ENABLED);
        }
        String encryptedPin = HashUtils.hash(Base64.getDecoder().decode(user.getTfa().getBackupCodesSalt()), pin);

        // Before checking the pin, check the user's backup codes
        for (String backupCode : user.getTfa().getBackupCodes()) {
            if (!encryptedPin.equals(backupCode)) {
                continue;
            }
            // The code is a valid backup code, remove it from the user's list
            user.getTfa().getBackupCodes().remove(backupCode);
            userRepository.save(user);
            return;
        }

        // Check if the TFA pin is valid
        if (!tfaService.getPin(user.getTfa().getSecret()).equals(pin)) {
            throw new BadRequestException(Error.TFA_PIN_INVALID);
        }
    }

    /**
     * Get the authenticated user.
     *
     * @return the authenticated user
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    @NonNull
    public User getAuthenticatedUser() throws ResourceNotFoundException {
        return getSessionAndUser().getRight();
    }

    /**
     * Get the authenticated session and associated user.
     *
     * @return the authenticated session and user
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    @NonNull
    public Tuple<Session, User> getSessionAndUser() throws ResourceNotFoundException {
        Session session = (Session) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        return new Tuple<>(session, getUserFromSnowflake(session.getUserSnowflake()));
    }

    /**
     * Get a user from a snowflake, if the user exists.
     *
     * @param snowflake the snowflake of the user
     * @return the user from the snowflake
     * @throws BadRequestException       if the snowflake is invalid
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    @NonNull
    public User getUserFromSnowflake(long snowflake) {
        if (snowflake < 1L) {
            throw new ResourceNotFoundException(Error.USER_NOT_FOUND);
        }
        User user = userRepository.findById(snowflake).orElse(null);
        if (user == null) {
            throw new ResourceNotFoundException(Error.USER_NOT_FOUND);
        }
        return user;
    }

    /**
     * Generate an auth token for a user.
     *
     * @param request the http request
     * @param user    the user to generate for
     * @return the generated auth token
     * @throws BadRequestException if the user is disabled
     */
    @NonNull
    private Session generateSession(@NonNull HttpServletRequest request, @NonNull User user) throws BadRequestException {
        // User's account has been disabled
        if (user.hasFlag(UserFlag.DISABLED)) {
            throw new BadRequestException(Error.USER_DISABLED);
        }
        return sessionRepository.save(new Session(
                snowflakeService.generateSnowflake(), user.getSnowflake(),
                StringUtils.generateRandom(128, true, true, false),
                StringUtils.generateRandom(128, true, true, false),
                SessionLocation.buildFromRequest(request),
                System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30L)
        ));
    }

    /**
     * Validate the given registration input.
     *
     * @param input the registration input
     * @throws BadRequestException if the input has an error
     */
    private void validateRegistrationInput(UserRegistrationInput input) throws BadRequestException {
        if (input == null || (!input.isValid())) { // Ensure the input was provided
            throw new BadRequestException(Error.MALFORMED_REGISTRATION_INPUT);
        }
        if (!StringUtils.isValidEmail(input.getEmail())) { // Ensure the email is valid
            throw new BadRequestException(Error.EMAIL_INVALID);
        }
        if (!StringUtils.isValidUsername(input.getUsername())) { // Ensure the username is valid
            throw new BadRequestException(Error.USERNAME_INVALID);
        }
        if (!input.getPassword().equals(input.getPasswordConfirmation())) { // Ensure passwords match
            throw new BadRequestException(Error.PASSWORDS_DO_NOT_MATCH);
        }
        // The password must meet the requirements
        StringUtils.PasswordError passwordError = StringUtils.checkPasswordRequirements(input.getPassword());
        if (passwordError != null) {
            throw new BadRequestException(passwordError);
        }
        // Finally validate the captcha
        captchaService.validateCaptcha(input.getCaptchaResponse());
    }

    /**
     * Validate the given login input.
     *
     * @param input the login input
     * @throws BadRequestException if the input has an error
     */
    private void validateLoginInput(UserLoginInput input) throws BadRequestException {
        if (input == null || (!input.isValid())) { // Ensure the input was provided
            throw new BadRequestException(Error.MALFORMED_LOGIN_INPUT);
        }
        if (!StringUtils.isValidEmail(input.getEmail())) { // Ensure the email is valid
            throw new BadRequestException(Error.EMAIL_INVALID);
        }
        // Finally validate the captcha
        captchaService.validateCaptcha(input.getCaptchaResponse());
    }

    /**
     * Authentication errors.
     */
    private enum Error implements IGenericResponse {
        REGISTRATION_DISABLED,
        MALFORMED_REGISTRATION_INPUT,
        MALFORMED_LOGIN_INPUT,
        EMAIL_INVALID,
        USERNAME_INVALID,
        USER_NOT_FOUND,
        PASSWORDS_DO_NOT_MATCH,
        BORDER_CROSSING,
        TFA_NOT_ENABLED,
        TFA_PIN_INVALID,
        EMAIL_ALREADY_USED,
        USER_DISABLED
    }
}
package cc.pulseapp.api.service;

import cc.pulseapp.api.common.EnvironmentUtils;
import cc.pulseapp.api.common.HashUtils;
import cc.pulseapp.api.common.RequestUtils;
import cc.pulseapp.api.common.StringUtils;
import cc.pulseapp.api.exception.impl.BadRequestException;
import cc.pulseapp.api.exception.impl.ResourceNotFoundException;
import cc.pulseapp.api.model.IGenericResponse;
import cc.pulseapp.api.model.user.Session;
import cc.pulseapp.api.model.user.User;
import cc.pulseapp.api.model.user.UserFlag;
import cc.pulseapp.api.model.user.UserTier;
import cc.pulseapp.api.model.user.input.UserLoginInput;
import cc.pulseapp.api.model.user.input.UserRegistrationInput;
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
     * The repository to store and retrieve users.
     */
    @NonNull private final UserRepository userRepository;

    /**
     * The auth token repository for generating and validating auth tokens.
     */
    @NonNull private final SessionRepository sessionRepository;

    @Autowired
    public AuthService(@NonNull CaptchaService captchaService, @NonNull SnowflakeService snowflakeService,
                       @NonNull UserRepository userRepository, @NonNull SessionRepository sessionRepository) {
        this.captchaService = captchaService;
        this.snowflakeService = snowflakeService;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Register a new user.
     *
     * @param request the http request
     * @param input   the registration input
     * @return the registered user's auth token
     * @throws BadRequestException if the input has an error
     */
    @NonNull
    public Session registerUser(@NonNull HttpServletRequest request, UserRegistrationInput input) throws BadRequestException {
        validateRegistrationInput(input); // Ensure the input is valid

        // Ensure the given email hasn't been used before
        if (userRepository.findByEmailIgnoreCase(input.getEmail()) != null) {
            throw new BadRequestException(Error.EMAIL_ALREADY_USED);
        }

        // Create the user and return it
        byte[] salt = HashUtils.generateSalt();
        Date now = new Date();
        return generateSession(request, userRepository.save(new User(
                snowflakeService.generateSnowflake(), input.getEmail(), input.getUsername(),
                HashUtils.hash(salt, input.getPassword()), Base64.getEncoder().encodeToString(salt),
                null, UserTier.FREE, 0, now
        )));
    }

    /**
     * Login a user.
     *
     * @param request the http request
     * @param input   the login input
     * @return the logged in user's auth token
     * @throws BadRequestException if the input has an error
     */
    @NonNull
    public Session loginUser(@NonNull HttpServletRequest request, UserLoginInput input) throws BadRequestException {
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
        user.setLastLogin(new Date());
        return generateSession(request, userRepository.save(user));
    }

    /**
     * Get the authenticated user.
     *
     * @return the authenticated user
     * @throws ResourceNotFoundException if the user doesn't exist
     */
    @NonNull
    public User getAuthenticatedUser() throws ResourceNotFoundException {
        Session session = (Session) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        return getUserFromSnowflake(session.getUserSnowflake());
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
                RequestUtils.getRealIp(request), RequestUtils.getUserAgent(request),
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
        // Ensure the input was provided
        if (input == null || (!input.isValid())) {
            throw new BadRequestException(Error.MALFORMED_INPUT);
        }
        // Ensure the email is valid
        if (!StringUtils.isValidEmail(input.getEmail())) {
            throw new BadRequestException(Error.EMAIL_INVALID);
        }
        // Ensure the username is valid
        if (!StringUtils.isValidUsername(input.getUsername())) {
            throw new BadRequestException(Error.USERNAME_INVALID);
        }
        // Password and confirmed password must match
        if (!input.getPassword().equals(input.getPasswordConfirmation())) {
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
        // Ensure the input was provided
        if (input == null || (!input.isValid())) {
            throw new BadRequestException(Error.MALFORMED_INPUT);
        }
        // Ensure the email is valid
        if (input.getEmail() != null && (!StringUtils.isValidEmail(input.getEmail()))) {
            throw new BadRequestException(Error.EMAIL_INVALID);
        }
        // Finally validate the captcha
        captchaService.validateCaptcha(input.getCaptchaResponse());
    }

    private enum Error implements IGenericResponse {
        MALFORMED_INPUT,
        EMAIL_INVALID,
        USERNAME_INVALID,
        USER_NOT_FOUND,
        PASSWORDS_DO_NOT_MATCH,
        EMAIL_ALREADY_USED,
        USER_DISABLED
    }
}
package cc.pulseapp.api.service;

import cc.pulseapp.api.common.EnvironmentUtils;
import cc.pulseapp.api.common.HashUtils;
import cc.pulseapp.api.common.StringUtils;
import cc.pulseapp.api.exception.impl.BadRequestException;
import cc.pulseapp.api.model.IGenericResponse;
import cc.pulseapp.api.model.user.AuthToken;
import cc.pulseapp.api.model.user.User;
import cc.pulseapp.api.model.user.UserFlag;
import cc.pulseapp.api.model.user.UserTier;
import cc.pulseapp.api.model.user.input.UserRegistrationInput;
import cc.pulseapp.api.repository.AuthTokenRepository;
import cc.pulseapp.api.repository.UserRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;
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
    @NonNull private final AuthTokenRepository authTokenRepository;

    @Autowired
    public AuthService(@NonNull CaptchaService captchaService, @NonNull SnowflakeService snowflakeService,
                       @NonNull UserRepository userRepository, @NonNull AuthTokenRepository authTokenRepository) {
        this.captchaService = captchaService;
        this.snowflakeService = snowflakeService;
        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
    }

    /**
     * Register a new user.
     *
     * @param input the registration input
     * @return the registered user's auth token
     * @throws BadRequestException if the input has an error
     */
    @NonNull
    public AuthToken registerUser(UserRegistrationInput input) throws BadRequestException {
        validateRegistrationInput(input); // Ensure the input is valid

        // Ensure the given email hasn't been used before
        if (userRepository.findByEmailIgnoreCase(input.getEmail()) != null) {
            throw new BadRequestException(Error.EMAIL_ALREADY_USED);
        }

        // Create the user and return it
        byte[] salt = HashUtils.generateSalt();
        Date now = new Date();
        return generateAuthToken(userRepository.save(new User(
                snowflakeService.generateSnowflake(), input.getEmail(), input.getUsername(),
                HashUtils.hash(salt, input.getPassword()), Base64.getEncoder().encodeToString(salt),
                UserTier.FREE, 0, now
        )));
    }

    /**
     * Generate an auth token for a user.
     *
     * @param user the user to generate for
     * @return the generated auth token
     * @throws BadRequestException if the user is disabled
     */
    @NonNull
    private AuthToken generateAuthToken(@NonNull User user) throws BadRequestException {
        // User's account has been disabled
        if (user.hasFlag(UserFlag.DISABLED)) {
            throw new BadRequestException(Error.USER_DISABLED);
        }
        return authTokenRepository.save(new AuthToken(
                UUID.randomUUID(), user.getId(),
                StringUtils.generateRandom(128, true, true, false),
                StringUtils.generateRandom(128, true, true, false),
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
        if (EnvironmentUtils.isProduction()) {
            captchaService.validateCaptcha(input.getCaptchaResponse());
        }
    }

    private enum Error implements IGenericResponse {
        MALFORMED_INPUT,
        EMAIL_INVALID,
        USERNAME_INVALID,
        PASSWORDS_DO_NOT_MATCH,
        EMAIL_ALREADY_USED,
        USER_DISABLED
    }
}
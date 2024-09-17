package cc.pulseapp.api.service;

import cc.pulseapp.api.common.StringUtils;
import cc.pulseapp.api.model.user.User;
import cc.pulseapp.api.model.user.UserDTO;
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
     * The user repository to use.
     */
    @NonNull private final UserRepository userRepository;

    @Autowired
    public UserService(@NonNull AuthService authService, @NonNull SnowflakeService snowflakeService, @NonNull UserRepository userRepository) {
        this.authService = authService;
        this.snowflakeService = snowflakeService;
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
}
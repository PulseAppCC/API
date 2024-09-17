package cc.pulseapp.api.service;

import cc.pulseapp.api.model.user.User;
import cc.pulseapp.api.model.user.UserDTO;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Braydon
 */
@Service
public final class UserService {
    @NonNull private final AuthService authService;

    /**
     * The service to use for snowflake timestamp extraction.
     */
    @NonNull private final SnowflakeService snowflakeService;

    @Autowired
    public UserService(@NonNull AuthService authService, @NonNull SnowflakeService snowflakeService) {
        this.authService = authService;
        this.snowflakeService = snowflakeService;
    }

    @NonNull
    public UserDTO getUser() {
        User user = authService.getAuthenticatedUser();
        return UserDTO.asDTO(user, new Date(snowflakeService.extractCreationTime(user.getSnowflake())));
    }
}
package cc.pulseapp.api.controller.v1;

import cc.pulseapp.api.exception.impl.BadRequestException;
import cc.pulseapp.api.model.user.AuthToken;
import cc.pulseapp.api.model.user.input.UserRegistrationInput;
import cc.pulseapp.api.service.AuthService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller is responsible for
 * handling user authentication requests.
 *
 * @author Braydon
 */
@RestController
@RequestMapping(value = "/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public final class AuthController {
    /**
     * The user service to use.
     */
    @NonNull private final AuthService authService;

    @Autowired
    public AuthController(@NonNull AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register") @ResponseBody @NonNull
    public ResponseEntity<AuthToken> register(UserRegistrationInput input) throws BadRequestException {
        return ResponseEntity.ok(authService.registerUser(input));
    }
}
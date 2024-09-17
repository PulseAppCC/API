package cc.pulseapp.api.controller.v1;

import cc.pulseapp.api.exception.impl.BadRequestException;
import cc.pulseapp.api.model.user.Session;
import cc.pulseapp.api.model.user.input.UserLoginInput;
import cc.pulseapp.api.model.user.input.UserRegistrationInput;
import cc.pulseapp.api.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
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

    /**
     * A POST endpoint to register a new user.
     *
     * @param request the http request
     * @param input   the registration input
     * @return the session for the registered user
     * @throws BadRequestException if the registration fails
     */
    @PostMapping("/register") @ResponseBody @NonNull
    public ResponseEntity<Session> register(@NonNull HttpServletRequest request, UserRegistrationInput input) throws BadRequestException {
        return ResponseEntity.ok(authService.registerUser(request, input));
    }

    /**
     * A POST endpoint to login a user.
     *
     * @param request the http request
     * @param input   the login input
     * @return the session for the login user
     * @throws BadRequestException if the login fails
     */
    @PostMapping("/login") @ResponseBody @NonNull
    public ResponseEntity<Session> login(@NonNull HttpServletRequest request, UserLoginInput input) throws BadRequestException {
        return ResponseEntity.ok(authService.loginUser(request, input));
    }
}
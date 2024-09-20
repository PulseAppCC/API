package cc.pulseapp.api.controller.v1;

import cc.pulseapp.api.exception.impl.BadRequestException;
import cc.pulseapp.api.model.user.User;
import cc.pulseapp.api.model.user.UserDTO;
import cc.pulseapp.api.model.user.input.CompleteOnboardingInput;
import cc.pulseapp.api.model.user.input.DisableTFAInput;
import cc.pulseapp.api.model.user.input.EnableTFAInput;
import cc.pulseapp.api.model.user.input.UserExistsInput;
import cc.pulseapp.api.model.user.response.UserSetupTFAResponse;
import cc.pulseapp.api.service.UserService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * This controller is responsible for
 * handling {@link User} related requests.
 *
 * @author Braydon
 */
@RestController
@RequestMapping(value = "/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
public final class UserController {
    /**
     * The user service to use.
     */
    @NonNull private final UserService userService;

    @Autowired
    public UserController(@NonNull UserService userService) {
        this.userService = userService;
    }

    /**
     * A GET endpoint to get the
     * currently authenticated user.
     *
     * @return the currently authenticated user
     */
    @GetMapping("/@me") @ResponseBody @NonNull
    public ResponseEntity<UserDTO> getUser() {
        return ResponseEntity.ok(userService.getUser());
    }

    /**
     * A GET endpoint to check if a
     * user exists with the given email.
     *
     * @param input the input to check
     * @return the response
     */
    @PostMapping("/exists") @ResponseBody @NonNull
    public ResponseEntity<Map<String, Object>> doesUserExist(UserExistsInput input) {
        return ResponseEntity.ok(Map.of("exists", userService.doesUserExist(input)));
    }

    /**
     * A POST endpoint to complete
     * the onboarding process.
     *
     * @param input the completion input
     * @return the response
     * @throws BadRequestException if the completion fails
     */
    @PostMapping("/complete-onboarding") @ResponseBody @NonNull
    public ResponseEntity<Map<String, Object>> completeOnboarding(CompleteOnboardingInput input) throws BadRequestException {
        userService.completeOnboarding(input);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * A POST endpoint to start
     * setting up TFA for a user.
     *
     * @return the setup response
     * @throws BadRequestException if the setup fails
     */
    @PostMapping("/setup-tfa") @ResponseBody @NonNull
    public ResponseEntity<UserSetupTFAResponse> setupTwoFactor() throws BadRequestException {
        return ResponseEntity.ok(userService.setupTwoFactor());
    }

    /**
     * A POST endpoint to enable TFA for a useer.
     *
     * @param input the input to process
     * @return the raw backup codes
     * @throws BadRequestException if enabling fails
     */
    @PostMapping("/enable-tfa") @ResponseBody @NonNull
    public ResponseEntity<List<String>> enableTwoFactor(EnableTFAInput input) throws BadRequestException {
        return ResponseEntity.ok(userService.enableTwoFactor(input));
    }

    /**
     * A POST endpoint to disable TFA for a useer.
     *
     * @param input the input to process
     * @return the disabled response
     * @throws BadRequestException if disabling fails
     */
    @PostMapping("/disable-tfa") @ResponseBody @NonNull
    public ResponseEntity<Map<String, Object>> disableTwoFactor(DisableTFAInput input) throws BadRequestException {
        userService.disableTwoFactor(input);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * A POST endpoint to logout the user.
     *
     * @return the logout response
     */
    @PostMapping("/logout") @ResponseBody @NonNull
    public ResponseEntity<Map<String, Object>> logout() {
        userService.logout();
        return ResponseEntity.ok(Map.of("success", true));
    }
}
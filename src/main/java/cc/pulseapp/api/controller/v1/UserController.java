package cc.pulseapp.api.controller.v1;

import cc.pulseapp.api.exception.impl.BadRequestException;
import cc.pulseapp.api.model.user.User;
import cc.pulseapp.api.model.user.UserDTO;
import cc.pulseapp.api.model.user.input.CompleteOnboardingInput;
import cc.pulseapp.api.service.UserService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * @param email the email to check
     * @return the response
     */
    @GetMapping("/exists") @ResponseBody @NonNull
    public ResponseEntity<Map<String, Object>> doesUserExist(@RequestParam @NonNull String email) {
        return ResponseEntity.ok(Map.of("exists", userService.doesUserExist(email)));
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
}
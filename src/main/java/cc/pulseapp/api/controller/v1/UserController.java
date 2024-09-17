package cc.pulseapp.api.controller.v1;

import cc.pulseapp.api.model.user.UserDTO;
import cc.pulseapp.api.service.UserService;
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
    @PostMapping("/@me") @ResponseBody @NonNull
    public ResponseEntity<UserDTO> getUser() {
        return ResponseEntity.ok(userService.getUser());
    }
}
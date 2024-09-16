package cc.pulseapp.api.controller;

import cc.pulseapp.api.common.EnvironmentUtils;
import cc.pulseapp.api.model.AppInformation;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The root controller for this app.
 *
 * @author Braydon
 */
@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public final class AppController {
    /**
     * The build properties for this app, null if not available.
     */
    private final BuildProperties buildProperties;

    @Autowired
    public AppController(@Nullable BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    /**
     * A GET endpoint to get info about this app.
     *
     * @return the info response
     */
    @GetMapping @ResponseBody @NonNull
    public ResponseEntity<AppInformation> getAppInfo() {
        return ResponseEntity.ok(new AppInformation(
                buildProperties == null ? "unknown" : buildProperties.getVersion(),
                EnvironmentUtils.isProduction() ? "production" : "staging"
        ));
    }
}
package cc.pulseapp.api.service;

import cc.pulseapp.api.common.EnvironmentUtils;
import cc.pulseapp.api.exception.impl.BadRequestException;
import cc.pulseapp.api.model.IGenericResponse;
import com.google.gson.JsonObject;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

/**
 * @author Braydon
 */
@Service
public final class CaptchaService {
    @Value("${captcha.secret}")
    private String secretKey;

    /**
     * Validates the captcha response.
     *
     * @param captchaResponse the response to validate
     * @throws BadRequestException if the response is invalid
     */
    public void validateCaptcha(@NonNull String captchaResponse) throws BadRequestException {
        JsonObject body = new JsonObject();
        body.addProperty("secret", secretKey);
        body.addProperty("response", captchaResponse);
        HttpResponse<JsonNode> response = Unirest.post("https://challenges.cloudflare.com/turnstile/v0/siteverify")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(body)
                .asJson();
        if (EnvironmentUtils.isProduction() && !response.getBody().getObject().getBoolean("success")) {
            throw new BadRequestException(Error.CAPTCHA_INVALID);
        }
    }

    public enum Error implements IGenericResponse {
        CAPTCHA_INVALID
    }
}
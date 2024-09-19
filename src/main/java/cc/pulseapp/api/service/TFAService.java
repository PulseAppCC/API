package cc.pulseapp.api.service;

import de.taimos.totp.TOTP;
import lombok.NonNull;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * This service is responsible for
 * two factor authentication.
 *
 * @author Braydon
 */
@Service
public final class TFAService {
    private static final Base32 BASE_32 = new Base32();
    private static final String APP_ISSUER = "Pulse App";

    /**
     * Generate a secret key.
     *
     * @return the secret key
     */
    @NonNull
    public String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[15];
        random.nextBytes(bytes);
        return BASE_32.encodeToString(bytes);
    }

    /**
     * Generate a QR code URL for a user.
     *
     * @param username the user's username
     * @param secret   the user's tfa secret
     * @return the qr code url
     */
    @NonNull
    public String generateQrCodeUrl(@NonNull String username, @NonNull String secret) {
        return "otpauth://totp/"
                + URLEncoder.encode(APP_ISSUER + ":" + username, StandardCharsets.UTF_8).replace("+", "%20")
                + "?issuer=" + URLEncoder.encode(APP_ISSUER, StandardCharsets.UTF_8).replace("+", "%20")
                + "&secret=" + URLEncoder.encode(secret, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * Get the current 6-digit pin
     * from the given secret key.
     *
     * @param secretKey the secret key
     * @return the 6-digit pin
     */
    @NonNull
    public String getPin(@NonNull String secretKey) {
        byte[] bytes = BASE_32.decode(secretKey);
        String hexKey = Hex.encodeHexString(bytes);
        return TOTP.getOTP(hexKey);
    }
}
package cc.pulseapp.api.common;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * @author Braydon
 */
@UtilityClass
public final class HashUtils {
    private static final SecretKeyFactory PBKDF2;
    private static final int ITERATION_COUNT = 512000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    static {
        try {
            PBKDF2 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Hash the given input.
     *
     * @param salt  the salt to hash with
     * @param input the input to hash
     * @return the hashed input
     */
    @SneakyThrows
    public static String hash(byte[] salt, @NonNull String input) {
        KeySpec spec = new PBEKeySpec(input.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        byte[] hash = PBKDF2.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Generate a salt.
     *
     * @return the generated salt
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[32];
        RANDOM.nextBytes(salt);
        return salt;
    }
}
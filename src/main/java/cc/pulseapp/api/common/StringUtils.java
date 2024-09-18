package cc.pulseapp.api.common;

import cc.pulseapp.api.model.IGenericResponse;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * @author Braydon
 */
@UtilityClass
public final class StringUtils {
    private static final String ALPHABET_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ";
    private static final String NUMERIC_STRING = "0123456789";
    private static final String SPECIAL_STRING = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9_.]*$");

    /**
     * Check if the given email is valid.
     *
     * @param email the email to check
     * @return whether the email is valid
     */
    public static boolean isValidEmail(@NonNull String email) {
        return !email.isBlank() && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Check if the given username is valid.
     *
     * @param username the username to check
     * @return whether the username is valid
     */
    public static boolean isValidUsername(@NonNull String username) {
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Check if the given password meets the requirements.
     *
     * @param password the password to check
     * @return the error if password requirements are not met, otherwise null
     */
    public static PasswordError checkPasswordRequirements(@NonNull String password) {
        return checkPasswordRequirements(password, 8, 76, true, true, true);
    }

    /**
     * Check if the given password meets the requirements.
     *
     * @param password  the password to check
     * @param minLength the minimum length of the password
     * @param maxLength the maximum length of the password
     * @param alphabet  whether the password must contain alphabet characters
     * @param numeric   whether the password must contain numeric characters
     * @param special   whether the password must contain special characters
     * @return the error if password requirements are not met, otherwise null
     */
    private static PasswordError checkPasswordRequirements(@NonNull String password, int minLength, int maxLength, boolean alphabet, boolean numeric, boolean special) {
        boolean tooShort = password.length() < minLength;
        if (password.length() < minLength || password.length() > maxLength) {
            return tooShort ? PasswordError.PASSWORD_TOO_SHORT : PasswordError.PASSWORD_TOO_LONG;
        }
        if (alphabet && !password.matches(".*[a-zA-Z].*")) {
            return PasswordError.PASSWORD_MISSING_ALPHABET;
        }
        if (numeric && !password.matches(".*\\d.*")) {
            return PasswordError.PASSWORD_MISSING_NUMERIC;
        }
        if (special && !password.matches(".*[^a-zA-Z0-9].*")) {
            return PasswordError.PASSWORD_MISSING_SPECIAL;
        }
        return null; // Password meets the requirements
    }

    /**
     * Generate a random string with the given length.
     *
     * @param length   the length of the string
     * @param alphabet whether the string should contain alphabet characters
     * @param numeric  whether the string should contain numeric characters
     * @param special  whether the string should contain special characters
     * @return the generated random string
     */
    @NonNull
    public static String generateRandom(int length, boolean alphabet, boolean numeric, boolean special) {
        if (length < 1) {
            throw new IllegalArgumentException("Length must be at least 1");
        }
        if (!alphabet && !numeric && !special) { // Validate
            throw new IllegalArgumentException("At least one of alphabet, numeric, or special must be true");
        }
        // Build the symbols string
        StringBuilder symbols = new StringBuilder();
        if (alphabet) symbols.append(ALPHABET_STRING);
        if (numeric) symbols.append(NUMERIC_STRING);
        if (special) symbols.append(SPECIAL_STRING);

        // Generate the random string
        char[] buffer = new char[length];
        for (int idx = 0; idx < buffer.length; ++idx) {
            buffer[idx] = symbols.charAt(RANDOM.nextInt(symbols.length()));
        }
        return new String(buffer);
    }

    public enum PasswordError implements IGenericResponse {
        PASSWORD_TOO_SHORT,
        PASSWORD_TOO_LONG,
        PASSWORD_MISSING_ALPHABET,
        PASSWORD_MISSING_NUMERIC,
        PASSWORD_MISSING_SPECIAL
    }
}
package Airline;


import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Salted PBKDF2 password hashing. Plain-text passwords are never stored or
 * logged — only the salt + derived hash are kept (see {@link User}).
 */
public final class PasswordUtil {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH_BITS = 256;
    private static final SecureRandom RNG = new SecureRandom();

    private PasswordUtil() {}

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RNG.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Could not hash password", e);
        }
    }

    public static boolean verify(String password, String saltBase64, String expectedHash) {
        String actual = hash(password, saltBase64);
        return constantTimeEquals(actual, expectedHash);
    }

    /** Avoids leaking timing information about how many characters matched. */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    public enum PasswordStrength { WEAK, MEDIUM, STRONG }

    /** Simple heuristic: length + character-class diversity. */
    public static PasswordStrength checkStrength(String password) {
        if (password == null || password.length() < 6) return PasswordStrength.WEAK;

        boolean hasUpper   = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower   = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit   = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        int score = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);

        if (password.length() >= 10 && score >= 3) return PasswordStrength.STRONG;
        if (password.length() >= 8 && score >= 2) return PasswordStrength.MEDIUM;
        return PasswordStrength.WEAK;
    }
}
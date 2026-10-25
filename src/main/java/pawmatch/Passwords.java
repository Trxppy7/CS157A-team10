package pawmatch;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Passwords {
    private static final int ITERATIONS = 600000;

    public static String hash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
    }

    public static boolean matches(String password, String stored) {
        try {
            if (stored == null) return false;
            String[] parts = stored.split(":");
            if (parts.length != 3) return false;
            int iterations = Integer.parseInt(parts[0]);
            if (iterations < 1 || iterations > ITERATIONS) return false;
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            return MessageDigest.isEqual(expected, derive(password, salt, iterations));
        } catch (IllegalArgumentException error) {
            return false;
        }
    }

    public static void main(String[] args) {
        java.io.Console console = System.console();
        if (console == null) throw new IllegalStateException("Run this from a terminal.");
        char[] password = console.readPassword("Password (8 to 128 characters): ");
        if (password == null || password.length < 8 || password.length > 128) {
            throw new IllegalArgumentException("Use 8 to 128 characters.");
        }
        try {
            System.out.println(hash(new String(password)));
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec key = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(key).getEncoded();
        } catch (GeneralSecurityException error) {
            throw new IllegalStateException("Password hashing is unavailable.", error);
        } finally {
            key.clearPassword();
        }
    }
}

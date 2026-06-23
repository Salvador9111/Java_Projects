package Airline;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Central store for all accounts (admin + passengers). Handles
 * registration, authentication with lockout protection, and the
 * security-question based password reset flow.
 *
 * Accounts are persisted to a file in the user's home directory so they
 * survive application restarts. This is a lightweight, dependency-free
 * stand-in for a real user database / identity provider.
 */
public final class UserStore {

    // NOTE: these two fields must be declared (and therefore initialized)
    // BEFORE `INSTANCE`. Java runs static initializers in textual order, and
    // `INSTANCE`'s constructor calls load(), which needs STORE_FILE — if
    // INSTANCE were declared first, STORE_FILE would still be null when the
    // constructor runs, causing a NullPointerException.
    private static final Path STORE_FILE =
            Paths.get(System.getProperty("user.home"), ".airline_reservation_users.dat");
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private static final UserStore INSTANCE = new UserStore();

    private final Map<String, User> usersByUsername = new LinkedHashMap<>();

    private UserStore() {
        load();
        seedDefaultAdmin();
    }

    public static UserStore getInstance() { return INSTANCE; }

    private void seedDefaultAdmin() {
        boolean hasAdmin = usersByUsername.values().stream().anyMatch(u -> u.getRole() == Role.ADMIN);
        if (!hasAdmin) {
            register("admin", "admin@airline.local", "admin123", Role.ADMIN,
                    "What is the original default admin password?", "admin123");
        }
    }

    public synchronized RegistrationResult register(String username, String email, String password,
                                                    Role role, String securityQuestion, String securityAnswer) {
        if (username == null || username.trim().isEmpty()) return RegistrationResult.INVALID_USERNAME;
        String key = username.trim().toLowerCase();
        if (usersByUsername.containsKey(key)) return RegistrationResult.USERNAME_TAKEN;
        if (password == null || PasswordUtil.checkStrength(password) == PasswordUtil.PasswordStrength.WEAK)
            return RegistrationResult.WEAK_PASSWORD;
        if (securityAnswer == null || securityAnswer.trim().isEmpty())
            return RegistrationResult.MISSING_SECURITY_ANSWER;

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(password, salt);
        String ansSalt = PasswordUtil.generateSalt();
        String ansHash = PasswordUtil.hash(normalize(securityAnswer), ansSalt);

        User u = new User(username.trim(), email, salt, hash, role, securityQuestion, ansSalt, ansHash);
        usersByUsername.put(key, u);
        save();
        return RegistrationResult.SUCCESS;
    }

    public synchronized LoginResult login(String username, String password) {
        if (username == null) return LoginResult.NOT_FOUND;
        User u = usersByUsername.get(username.trim().toLowerCase());
        if (u == null) return LoginResult.NOT_FOUND;
        if (u.isLocked()) return LoginResult.LOCKED;

        boolean ok = PasswordUtil.verify(password == null ? "" : password, u.getSalt(), u.getPasswordHash());
        if (ok) {
            u.setFailedAttempts(0);
            u.setLastLogin(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.now()));
            save();
            return LoginResult.SUCCESS;
        }

        u.setFailedAttempts(u.getFailedAttempts() + 1);
        if (u.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            u.setLocked(true);
        }
        save();
        return u.isLocked() ? LoginResult.LOCKED : LoginResult.WRONG_PASSWORD;
    }

    public synchronized Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable(usersByUsername.get(username.trim().toLowerCase()));
    }

    public synchronized boolean verifySecurityAnswer(String username, String answer) {
        User u = usersByUsername.get(username == null ? "" : username.trim().toLowerCase());
        if (u == null || answer == null) return false;
        return PasswordUtil.verify(normalize(answer), u.getSecurityAnswerSalt(), u.getSecurityAnswerHash());
    }

    /** Resets the password (used after a successful security-question check) and clears any lockout. */
    public synchronized boolean resetPassword(String username, String newPassword) {
        User u = usersByUsername.get(username == null ? "" : username.trim().toLowerCase());
        if (u == null) return false;
        if (newPassword == null || PasswordUtil.checkStrength(newPassword) == PasswordUtil.PasswordStrength.WEAK)
            return false;

        String salt = PasswordUtil.generateSalt();
        u.setSalt(salt);
        u.setPasswordHash(PasswordUtil.hash(newPassword, salt));
        u.setFailedAttempts(0);
        u.setLocked(false);
        save();
        return true;
    }

    /** Changes the password for an already-authenticated user who knows their current password. */
    public synchronized boolean changePassword(String username, String currentPassword, String newPassword) {
        User u = usersByUsername.get(username == null ? "" : username.trim().toLowerCase());
        if (u == null) return false;
        if (!PasswordUtil.verify(currentPassword == null ? "" : currentPassword, u.getSalt(), u.getPasswordHash()))
            return false;
        if (newPassword == null || PasswordUtil.checkStrength(newPassword) == PasswordUtil.PasswordStrength.WEAK)
            return false;

        String salt = PasswordUtil.generateSalt();
        u.setSalt(salt);
        u.setPasswordHash(PasswordUtil.hash(newPassword, salt));
        save();
        return true;
    }

    private String normalize(String answer) { return answer.trim().toLowerCase(); }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!Files.exists(STORE_FILE)) return;
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(STORE_FILE.toFile())))) {
            Map<String, User> loaded = (Map<String, User>) in.readObject();
            usersByUsername.putAll(loaded);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("UserStore: could not load saved accounts (" + e.getMessage() + ")");
        }
    }

    private void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(STORE_FILE.toFile())))) {
            out.writeObject(new LinkedHashMap<>(usersByUsername));
        } catch (IOException e) {
            System.err.println("UserStore: could not save accounts (" + e.getMessage() + ")");
        }
    }

    public enum RegistrationResult { SUCCESS, USERNAME_TAKEN, WEAK_PASSWORD, INVALID_USERNAME, MISSING_SECURITY_ANSWER }
    public enum LoginResult { SUCCESS, WRONG_PASSWORD, NOT_FOUND, LOCKED }
}
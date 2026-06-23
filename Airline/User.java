package Airline;


import java.io.Serializable;

/**
 * A registered account (admin or passenger).
 *
 * Passwords and security-question answers are never stored in plain text —
 * only salted PBKDF2 hashes (see {@link PasswordUtil}) are kept.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private String email;
    private String salt;
    private String passwordHash;
    private final Role role;

    private String securityQuestion;
    private String securityAnswerSalt;
    private String securityAnswerHash;

    private int failedAttempts = 0;
    private boolean locked = false;
    private String lastLogin = "Never";

    public User(String username, String email, String salt, String passwordHash, Role role,
                String securityQuestion, String securityAnswerSalt, String securityAnswerHash) {
        this.username = username;
        this.email = email;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.role = role;
        this.securityQuestion = securityQuestion;
        this.securityAnswerSalt = securityAnswerSalt;
        this.securityAnswerHash = securityAnswerHash;
    }

    public String getUsername() { return username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }

    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }

    public String getSecurityAnswerSalt() { return securityAnswerSalt; }
    public void setSecurityAnswerSalt(String securityAnswerSalt) { this.securityAnswerSalt = securityAnswerSalt; }

    public String getSecurityAnswerHash() { return securityAnswerHash; }
    public void setSecurityAnswerHash(String securityAnswerHash) { this.securityAnswerHash = securityAnswerHash; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
}
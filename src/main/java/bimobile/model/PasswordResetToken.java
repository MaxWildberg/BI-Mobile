package bimobile.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA-Entität für Passwort-Zurücksetz-Tokens,
 * verknüpft mit einem Benutzer, inklusive Ablaufzeit, „benutzt“-Status und Gültigkeitsprüfung.
 *
 * @author Jannick Braun
 */


@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    private static final int EXPIRATION_HOURS = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean used = false;

    public PasswordResetToken() {}

    public PasswordResetToken(User user) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
        this.expiryDate = LocalDateTime.now().plusHours(EXPIRATION_HOURS);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean isValid() {
        return !isExpired() && !used;
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public User getUser() { return user; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}
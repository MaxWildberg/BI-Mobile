package bimobile.service;

import bimobile.dao.PasswordResetTokenRepository;
import bimobile.dao.UserRepository;
import bimobile.model.PasswordResetToken;
import bimobile.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserRepository userRepository,
                                JavaMailSender mailSender,
                                PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public boolean initiatePasswordReset(String email, String baseUrl) {
        Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase().trim());

        if (userOpt.isEmpty()) {
            logger.info("Password reset requested for non-existent email: {}", email);
            return true;
        }

        User user = userOpt.get();

        tokenRepository.deleteByUser(user);

        PasswordResetToken token = new PasswordResetToken(user);
        tokenRepository.save(token);

        String resetLink = baseUrl + "/reset-password?token=" + token.getToken();
        sendResetEmail(user.getEmail(), user.getFirstName(), resetLink);

        logger.info("Password reset email sent to: {}", email);
        return true;
    }

    private void sendResetEmail(String toEmail, String firstName, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("leonard.koechling@hsbi.de");  // ANPASSEN an euren SMTP-User
            message.setTo(toEmail);
            message.setSubject("BI-Mobile - Passwort zurücksetzen");
            message.setText(
                    "Hallo " + firstName + ",\n\n" +
                            "Sie haben eine Anfrage zum Zurücksetzen Ihres Passworts gestellt.\n\n" +
                            "Klicken Sie auf den folgenden Link, um Ihr Passwort zurückzusetzen:\n" +
                            resetLink + "\n\n" +
                            "Dieser Link ist 24 Stunden gültig.\n\n" +
                            "Falls Sie diese Anfrage nicht gestellt haben, können Sie diese E-Mail ignorieren.\n\n" +
                            "Mit freundlichen Grüßen,\n" +
                            "Ihr BI-Mobile Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    public Optional<PasswordResetToken> validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty() || !tokenOpt.get().isValid()) {
            return Optional.empty();
        }

        return tokenOpt;
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = validateToken(token);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        logger.info("Password reset successful for user: {}", user.getEmail());
        return true;
    }
}
package bimobile.config;

import bimobile.dao.UserRepository;
import bimobile.model.RoleType;
import bimobile.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            logger.info("=== Prüfe Benutzer-Initialisierung ===");
            logger.info("Anzahl User in DB: {}", userRepository.count());

            if (userRepository.count() == 0) {
                logger.info("Keine Benutzer gefunden - erstelle Testbenutzer...");

                User director = new User(
                        "Max",
                        "Mustermann",
                        "director@bimobile.de",
                        passwordEncoder.encode("director123"),
                        RoleType.MANAGEMENT
                );
                userRepository.save(director);
                logger.info("Testbenutzer erstellt: director@bimobile.de / director123");

                User manager = new User(
                        "Erika",
                        "Musterfrau",
                        "manager@bimobile.de",
                        passwordEncoder.encode("manager123"),
                        RoleType.BRANCH_MANAGER
                );
                userRepository.save(manager);
                logger.info("Testbenutzer erstellt: manager@bimobile.de / manager123");

                User employee = new User(
                        "Hans",
                        "Müller",
                        "employee@bimobile.de",
                        passwordEncoder.encode("employee123"),
                        RoleType.EMPLOYEE
                );
                userRepository.save(employee);
                logger.info("Testbenutzer erstellt: employee@bimobile.de / employee123");

                logger.info("=== Testbenutzer-Initialisierung abgeschlossen ===");
            } else {
                logger.info("Benutzer existieren bereits, überspringe Initialisierung.");
            }
        };
    }
}
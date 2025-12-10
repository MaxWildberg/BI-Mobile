package bimobile.security;

import bimobile.service.CustomUserDetailsService;
import bimobile.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Konfiguration des Spring Security Filters.
     * Vaadin bringt bereits eine eigene Sicherheitskonfiguration mit, die unter anderem
     * automatisch anyRequest().authenticated() setzt. Dieses Verhalten muss erhalten bleiben,
     * deshalb definiere ich hier nur meine eigenen Ausnahmen (z. B. H2-Console).
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // Eigene Ausnahmen definieren (hier: H2-Konsole freigeben)
        // Wichtig: KEIN anyRequest() an dieser Stelle, denn Vaadin übernimmt das intern!
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
        );

        // Die H2-Konsole benötigt spezielle Header- und CSRF-Ausnahmen:
        // - FrameOptions: erlaubt das Einbetten der H2-Konsole
        // - CSRF-Ausnahme, damit das UI korrekt geladen wird
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));

        // Mein eigener UserDetailsService für die Authentifizierung
        http.userDetailsService(userDetailsService);

        // VaadinSecurity übernimmt:
        // - anyRequest().authenticated()
        // - SessionHandling
        // - AccessDeniedView etc.
        super.configure(http);

        // Login-View festlegen, die bei nicht eingeloggten Nutzern angezeigt wird
        setLoginView(http, LoginView.class);
    }

    /**
     * Zusätzliche In-Memory-User für Test- und Entwicklungszwecke.
     * Mein CustomUserDetailsService bleibt aktiv, dieses InMemory-Bean dient vor allem
     * dazu, dass ich mich schnell einloggen kann, ohne echte DB-Daten anzulegen.
     */
    @Bean
    public UserDetailsService users() {
        var user = User.withUsername("user")
                .password(passwordEncoder().encode("user123"))
                .roles("USER")
                .build();

        var admin = User.withUsername("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN", "USER")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    /**
     * PasswordEncoder für sichere Passwort-Hashes (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
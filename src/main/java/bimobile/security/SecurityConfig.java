package bimobile.security;

import bimobile.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Konfiguriert die Spring/Vaadin-Sicherheit der Anwendung: richtet Authentifizierung mit der Vaadin-LoginView ein,
 * erlaubt den Zugriff auf die H2-Konsole (inkl. Frame-/CSRF-Anpassungen)
 * und stellt einen BCrypt-PasswordEncoder als Bean bereit.
 *
 * @author Jannick Braun
 */


@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/h2-console/**").permitAll()
		);

		http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
		http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));

		super.configure(http);

		setLoginView(http, LoginView.class);
	}
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}

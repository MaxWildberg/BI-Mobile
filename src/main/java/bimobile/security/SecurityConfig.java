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

    @Override
    protected void configure(HttpSecurity http) throws Exception {

	    // H2 Console erlauben und übrige Anfragen absichern
	    http.authorizeHttpRequests(auth -> auth
			    .requestMatchers("/h2-console/**").permitAll()
			    .anyRequest().authenticated()
	    );

	    // H2 benötigt Frames
	    http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
	    http.csrf(csrf -> csrf.disable());
	    http.userDetailsService(userDetailsService);

	    super.configure(http);

	    // Login View definieren
	    setLoginView(http, LoginView.class);
    }
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

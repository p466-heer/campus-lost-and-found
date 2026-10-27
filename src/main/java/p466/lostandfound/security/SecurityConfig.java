
package p466.lostandfound.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import p466.lostandfound.user.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${spring.h2.console.enabled:false}") boolean h2ConsoleEnabled
    ) throws Exception {

        http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/actuator/health").permitAll();

                    if (h2ConsoleEnabled) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }

                    auth.requestMatchers("/admin/**").hasRole("ADMIN")
                            .requestMatchers(
                                    "/",
                                    "/items",
                                    "/items/resolved",
                                    "/items/{id}",
                                    "/register",
                                    "/login",
                                    "/css/**",
                                    "/js/**",
                                    "/images/**"
                            ).permitAll()
                            .anyRequest().authenticated();
                })
                .csrf(csrf -> {
                    if (h2ConsoleEnabled) {
                        csrf.ignoringRequestMatchers("/h2-console/**");
                    }
                })
                .headers(headers -> {
                    if (h2ConsoleEnabled) {
                        headers.frameOptions(frame -> frame.sameOrigin());
                    }
                })
                .formLogin(form -> form
                        .defaultSuccessUrl("/items", true)
                        .permitAll()
                )
                .logout(logout -> logout.permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(AppUserRepository userRepository) {
        return email -> {
            var appUser = userRepository.findByEmail(email.toLowerCase())
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found"));

            return User.withUsername(appUser.getEmail())
                    .password(appUser.getPassword())
                    .roles(appUser.getRole())
                    .build();
        };
    }
}

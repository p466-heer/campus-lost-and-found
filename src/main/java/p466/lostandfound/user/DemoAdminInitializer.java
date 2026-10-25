package p466.lostandfound.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoAdminInitializer {

    @Bean
    CommandLineRunner createDemoAdmin(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            if (!userRepository.existsByEmail("admin@example.com")) {
                AppUser admin = new AppUser(
                        "Luddy Admin",
                        "admin@example.com",
                        passwordEncoder.encode("Admin123!")
                );

                admin.setRole("ADMIN");
                userRepository.save(admin);
            }
        };
    }
}

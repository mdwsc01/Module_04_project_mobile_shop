package com.example.mobileshop.config;

import com.example.mobileshop.entity.AdminAccount;
import com.example.mobileshop.repository.AdminAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeedConfig {
    @Bean
    CommandLineRunner seedAdmin(AdminAccountRepository accounts, PasswordEncoder encoder,
                                @Value("${app.admin.username:admin}") String username,
                                @Value("${app.admin.password:Admin@123}") String password) {
        return args -> {
            if (accounts.count() != 0 || username.isBlank() || password.isBlank()) return;
            AdminAccount admin = new AdminAccount();
            admin.setUsername(username);
            admin.setPasswordHash(encoder.encode(password));
            accounts.save(admin);
        };
    }
}

package com.example.mobileshop.config;

import com.example.mobileshop.repository.AdminAccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(AdminAccountRepository accounts) {
        return username -> accounts.findByUsername(username)
                .map(a -> User.withUsername(a.getUsername()).password(a.getPasswordHash()).roles("ADMIN").build())
                .orElseThrow(() -> new UsernameNotFoundException("Không tồn tại tài khoản"));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true).failureUrl("/login?error").permitAll())
                .rememberMe(remember -> remember.tokenValiditySeconds(7 * 24 * 60 * 60))
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout"))
                .build();
    }
}

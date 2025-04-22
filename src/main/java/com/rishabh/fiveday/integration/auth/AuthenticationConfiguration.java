package com.rishabh.fiveday.integration.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring Security configuration for API authentication
 */
@Configuration
@EnableWebSecurity
public class AuthenticationConfiguration {

    /**
     * Configure security for the application
     * For this API, we're using API keys for authentication with Git providers,
     * not for authenticating users to our API. So we disable CSRF and allow all requests.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .httpBasic(withDefaults());
        
        return http.build();
    }
}
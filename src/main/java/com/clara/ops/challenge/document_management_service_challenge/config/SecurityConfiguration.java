package com.clara.ops.challenge.document_management_service_challenge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfiguration class defines the security settings and configurations for the application
 * using Spring Security. It enables web security, configures HTTP security, and provides beans for
 * password encoding and security filter chain. <br>
 * The class includes the definition of a stateless session management policy and disables
 * Cross-Site Request Forgery (CSRF) and Cross-Origin Resource Sharing (CORS) to allow access to
 * specific endpoints. <br>
 * It also enforces authentication for all incoming requests except to the resources exposed by the
 * {@link
 * com.clara.ops.challenge.document_management_service_challenge.controller.DocumentManagementController}
 * controller.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Bean
  public SecurityFilterChain appSecurity(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers("/registration").permitAll().anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}

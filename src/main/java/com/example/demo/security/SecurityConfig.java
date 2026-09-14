package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          AuthenticationProvider authenticationProvider,
                          CustomAuthenticationEntryPoint authenticationEntryPoint,
                          CustomAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Ù…Ø³ÛŒØ±Ù‡Ø§ÛŒ Ú©Ø§Ù…Ù„Ø§Ù‹ Ø¹Ù…ÙˆÙ…ÛŒ (Public)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        .requestMatchers("/h2-console/**", "/error", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 2. Ù…Ø³ÛŒØ±Ù‡Ø§ÛŒ Ø§Ø®ØªØµØ§ØµÛŒ (Ø¨Ø§ÛŒØ¯ Ù‚Ø¨Ù„ Ø§Ø² /** ØªØ¹Ø±ÛŒÙ Ø´ÙˆÙ†Ø¯)
                        // Ø¯Ø³ØªØ±Ø³ÛŒ Ø¨Ù‡ Ù¾Ø±ÙˆÙØ§ÛŒÙ„ Ø´Ø®ØµÛŒ Ø¨Ø±Ø§ÛŒ Ù‡Ø± Ø¯Ùˆ Ù†Ù‚Ø´ Ù…Ø¬Ø§Ø² Ø§Ø³Øª
                        .requestMatchers("/api/users/me").hasAnyRole("USER", "ADMIN")

                        // 3. Ù…Ø³ÛŒØ±Ù‡Ø§ÛŒ Ù…Ø¯ÛŒØ±ÛŒØªÛŒ (ÙÙ‚Ø· ADMIN)
                        // Ù‡Ø± Ø¹Ù…Ù„ÛŒØ§Øª Ø¯ÛŒÚ¯Ø±ÛŒ Ø±ÙˆÛŒ Ú©Ø§Ø±Ø¨Ø±Ø§Ù† Ù…Ø­Ø¯ÙˆØ¯ Ø¨Ù‡ Ø§Ø¯Ù…ÛŒÙ† Ø§Ø³Øª
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // 4. Ø³Ø§ÛŒØ± Ø¯Ø±Ø®ÙˆØ§Ø³Øªâ€ŒÙ‡Ø§
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
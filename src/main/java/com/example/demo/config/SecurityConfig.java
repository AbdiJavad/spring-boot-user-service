package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ۱. تعریف الگوریتم هش کردن پسورد با استاندارد BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ۲. تعریف قوانین فیلترهای امنیتی برای مسیرهای API
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // غیرفعال‌سازی CSRF برای APIهای REST بدون Session
                .csrf(csrf -> csrf.disable())

                // سیاست مدیریت نشست به صورت Stateless (بدون ساخت سشن سمت سرور)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // مشخص کردن مجوز دسترسی مسیرها
                .authorizeHttpRequests(auth -> auth
                        // مسیرهای باز برای ثبت‌نام و تست
                        .requestMatchers("/api/v1/auth/**", "/api/v1/users/register").permitAll()
                        // بقیه مسیرها نیاز به احراز هویت دارند
                        .anyRequest().authenticated()
                )

                // فعال‌سازی Basic Auth برای تست‌های سریع اولیه
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}

package com.example.demo.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import org.springframework.security.authentication.AuthenticationProvider; // این خط را اگر AuthenticationProvider به صورت جداگانه تعریف نکردی، حذف کن.
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    // private final AuthenticationProvider authenticationProvider; // این خط را اگر AuthenticationProvider به صورت جداگانه تعریف نکردی، حذف کن.

    // سازنده را بر اساس مواردی که استفاده می‌کنی تنظیم کن
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter /*, AuthenticationProvider authenticationProvider*/) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        // this.authenticationProvider = authenticationProvider; // این خط را اگر AuthenticationProvider به صورت جداگانه تعریف نکردی، حذف کن.
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // غیرفعال‌سازی CSRF برای APIهای Stateless
                .csrf(csrf -> csrf.disable())

                // مدیریت خطا: تبدیل خطای عدم احراز هویت به 401 استاندارد JSON
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                )

                // عدم نگهداری Session روی سرور
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // تنظیم دسترسی اندپوینت‌ها
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/users/register",
                                "/error" // برای اطمینان از اینکه Spring Security صفحه خطا را بلاک نکند
                        ).permitAll()
                        .anyRequest().authenticated() // همه درخواست‌های دیگر نیاز به احراز هویت دارند
                );

        // اگر AuthenticationProvider جداگانه داری، این خط را فعال کن
        // .authenticationProvider(authenticationProvider)

        // قرار دادن فیلتر JWT قبل از فیلتر پیش‌فرض لاگین
        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    // Bean برای سفارشی‌سازی پاسخ عدم احراز هویت
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write("""
                        {
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "Authentication is required to access this resource"
                        }
                        """);
        };
    }
}

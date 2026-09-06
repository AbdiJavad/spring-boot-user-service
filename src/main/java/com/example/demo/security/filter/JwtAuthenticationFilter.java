package com.example.demo.security.filter;

import com.example.demo.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j // برای لاگ کردن حرفه‌ای (در پروژه‌های Enterprise بسیار حیاتی است)
public class JwtAuthenticationFilter extends org.springframework.web.filter.OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = extractAuthorizationHeader(request);
        final String jwt;
        final String userEmail;

        // ۱. بررسی اینکه آیا هدر Authorization وجود دارد و با Bearer شروع می‌شود یا خیر
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7); // حذف کلمه "Bearer "
        try {
            userEmail = jwtService.extractUsername(jwt);

            // ۲. اگر نام کاربری استخراج شده و کاربر هنوز در SecurityContext نیست
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // ۳. بررسی اعتبار توکن
                if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // ۴. قرار دادن احراز هویت در Context سیستم
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("User {} authenticated successfully via JWT", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("Cannot authenticate user: {}", e.getMessage());
            // در اینجا می‌توانیم خطای اختصاصی برگردانیم، فعلاً اجازه می‌دهیم فیلترهای بعدی هندل کنند
        }

        filterChain.doFilter(request, response);
    }

    private String extractAuthorizationHeader(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }
}

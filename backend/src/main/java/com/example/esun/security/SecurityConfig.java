package com.example.esun.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 防護關閉：使用 JWT 無狀態驗證，不依賴Cookie/Session
            .csrf(AbstractHttpConfigurer::disable)

            // 允許 CORS：讓前端 localhost:3000 能呼叫後端 localhost:8080
            .cors(cors -> cors.configure(http))

            // STATELESS：不建立 Session，每次請求靠 JWT 辨識身份
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // 登入、註冊不需要 JWT
                .requestMatchers("/api/auth/**").permitAll()
                // 其他所有 API 都需要帶有效的 JWT
                .anyRequest().authenticated())

            // 在內建帳號密碼過濾器之前插入自訂的 JWT 過濾器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

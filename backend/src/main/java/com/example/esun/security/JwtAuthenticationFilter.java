package com.example.esun.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

// 每個請求進來時執行一次，從 Header 取出 JWT 驗證，通過後將使用者資訊存入 SecurityContext
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 前端每次請求都會在 Header 帶上：Authorization: Bearer eyJ...
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // 去掉 "Bearer " 前綴

            if (jwtTokenProvider.validateToken(token)) {
                String phone = jwtTokenProvider.getPhone(token);
                Long userId = jwtTokenProvider.getUserId(token);

                // 建立認證物件，details 存 userId 供 SecurityUtils 取用
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                new User(phone, "", Collections.emptyList()),
                                null,
                                Collections.emptyList());
                authentication.setDetails(userId);

                // 存入 SecurityContext，同一請求的任何位置都能讀到
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 繼續執行下一個過濾器，最終到達 Controller
        filterChain.doFilter(request, response);
    }
}

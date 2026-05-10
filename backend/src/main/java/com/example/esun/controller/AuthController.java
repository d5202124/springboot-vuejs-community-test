package com.example.esun.controller;

import com.example.esun.dto.AuthRequest;
import com.example.esun.dto.AuthResponse;
import com.example.esun.dto.RegisterRequest;
import com.example.esun.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/register - 註冊，@Valid 觸發 DTO 欄位驗證
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("註冊成功");
    }

    // POST /api/auth/login - 登入，成功回傳 JWT 與 userName
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

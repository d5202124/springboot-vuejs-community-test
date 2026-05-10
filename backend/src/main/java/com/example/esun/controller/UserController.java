package com.example.esun.controller;

import com.example.esun.common.SecurityUtils;
import com.example.esun.dto.ProfileRequest;
import com.example.esun.dto.ProfileResponse;
import com.example.esun.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// /me 路徑代表「目前登入的使用者自己」，不需要帶 userId 參數
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users/me - 取得個人資料
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getProfile() {
        return ResponseEntity.ok(userService.getProfile(SecurityUtils.getCurrentUserId()));
    }

    // PUT /api/users/me - 更新個人資料（姓名、頭像、自我介紹）
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(SecurityUtils.getCurrentUserId(), request));
    }
}

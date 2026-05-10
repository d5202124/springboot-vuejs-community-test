package com.example.esun.service;

import com.example.esun.common.InputSanitizer;
import com.example.esun.common.PasswordHasher;
import com.example.esun.dto.AuthRequest;
import com.example.esun.dto.AuthResponse;
import com.example.esun.dto.RegisterRequest;
import com.example.esun.repository.UserRepository;
import com.example.esun.security.JwtTokenProvider;
import com.example.esun.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordHasher passwordHasher,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String phone = request.getPhone();

        // 手機號碼重複則（GlobalExceptionHandler 轉成 400）
        if (userRepository.findByPhone(phone) != null) {
            throw new IllegalArgumentException("此手機號碼已被註冊");
        }

        String hashed = passwordHasher.hashPassword(request.getPassword()); // 密碼加鹽雜湊

        User user = new User();
        user.setUserName(request.getUserName());
        user.setPhone(phone);
        user.setPassword(hashed);
        user.setCoverImage(InputSanitizer.sanitize(request.getCoverImage())); // URL 欄位 XSS 清理
        user.setBiography(request.getBiography());

        userRepository.createUser(user);
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByPhone(request.getPhone());

        // 統一回傳同一個錯誤訊息，避免透露是帳號還是密碼錯誤
        if (user == null || !passwordHasher.verifyPassword(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("登入失敗：帳號或密碼錯誤");
        }

        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getPhone());
        return new AuthResponse(token, user.getUserName());
    }
}

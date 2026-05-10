package com.example.esun.service;

import com.example.esun.common.InputSanitizer;
import com.example.esun.dto.ProfileRequest;
import com.example.esun.dto.ProfileResponse;
import com.example.esun.model.User;
import com.example.esun.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 取得個人資料，轉換成 ProfileResponse（不含密碼）回傳
    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) throw new NoSuchElementException("使用者不存在");
        return toResponse(user);
    }

    // 更新個人資料（姓名、頭像、自我介紹）
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileRequest request) {
        User user = userRepository.findById(userId);
        if (user == null) throw new NoSuchElementException("使用者不存在");

        user.setUserName(request.getUserName());
        user.setCoverImage(InputSanitizer.sanitize(request.getCoverImage())); // URL 清理
        user.setBiography(request.getBiography());
        userRepository.updateUser(user);

        return toResponse(user);
    }

    // User model 轉 ProfileResponse（去掉密碼欄位）
    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(
                user.getUserId(), user.getUserName(), user.getPhone(),
                user.getCoverImage(), user.getBiography());
    }
}

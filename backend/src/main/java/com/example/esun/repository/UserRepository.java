package com.example.esun.repository;

import com.example.esun.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    // RowMapper：將 ResultSet 的一列資料轉成 User 物件
    private final RowMapper<User> userMapper = (rs, rowNum) -> {
        User user = new User();
        user.setUserId(rs.getLong("UserID"));
        user.setUserName(rs.getString("UserName"));
        user.setPhone(rs.getString("Phone"));
        user.setPassword(rs.getString("Password"));
        user.setCoverImage(rs.getString("CoverImage"));
        user.setBiography(rs.getString("Biography"));
        return user;
    };

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 呼叫 Stored Procedure 新增使用者，? 對應後面的參數（防 SQL Injection）
    public void createUser(User user) {
        jdbcTemplate.update("CALL sp_create_user(?, ?, ?, ?, ?)",
                user.getUserName(), user.getPhone(), user.getPassword(),
                user.getCoverImage(), user.getBiography());
    }

    // 依手機號碼查詢使用者（登入、重複註冊驗證用）
    public User findByPhone(String phone) {
        return jdbcTemplate.query("CALL sp_find_user_by_phone(?)", userMapper, phone)
                .stream().findFirst().orElse(null);
    }

    // 依 userId 查詢使用者（個人資料頁用）
    public User findById(Long userId) {
        return jdbcTemplate.query("CALL sp_get_user_by_id(?)", userMapper, userId)
                .stream().findFirst().orElse(null);
    }

    // 更新使用者資料（姓名、頭像、自我介紹）
    public void updateUser(User user) {
        jdbcTemplate.update("CALL sp_update_user(?, ?, ?, ?)",
                user.getUserId(), user.getUserName(), user.getCoverImage(), user.getBiography());
    }
}

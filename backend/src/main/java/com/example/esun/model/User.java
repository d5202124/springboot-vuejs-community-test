package com.example.esun.model;

public class User {
    private Long userId;
    private String userName;
    private String phone;
    private String password; // 資料庫存的是 salt:hash，不是明碼
    private String coverImage;
    private String biography;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }
}

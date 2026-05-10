package com.example.esun.dto;

import jakarta.validation.constraints.NotBlank;

public class ProfileRequest {

    @NotBlank(message = "姓名不可為空")
    private String userName;

    private String coverImage; // 頭像 URL（選填）
    private String biography;  // 自我介紹（選填）

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }
}

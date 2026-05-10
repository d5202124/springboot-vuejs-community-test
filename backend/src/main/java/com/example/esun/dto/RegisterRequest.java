package com.example.esun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RegisterRequest {

    @NotBlank(message = "姓名不可為空")
    private String userName;

    @NotBlank(message = "手機號碼不可為空")
    @Pattern(regexp = "^09\\d{8}$", message = "手機號碼格式錯誤，須為09開頭共10碼")
    private String phone;

    @NotBlank(message = "密碼不可為空")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
        message = "密碼須至少8碼，包含大小寫英文與數字，不含特殊符號"
    )
    private String password;

    private String coverImage; // 選填
    private String biography;  // 選填

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

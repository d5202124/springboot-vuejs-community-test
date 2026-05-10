package com.example.esun.dto;

public class ProfileResponse {
    private Long userId;
    private String userName;
    private String phone;
    private String coverImage;
    private String biography;

    public ProfileResponse(Long userId, String userName, String phone, String coverImage, String biography) {
        this.userId = userId;
        this.userName = userName;
        this.phone = phone;
        this.coverImage = coverImage;
        this.biography = biography;
    }

    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getPhone() { return phone; }
    public String getCoverImage() { return coverImage; }
    public String getBiography() { return biography; }
}

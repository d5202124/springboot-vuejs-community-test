package com.example.esun.model;

import java.time.LocalDateTime;

public class Post {
    private Long postId;
    private Long userId;
    private String userName;   // 從 User 資料表 JOIN 而來，非 Post 欄位
    private String coverImage; // 發文者的頭像 URL，從 User JOIN 而來
    private String content;
    private String image;      // 貼文附圖 URL（選填）
    private LocalDateTime createdAt;

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

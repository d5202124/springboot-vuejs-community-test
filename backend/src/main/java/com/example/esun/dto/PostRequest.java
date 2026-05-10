package com.example.esun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PostRequest {
    private Long userId; // 不使用，由後端從 JWT 取得

    @NotBlank(message = "內容不可為空")
    @Size(max = 2000, message = "內容不可超過2000字")
    private String content;

    private String image; // 圖片 URL（選填）

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}

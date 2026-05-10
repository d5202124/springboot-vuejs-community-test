package com.example.esun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {
    private Long userId; // 不使用，由後端從 JWT 取得

    private Long postId; // 新增留言必填，更新留言不使用

    @NotBlank(message = "內容不可為空")
    @Size(max = 500, message = "留言不可超過500字")
    private String content;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

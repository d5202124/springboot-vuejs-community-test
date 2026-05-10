package com.example.esun.controller;

import com.example.esun.common.SecurityUtils;
import com.example.esun.dto.CommentRequest;
import com.example.esun.model.Comment;
import com.example.esun.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // POST /api/comments - 新增留言
    @PostMapping
    public ResponseEntity<String> createComment(@Valid @RequestBody CommentRequest request) {
        commentService.createComment(request, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok("留言成功");
    }

    // GET /api/comments/post/{postId} - 取得指定貼文的所有留言
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsForPost(postId));
    }

    // PUT /api/comments/{commentId} - 編輯留言（需Service 層驗證）
    @PutMapping("/{commentId}")
    public ResponseEntity<String> updateComment(@PathVariable Long commentId,
                                                @Valid @RequestBody CommentRequest request) {
        commentService.updateComment(commentId, request.getContent(), SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok("留言已更新");
    }

    // DELETE /api/comments/{commentId} - 刪除留言（需Service 層驗證）
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok("留言已刪除");
    }
}

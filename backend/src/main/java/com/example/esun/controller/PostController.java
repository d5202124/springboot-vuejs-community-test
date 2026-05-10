package com.example.esun.controller;

import com.example.esun.common.SecurityUtils;
import com.example.esun.dto.PostRequest;
import com.example.esun.model.Post;
import com.example.esun.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// userId 一律從 JWT 取得，不信任前端傳來的值
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // POST /api/posts - 新增貼文
    @PostMapping
    public ResponseEntity<String> createPost(@Valid @RequestBody PostRequest request) {
        postService.createPost(request, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok("發文成功");
    }

    // GET /api/posts - 取得所有貼文（含發文者姓名、頭像，依時間倒序）
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getPosts());
    }

    // PUT /api/posts/{postId} - 編輯貼文（需Service 層驗證）
    @PutMapping("/{postId}")
    public ResponseEntity<String> updatePost(@PathVariable Long postId,
                                             @Valid @RequestBody PostRequest request) {
        postService.updatePost(postId, request, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok("貼文已更新");
    }

    // DELETE /api/posts/{postId} - 刪除貼文（需Service 層驗證）
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok("貼文已刪除");
    }
}

package com.example.esun.service;

import com.example.esun.common.InputSanitizer;
import com.example.esun.dto.PostRequest;
import com.example.esun.model.Post;
import com.example.esun.repository.PostRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // 新增貼文，userId 從 JWT 取得
    @Transactional
    public void createPost(PostRequest request, Long currentUserId) {
        Post post = new Post();
        post.setUserId(currentUserId);
        post.setContent(request.getContent());
        post.setImage(InputSanitizer.sanitize(request.getImage())); // URL 欄位 XSS 清理
        postRepository.createPost(post);
    }

    // 取得所有貼文（SP中JOIN User，含發文者姓名與頭像）
    public List<Post> getPosts() {
        return postRepository.findAllPosts();
    }

    // 更新貼文，先驗證擁有者
    @Transactional
    public void updatePost(Long postId, PostRequest request, Long currentUserId) {
        Post existing = postRepository.findPostById(postId);
        if (existing == null) throw new NoSuchElementException("貼文不存在");
        if (!existing.getUserId().equals(currentUserId)) throw new AccessDeniedException("無權限操作此資源");

        Post post = new Post();
        post.setPostId(postId);
        post.setContent(request.getContent());
        post.setImage(InputSanitizer.sanitize(request.getImage()));
        postRepository.updatePost(post);
    }

    // 刪除貼文，先驗證擁有者
    @Transactional
    public void deletePost(Long postId, Long currentUserId) {
        Post existing = postRepository.findPostById(postId);
        if (existing == null) throw new NoSuchElementException("貼文不存在");
        if (!existing.getUserId().equals(currentUserId)) throw new AccessDeniedException("無權限操作此資源");
        postRepository.deletePost(postId);
    }
}

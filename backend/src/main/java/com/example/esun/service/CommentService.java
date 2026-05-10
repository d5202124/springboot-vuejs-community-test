package com.example.esun.service;

import com.example.esun.dto.CommentRequest;
import com.example.esun.model.Comment;
import com.example.esun.repository.CommentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    // 新增留言，userId 從 JWT 取得
    @Transactional
    public void createComment(CommentRequest request, Long currentUserId) {
        Comment comment = new Comment();
        comment.setUserId(currentUserId);
        comment.setPostId(request.getPostId());
        comment.setContent(request.getContent());
        commentRepository.createComment(comment);
    }

    // 取得指定貼文的所有留言（SP 中 JOIN User，含留言者姓名）
    public List<Comment> getCommentsForPost(Long postId) {
        return commentRepository.findCommentsByPostId(postId);
    }

    // 更新留言，先驗證擁有者
    @Transactional
    public void updateComment(Long commentId, String content, Long currentUserId) {
        Comment existing = commentRepository.findCommentById(commentId);
        if (existing == null) throw new NoSuchElementException("留言不存在");
        if (!existing.getUserId().equals(currentUserId)) throw new AccessDeniedException("無權限操作此資源");
        commentRepository.updateComment(commentId, content);
    }

    // 刪除留言，先驗證擁有者
    @Transactional
    public void deleteComment(Long commentId, Long currentUserId) {
        Comment existing = commentRepository.findCommentById(commentId);
        if (existing == null) throw new NoSuchElementException("留言不存在");
        if (!existing.getUserId().equals(currentUserId)) throw new AccessDeniedException("無權限操作此資源");
        commentRepository.deleteComment(commentId);
    }
}

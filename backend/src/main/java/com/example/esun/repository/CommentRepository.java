package com.example.esun.repository;

import com.example.esun.model.Comment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

// 兩種 RowMapper：commentMapper 含 JOIN User（列表用），commentOwnerMapper 只取 UserID（擁有者驗證用）
@Repository
public class CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public CommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 完整 RowMapper：含留言者姓名（JOIN User 查詢用）
    private final RowMapper<Comment> commentMapper = new RowMapper<>() {
        @Override
        public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Comment comment = new Comment();
            comment.setCommentId(rs.getLong("CommentID"));
            comment.setUserId(rs.getLong("UserID"));
            comment.setUserName(rs.getString("UserName"));
            comment.setPostId(rs.getLong("PostID"));
            comment.setContent(rs.getString("Content"));
            comment.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
            return comment;
        }
    };

    // 精簡 RowMapper：只需 UserID，用於擁有者驗證
    private final RowMapper<Comment> commentOwnerMapper = new RowMapper<>() {
        @Override
        public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Comment comment = new Comment();
            comment.setCommentId(rs.getLong("CommentID"));
            comment.setUserId(rs.getLong("UserID"));
            comment.setPostId(rs.getLong("PostID"));
            comment.setContent(rs.getString("Content"));
            comment.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
            return comment;
        }
    };

    // 新增留言
    public void createComment(Comment comment) {
        jdbcTemplate.update("CALL sp_create_comment(?, ?, ?)",
                comment.getUserId(), comment.getPostId(), comment.getContent());
    }

    // 取得指定貼文的所有留言（依時間升序，含 JOIN User 的留言者姓名）
    public List<Comment> findCommentsByPostId(Long postId) {
        return jdbcTemplate.query("CALL sp_get_comments_by_post(?)", commentMapper, postId);
    }

    // 依 commentId 查詢單一留言（供擁有者驗證使用）
    public Comment findCommentById(Long commentId) {
        return jdbcTemplate.query("CALL sp_get_comment_by_id(?)", commentOwnerMapper, commentId)
                .stream().findFirst().orElse(null);
    }

    // 更新留言內容
    public void updateComment(Long commentId, String content) {
        jdbcTemplate.update("CALL sp_update_comment(?, ?)", commentId, content);
    }

    // 刪除留言
    public void deleteComment(Long commentId) {
        jdbcTemplate.update("CALL sp_delete_comment(?)", commentId);
    }
}

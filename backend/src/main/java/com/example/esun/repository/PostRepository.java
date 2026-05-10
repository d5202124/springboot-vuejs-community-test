package com.example.esun.repository;

import com.example.esun.model.Post;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

// 兩種 RowMapper：postMapper 含 JOIN User 欄位（列表用），postOwnerMapper 只取 UserID（擁有者驗證用）
@Repository
public class PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public PostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 完整 RowMapper：含發文者姓名與頭像（JOIN User 查詢用）
    private final RowMapper<Post> postMapper = new RowMapper<>() {
        @Override
        public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
            Post post = new Post();
            post.setPostId(rs.getLong("PostID"));
            post.setUserId(rs.getLong("UserID"));
            post.setUserName(rs.getString("UserName"));
            post.setCoverImage(rs.getString("CoverImage"));
            post.setContent(rs.getString("Content"));
            post.setImage(rs.getString("Image"));
            post.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
            return post;
        }
    };

    // 精簡 RowMapper：只需 UserID，用於擁有者驗證（不需 JOIN User）
    private final RowMapper<Post> postOwnerMapper = new RowMapper<>() {
        @Override
        public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
            Post post = new Post();
            post.setPostId(rs.getLong("PostID"));
            post.setUserId(rs.getLong("UserID"));
            post.setContent(rs.getString("Content"));
            post.setImage(rs.getString("Image"));
            post.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
            return post;
        }
    };

    // 依 postId 查詢單一貼文（供擁有者驗證使用）
    public Post findPostById(Long postId) {
        return jdbcTemplate.query("CALL sp_get_post_by_id(?)", postOwnerMapper, postId)
                .stream().findFirst().orElse(null);
    }

    // 新增貼文
    public void createPost(Post post) {
        jdbcTemplate.update("CALL sp_create_post(?, ?, ?)",
                post.getUserId(), post.getContent(), post.getImage());
    }

    // 取得所有貼文（依時間倒序，含 JOIN User 的發文者資訊）
    public List<Post> findAllPosts() {
        return jdbcTemplate.query("CALL sp_get_posts()", postMapper);
    }

    // 更新貼文內容與圖片
    public void updatePost(Post post) {
        jdbcTemplate.update("CALL sp_update_post(?, ?, ?)",
                post.getPostId(), post.getContent(), post.getImage());
    }

    // 刪除貼文（資料庫有設 CASCADE，底下的留言也會一併刪除）
    public void deletePost(Long postId) {
        jdbcTemplate.update("CALL sp_delete_post(?)", postId);
    }
}

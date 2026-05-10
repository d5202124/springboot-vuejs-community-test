USE esun_db;

DROP PROCEDURE IF EXISTS sp_create_user;
DELIMITER $$
CREATE PROCEDURE sp_create_user(
  IN p_userName VARCHAR(100),
  IN p_phone VARCHAR(255),
  IN p_password VARCHAR(512),
  IN p_coverImage VARCHAR(512),
  IN p_biography TEXT
)
BEGIN
  INSERT INTO User(UserName, Phone, Password, CoverImage, Biography)
  VALUES(p_userName, p_phone, p_password, p_coverImage, p_biography);
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_find_user_by_phone;
DELIMITER $$
CREATE PROCEDURE sp_find_user_by_phone(
  IN p_phone VARCHAR(255)
)
BEGIN
  SELECT UserID, UserName, Phone, Password, CoverImage, Biography
  FROM User
  WHERE Phone = p_phone;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_create_post;
DELIMITER $$
CREATE PROCEDURE sp_create_post(
  IN p_userId BIGINT,
  IN p_content TEXT,
  IN p_image VARCHAR(512)
)
BEGIN
  INSERT INTO Post(UserID, Content, Image)
  VALUES(p_userId, p_content, p_image);
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_get_posts;
DELIMITER $$
CREATE PROCEDURE sp_get_posts()
BEGIN
  SELECT p.PostID, p.UserID, u.UserName, u.CoverImage, p.Content, p.Image, p.CreatedAt
  FROM Post p
  JOIN User u ON p.UserID = u.UserID
  ORDER BY p.CreatedAt DESC;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_post;
DELIMITER $$
CREATE PROCEDURE sp_update_post(
  IN p_postId BIGINT,
  IN p_content TEXT,
  IN p_image VARCHAR(512)
)
BEGIN
  UPDATE Post SET Content = p_content, Image = p_image WHERE PostID = p_postId;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_delete_post;
DELIMITER $$
CREATE PROCEDURE sp_delete_post(
  IN p_postId BIGINT
)
BEGIN
  DELETE FROM Post WHERE PostID = p_postId;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_create_comment;
DELIMITER $$
CREATE PROCEDURE sp_create_comment(
  IN p_userId BIGINT,
  IN p_postId BIGINT,
  IN p_content TEXT
)
BEGIN
  INSERT INTO Comment(UserID, PostID, Content)
  VALUES(p_userId, p_postId, p_content);
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_get_comments_by_post;
DELIMITER $$
CREATE PROCEDURE sp_get_comments_by_post(
  IN p_postId BIGINT
)
BEGIN
  SELECT c.CommentID, c.UserID, u.UserName, c.PostID, c.Content, c.CreatedAt
  FROM Comment c
  JOIN User u ON c.UserID = u.UserID
  WHERE c.PostID = p_postId
  ORDER BY c.CreatedAt ASC;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_get_comment_by_id;
DELIMITER $$
CREATE PROCEDURE sp_get_comment_by_id(
  IN p_commentId BIGINT
)
BEGIN
  SELECT CommentID, UserID, PostID, Content, CreatedAt
  FROM Comment
  WHERE CommentID = p_commentId;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_comment;
DELIMITER $$
CREATE PROCEDURE sp_update_comment(
  IN p_commentId BIGINT,
  IN p_content TEXT
)
BEGIN
  UPDATE Comment SET Content = p_content WHERE CommentID = p_commentId;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_delete_comment;
DELIMITER $$
CREATE PROCEDURE sp_delete_comment(
  IN p_commentId BIGINT
)
BEGIN
  DELETE FROM Comment WHERE CommentID = p_commentId;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_get_user_by_id;
DELIMITER $$
CREATE PROCEDURE sp_get_user_by_id(IN p_userId BIGINT)
BEGIN
  SELECT UserID, UserName, Phone, Password, CoverImage, Biography
  FROM User WHERE UserID = p_userId;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_update_user;
DELIMITER $$
CREATE PROCEDURE sp_update_user(
  IN p_userId BIGINT,
  IN p_userName VARCHAR(100),
  IN p_coverImage VARCHAR(512),
  IN p_biography TEXT
)
BEGIN
  UPDATE User SET UserName = p_userName, CoverImage = p_coverImage, Biography = p_biography
  WHERE UserID = p_userId;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_get_post_by_id;
DELIMITER $$
CREATE PROCEDURE sp_get_post_by_id(
  IN p_postId BIGINT
)
BEGIN
  SELECT PostID, UserID, Content, Image, CreatedAt
  FROM Post
  WHERE PostID = p_postId;
END$$
DELIMITER ;


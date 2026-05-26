package database;

import dto.entity.CommentEntity;

import java.sql.*;

public class CommentQueries {
    public static boolean isCommentExists(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM wp_comments WHERE comment_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection()){
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    public static int getCommentsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM wp_comments";
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }
    public static CommentEntity getCommentById(int commentId) throws SQLException{
        String sql = "SELECT comment_post_ID, comment_content FROM wp_comments WHERE comment_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection()){
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, commentId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return CommentEntity.builder()
                        .post(rs.getInt("comment_post_ID"))
                        .content(rs.getString("comment_content"))
                        .build();
            }
            return null;
        }
    }

    public static void deleteCommentById(Integer id) throws SQLException{
        String sql = "DELETE FROM wp_comments WHERE comment_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection()){
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public static int createCommentInDb(int postId, String content) throws SQLException {
        String sql = """
        INSERT INTO wp_comments (
            comment_post_ID,
            comment_author,
            comment_author_email,
            comment_author_url,
            comment_author_IP,
            comment_date,
            comment_date_gmt,
            comment_content,
            comment_karma,
            comment_approved,
            comment_agent,
            comment_type,
            comment_parent,
            user_id
        )
        VALUES (?, '', '', '', '', NOW(), NOW(), ?, 0, '1', '', 'comment', 0, 1)
        """;
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, postId);
            statement.setString(2, content);
            statement.executeUpdate();

            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Не удалось получить ID созданного комментария");
        }
    }
}

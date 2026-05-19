package database;

import dto.entity.CommentEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}

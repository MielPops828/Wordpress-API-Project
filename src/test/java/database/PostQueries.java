package database;

import dto.entity.PostEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostQueries {
    public static boolean isPostExists(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM wp_posts WHERE ID = ?";
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

    public static int getPostsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM wp_posts";
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public static PostEntity getPostById(int postId) throws SQLException{
        String sql = "SELECT post_title, post_content, post_status FROM wp_posts WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection()){
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, postId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return PostEntity.builder()
                        .title(rs.getString("post_title"))
                        .content(rs.getString("post_content"))
                        .status(rs.getString("post_status"))
                        .build();
            }
            return null;
        }
    }
}

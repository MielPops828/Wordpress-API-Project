package database;

import dto.entity.PostEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public static void deletePostById(Integer id) throws SQLException{
        String sql = "DELETE FROM wp_posts WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection()){
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public static void movePostToTrash(Integer id) throws SQLException {
        String sql = "UPDATE wp_posts SET post_status = 'trash' WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public static int createPostInDb(String title, String content, String status) throws SQLException {
        String sql = """
        INSERT INTO wp_posts (
            post_author,
            post_date,
            post_date_gmt,
            post_content,
            post_title,
            post_excerpt,
            post_status,
            comment_status,
            ping_status,
            post_password,
            post_name,
            to_ping,
            pinged,
            post_modified,
            post_modified_gmt,
            post_content_filtered,
            post_parent,
            guid,
            menu_order,
            post_type,
            post_mime_type,
            comment_count
        )
        VALUES (
            ?,
            NOW(),
            NOW(),
            ?,
            ?,
            '',
            ?,
            'open',
            'open',
            '',
            ?,
            '',
            '',
            NOW(),
            NOW(),
            '',
            0,
            '',
            0,
            'post',
            '',
            0
        )
        """;
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            String slug = title
                    .toLowerCase()
                    .replace(" ", "-");
            statement.setInt(1, 1);
            statement.setString(2, content);
            statement.setString(3, title);
            statement.setString(4, status);
            statement.setString(5, slug);
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Не удалось получить ID созданного поста");
        }
    }

    public static List<Integer> createPostsInDb(int count, String titlePrefix, String content, String status) throws SQLException {
        List<Integer> postIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String uniqueTitle = titlePrefix + "_" + System.currentTimeMillis() + "_" + i;
            int postId = createPostInDb(uniqueTitle, content, status);
            postIds.add(postId);
        }
        return postIds;
    }
}

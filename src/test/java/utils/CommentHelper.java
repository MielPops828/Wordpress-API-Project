package utils;

import database.CommentQueries;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommentHelper {
    public static List<Integer> createCommentsForPosts(List<Integer> postIds, String content) throws SQLException {
        List<Integer> commentIds = new ArrayList<>();
        for (Integer postId : postIds) {
            int commentId = CommentQueries.createCommentInDb(postId, content);
            commentIds.add(commentId);
        }
        return commentIds;
    }
}

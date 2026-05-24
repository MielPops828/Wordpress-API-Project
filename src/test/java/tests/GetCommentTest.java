package tests;

import database.CommentQueries;
import database.PostQueries;
import dto.entity.CommentEntity;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

@Epic("Wordpress API Tests")
@Feature("Получение комментария и списка комментариев")
public class GetCommentTest extends BaseTest{
    @Test
    @Description("Проверка успешного получения комментария по id")
    @Severity(SeverityLevel.NORMAL)
    public void testGetComment() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostQueries.createPostInDb("API_TEST_POST", "Some content", "publish");
        int commentId = CommentQueries.createCommentInDb(postId, "Test comment");
        createdPostIds.add(postId);
        createdCommentIds.add(commentId);
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .pathParam("id", commentId)
                .get("?rest_route=/wp/v2/comments/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
        CommentEntity comment = CommentQueries.getCommentById(commentId);
        softAssert.assertNotNull(comment);
        softAssert.assertEquals(commentId, response.jsonPath().getInt("id"));
        softAssert.assertEquals(comment.getPost(), postId);
        softAssert.assertTrue(response.jsonPath().getString("content.rendered").contains(comment.getContent()));
        softAssert.assertAll();
    }

    @Test
    @Description("Проверка успешного получения списка комментариев")
    @Severity(SeverityLevel.NORMAL)
    public void testGetListComments() throws SQLException {
        SoftAssert softAssert = new SoftAssert();

        List<Integer> postIds = PostQueries.createPostsInDb(config.createPostsCount(), "Test post", "Some content", "publish");
        createdPostIds.addAll(postIds);
        List<Integer> commentIds = new ArrayList<>();
        for (Integer postId : postIds) {
            int commentId = CommentQueries.createCommentInDb(postId, "Test comment");
            commentIds.add(commentId);
        }
        createdCommentIds.addAll(commentIds);
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .queryParam("per_page", config.getPerPage())
                .get("?rest_route=/wp/v2/comments")
                .then()
                .statusCode(200)
                .extract()
                .response();
        List<Integer> responseCommentIds = response.jsonPath().getList("id", Integer.class);

        for (Integer createdId : commentIds) {
            softAssert.assertTrue(responseCommentIds.contains(createdId), "Комментарий с id " + createdId + " отсутствует в списке");
        }
        softAssert.assertAll();
    }

    @Test
    @Description("Проверка получения комментария с указанием несуществующего id")
    @Severity(SeverityLevel.NORMAL)
    public void testGetCommentNonExistsId() {
        SoftAssert softAssert = new SoftAssert();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .pathParam("id", config.getCommentNonExistsId())
                .get("?rest_route=/wp/v2/comments/{id}")
                .then()
                .statusCode(404)
                .extract()
                .response();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_comment_invalid_id", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Неверный ID комментария.", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertAll();
    }

    @Test
    @Description("Проверка получения комментария с указанием невалидного id")
    @Severity(SeverityLevel.NORMAL)
    public void testGetCommentInvalidId() {
        SoftAssert softAssert = new SoftAssert();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .pathParam("id", config.getCommentInvalidId())
                .get("?rest_route=/wp/v2/comments/{id}")
                .then()
                .statusCode(404)
                .extract()
                .response();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_no_route", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Подходящий маршрут для URL и метода запроса не найден.", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertAll();
    }

    @Test
    @Description("Проверка получения существующего комментария с указанием id без авторизации")
    @Severity(SeverityLevel.NORMAL)
    public void testGetCommentNoAuth() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostQueries.createPostInDb("API_TEST_POST", "Some content", "publish");
        int commentId = CommentQueries.createCommentInDb(postId, "Test comment");
        createdPostIds.add(postId);
        createdCommentIds.add(commentId);
        Response response = given(spec)
                .pathParam("id", commentId)
                .get("?rest_route=/wp/v2/comments/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
        CommentEntity comment = CommentQueries.getCommentById(commentId);
        softAssert.assertNotNull(comment);
        softAssert.assertEquals(commentId, response.jsonPath().getInt("id"));
        softAssert.assertEquals(comment.getPost(), postId);
        softAssert.assertTrue(response.jsonPath().getString("content.rendered").contains(comment.getContent()));
        softAssert.assertAll();
    }

    @Test
    @Description("Проверка получения комментария c удаленного поста без авторизации")
    @Severity(SeverityLevel.NORMAL)
    public void testGetCommentNoAuthWithDeletedPost() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostQueries.createPostInDb("API_TEST_POST", "Some content", "publish");
        int commentId = CommentQueries.createCommentInDb(postId, "Test comment");
        createdPostIds.add(postId);
        createdCommentIds.add(commentId);
        PostQueries.movePostToTrash(postId);
        Response response = given(spec)
                .pathParam("id", commentId)
                .get("?rest_route=/wp/v2/comments/{id}")
                .then()
                .statusCode(401)
                .extract()
                .response();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_cannot_read", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Извините, вам не разрешено прочитать этот комментарий.", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertAll();
    }

    @Test
    @Description("Проверка получения списка комментариев без авторизации")
    @Severity(SeverityLevel.NORMAL)
    public void testGetListCommentsNoAuth() throws SQLException {
        SoftAssert softAssert = new SoftAssert();

        List<Integer> postIds = PostQueries.createPostsInDb(config.createPostsCount(), "Test post", "Some content", "publish");
        createdPostIds.addAll(postIds);
        List<Integer> commentIds = new ArrayList<>();
        for (Integer postId : postIds) {
            int commentId = CommentQueries.createCommentInDb(postId, "Test comment");
            commentIds.add(commentId);
        }
        createdCommentIds.addAll(commentIds);
        Response response = given(spec)
                .queryParam("per_page", config.getPerPage())
                .get("?rest_route=/wp/v2/comments")
                .then()
                .statusCode(200)
                .extract()
                .response();
        List<Integer> responseCommentIds = response.jsonPath().getList("id", Integer.class);

        for (Integer createdId : commentIds) {
            softAssert.assertTrue(responseCommentIds.contains(createdId), "Комментарий с id " + createdId + " отсутствует в списке");
        }
        softAssert.assertAll();
    }
}

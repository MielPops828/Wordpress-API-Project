package tests;

import database.CommentQueries;
import dto.entity.CommentEntity;
import dto.request.CommentRequest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.PostSteps;

import java.sql.SQLException;

import static io.restassured.RestAssured.given;

@Epic("WordPress-API-Test")
@Feature("Создание комментария к существующему посту")
public class CreateCommentTest extends BaseTest{
    @Test
    @Description("Создание нового комментария к существующему посту")
    @Severity(SeverityLevel.CRITICAL)
    public void testSuccessCreateComment() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        CommentRequest commentRequest = CommentRequest.builder()
                .post(postId)
                .content("Some comment")
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(commentRequest)
                .post("?rest_route=/wp/v2/comments")
                .then()
                .statusCode(201)
                .extract()
                .response();
        int commentId = response.jsonPath().getInt("id");
        String commentContent = response.jsonPath().getString("content.raw");
        CommentEntity commentEntity = CommentQueries.getCommentById(commentId);
        boolean isCommentCreated = CommentQueries.isCommentExists(commentId);
        softAssert.assertTrue(isCommentCreated, "Комментарий не был создан");
        softAssert.assertNotNull(commentEntity);
        softAssert.assertEquals(commentEntity.getPost(), postId);
        softAssert.assertEquals(commentEntity.getContent(), commentContent);
        softAssert.assertAll();
    }

    @Test
    @Description("Создание комментария к посту с указанием несуществующего id поста")
    @Severity(SeverityLevel.CRITICAL)
    public void testNotExistsIdCreateComment() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int countCommentsBefore = CommentQueries.getCommentsCount();
        CommentRequest commentRequest = CommentRequest.builder()
                .post(config.getCommentNonExistsId())
                .content("Some comment")
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(commentRequest)
                .post("?rest_route=/wp/v2/comments")
                .then()
                .statusCode(403)
                .extract()
                .response();
        int countCommentsAfter = CommentQueries.getCommentsCount();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_comment_invalid_post_id", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Извините, вам не разрешено создать этот комментарий без записи.", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertTrue(countCommentsBefore == countCommentsAfter, "Запись о данном комментарии базе данных была создана");
        softAssert.assertAll();
    }

    @Test
    @Description("Создание комментария к посту с указанием невалидного id поста")
    @Severity(SeverityLevel.CRITICAL)
    public void testInvalidIdCreateComment() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int countCommentsBefore = CommentQueries.getCommentsCount();
        CommentRequest commentRequest = CommentRequest.builder()
                .post(config.getCommentInvalidId())
                .content("Some comment")
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(commentRequest)
                .post("?rest_route=/wp/v2/comments")
                .then()
                .statusCode(400)
                .extract()
                .response();
        int countCommentsAfter = CommentQueries.getCommentsCount();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_invalid_param", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Неверный параметр: post", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertTrue(countCommentsBefore == countCommentsAfter, "Запись о данном комментарии базе данных была создана");
        softAssert.assertAll();
    }

    @Test
    @Description("Создание комментария к существующему посту без авторизации")
    @Severity(SeverityLevel.CRITICAL)
    public void testNoAuthCreateComment() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        int countCommentsBefore = CommentQueries.getCommentsCount();
        CommentRequest commentRequest = CommentRequest.builder()
                .post(postId)
                .content("Some comment")
                .build();
        Response response = given(spec)
                .body(commentRequest)
                .post("?rest_route=/wp/v2/comments")
                .then()
                .statusCode(401)
                .extract()
                .response();
        int countCommentsAfter = CommentQueries.getCommentsCount();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_comment_login_required", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Извините, для отправки комментария необходимо авторизоваться.", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertTrue(countCommentsBefore == countCommentsAfter, "Запись о данном комментарии базе данных была создана");
        softAssert.assertAll();
    }
}

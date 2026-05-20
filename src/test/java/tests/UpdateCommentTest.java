package tests;

import database.CommentQueries;
import dto.entity.CommentEntity;
import dto.request.CommentRequest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.CommentSteps;
import utils.PostSteps;

import java.sql.SQLException;

import static io.restassured.RestAssured.given;

@Epic("WordPress API Tests")
@Feature("Изменение комментария к посту")
public class UpdateCommentTest extends BaseTest{
    @Test
    @Description("Изменение существующего комментария к посту")
    @Severity(SeverityLevel.NORMAL)
    public void testSuccessUpdateComment() throws SQLException {
        int postId = PostSteps.createPost(spec, config);
        int commentId = CommentSteps.createComment(postId, spec, config);
        CommentRequest commentRequest = CommentRequest.builder()
                .content("Update comment")
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(commentRequest)
                .pathParam("id", commentId)
                .post("?rest_route=/wp/v2/comments/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
        String requestContent = response.jsonPath().getString("content.raw");
        CommentEntity commentIdAfter = CommentQueries.getCommentById(commentId);
        Assert.assertNotNull(commentIdAfter);
        Assert.assertEquals(commentIdAfter.getContent(), requestContent, "Изменение не было внесено");
    }

    @Test
    @Description("Изменение комментария к посту с указанием несуществующего id")
    @Severity(SeverityLevel.NORMAL)
    public void testNotExistsIdUpdateComment() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        int commentId = CommentSteps.createComment(postId, spec, config);
        CommentEntity commentEntityBefore = CommentQueries.getCommentById(commentId);
        CommentRequest commentRequest = CommentRequest.builder()
                .content("Update comment")
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(commentRequest)
                .pathParam("id", config.getCommentNonExistsId())
                .post("?rest_route=/wp/v2/comments/{id}")
                .then()
                .statusCode(404)
                .extract()
                .response();
        CommentEntity commentEntityAfter = CommentQueries.getCommentById(commentId);

        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_comment_invalid_id", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Неверный ID комментария.", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertNotNull(commentEntityBefore);
        softAssert.assertNotNull(commentEntityAfter);
        softAssert.assertEquals(commentEntityBefore.getContent(), commentEntityAfter.getContent(), "Изменения были внесены в запись");
        softAssert.assertAll();
    }

    @Test
    @Description("Изменение комментария к посту с указание невалидного id комментария")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidIdUpdateComment() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        int commentId = CommentSteps.createComment(postId, spec, config);
        CommentEntity commentEntityBefore = CommentQueries.getCommentById(commentId);
        CommentRequest commentRequest = CommentRequest.builder()
                .content("Update comment")
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(commentRequest)
                .pathParam("id", config.getCommentInvalidId())
                .post("?rest_route=/wp/v2/comments/{id}")
                .then()
                .statusCode(404)
                .extract()
                .response();
        CommentEntity commentEntityAfter = CommentQueries.getCommentById(commentId);

        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_no_route", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Подходящий маршрут для URL и метода запроса не найден.", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertNotNull(commentEntityBefore);
        softAssert.assertNotNull(commentEntityAfter);
        softAssert.assertEquals(commentEntityBefore.getContent(), commentEntityAfter.getContent(), "Изменения были внесены в запись");
        softAssert.assertAll();
    }

    @Test
    @Description("Изменение комментария к посту без авторизации")
    @Severity(SeverityLevel.NORMAL)
    public void testNoAuthUpdateComment() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        int commentId = CommentSteps.createComment(postId, spec, config);
        CommentEntity commentEntityBefore = CommentQueries.getCommentById(commentId);
        CommentRequest commentRequest = CommentRequest.builder()
                .content("Update comment")
                .build();
        Response response = given(spec)
                .body(commentRequest)
                .pathParam("id", commentId)
                .post("?rest_route=/wp/v2/comments/{id}")
                .then()
                .statusCode(401)
                .extract()
                .response();
        CommentEntity commentEntityAfter = CommentQueries.getCommentById(commentId);

        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_cannot_edit", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Извините, вам не разрешено редактировать этот комментарий.", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertNotNull(commentEntityBefore);
        softAssert.assertNotNull(commentEntityAfter);
        softAssert.assertEquals(commentEntityBefore.getContent(), commentEntityAfter.getContent(), "Изменения были внесены в запись");
        softAssert.assertAll();
    }
}

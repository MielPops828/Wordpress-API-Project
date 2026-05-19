package tests;

import database.CommentQueries;
import dto.request.CommentRequest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.CommentSteps;
import utils.PostSteps;

import java.sql.SQLException;

import static io.restassured.RestAssured.given;

@Epic("WordPress API Tests")
@Feature("Удаление комментария к посту")
public class DeleteCommentTest extends BaseTest{
    @Test
    @Description("Удаление комментария к существующему посту")
    @Severity(SeverityLevel.NORMAL)
    public void testSuccessDeleteComment() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        int commentId = CommentSteps.createComment(postId, spec, config);
        softAssert.assertTrue(CommentQueries.isCommentExists(commentId), "Запись о комментарии не была создана");
        CommentRequest commentRequest = CommentRequest.builder()
                .force(true)
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(commentRequest)
                .pathParam("id", commentId)
                .delete("?rest_route=/wp/v2/comments/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
        int deleteCommentId = response.jsonPath().getInt("previous.id");
        softAssert.assertEquals(commentId, deleteCommentId);
        softAssert.assertFalse(CommentQueries.isCommentExists(deleteCommentId), "Запись о комментарии не была удалена");
        softAssert.assertAll();
    }

    @Test
    @Description("Удаление комментария к посту с указанием несуществующего id")
    @Severity(SeverityLevel.NORMAL)
    public void testNotExistsIdDeleteComment() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        int commentId = CommentSteps.createComment(postId, spec, config);
        softAssert.assertTrue(CommentQueries.isCommentExists(commentId), "Запись о комментарии не была создана");
        CommentRequest commentRequest = CommentRequest.builder()
                .force(true)
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(commentRequest)
                .pathParam("id", config.getNonExistsIdDelete())
                .delete("?rest_route=/wp/v2/comments/{id}")
                .then()
                .statusCode(404)
                .extract()
                .response();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_comment_invalid_id", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Неверный ID комментария.", "Полученный текст сообщения не соответствует ожидаемому");

        softAssert.assertTrue(CommentQueries.isCommentExists(commentId), "Запись о комментарии была удалена");
        softAssert.assertAll();
    }

    @Test
    @Description("Удаление комментария к посту с указанием невалидного id комментария")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidIdDeleteComment() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        int commentId = CommentSteps.createComment(postId, spec, config);
        softAssert.assertTrue(CommentQueries.isCommentExists(commentId), "Запись о комментарии не была создана");
        CommentRequest commentRequest = CommentRequest.builder()
                .force(true)
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(commentRequest)
                .pathParam("id", config.getInvalidIdDelete())
                .delete("?rest_route=/wp/v2/comments/{id}")
                .then()
                .statusCode(404)
                .extract()
                .response();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_no_route", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Подходящий маршрут для URL и метода запроса не найден.", "Полученный текст сообщения не соответствует ожидаемому");

        softAssert.assertTrue(CommentQueries.isCommentExists(commentId), "Запись о комментарии была удалена");
        softAssert.assertAll();
    }

    @Test
    @Description("Удаление комментария без авторизации")
    @Severity(SeverityLevel.NORMAL)
    public void testNoAuthDeleteComment() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        int commentId = CommentSteps.createComment(postId, spec, config);
        softAssert.assertTrue(CommentQueries.isCommentExists(commentId), "Запись о комментарии не была создана");
        CommentRequest commentRequest = CommentRequest.builder()
                .force(true)
                .build();
        Response response = given(spec)
                .body(commentRequest)
                .pathParam("id", commentId)
                .delete("?rest_route=/wp/v2/comments/{id}")
                .then()
                .statusCode(401)
                .extract()
                .response();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_cannot_delete", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Извините, вам не разрешено удалить этот комментарий.", "Полученный текст сообщения не соответствует ожидаемому");

        softAssert.assertTrue(CommentQueries.isCommentExists(commentId), "Запись о комментарии была удалена");
        softAssert.assertAll();
    }
}

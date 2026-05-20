package tests;

import database.PostQueries;
import dto.request.PostRequest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.PostSteps;

import java.sql.SQLException;

import static io.restassured.RestAssured.given;

@Epic("WordPress API Tests")
@Feature("Удаление поста")
public class DeletePostTest extends BaseTest{
    @Test
    @Description("Удаление существующего поста")
    @Severity(SeverityLevel.NORMAL)
    public void testSuccessDeletePost() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        softAssert.assertTrue(PostQueries.isPostExists(postId), "Запись о посте не была создана");
        PostRequest postRequest = PostRequest.builder()
                .force(true)
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(postRequest)
                .pathParam("id", postId)
                .delete("?rest_route=/wp/v2/posts/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
        int deletePostId = response.jsonPath().getInt("previous.id");
        softAssert.assertEquals(postId, deletePostId);
        softAssert.assertFalse(PostQueries.isPostExists(postId), "Запись о посте не была удалена");
        softAssert.assertAll();
    }

    @Test
    @Description("Удаление поста с указанием несуществующего id поста")
    @Severity(SeverityLevel.NORMAL)
    public void testNotExistsIdDeletePost() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        softAssert.assertTrue(PostQueries.isPostExists(postId), "Запись о посте не была создана");
        PostRequest postRequest = PostRequest.builder()
                .force(true)
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(postRequest)
                .pathParam("id", config.getNonExistsIdDelete())
                .delete("?rest_route=/wp/v2/posts/{id}")
                .then()
                .statusCode(404)
                .extract()
                .response();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_post_invalid_id", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Неверный ID записи.", "Полученный текст сообщения не соответствует ожидаемому");

        softAssert.assertTrue(PostQueries.isPostExists(postId), "Запись о посте была удалена");
        softAssert.assertAll();
    }

    @Test
    @Description("Удаление поста с указанием невалидного id")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidIdDeletePost() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        softAssert.assertTrue(PostQueries.isPostExists(postId), "Запись о посте не была создана");
        PostRequest postRequest = PostRequest.builder()
                .force(true)
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(postRequest)
                .pathParam("id", config.getInvalidIdDelete())
                .delete("?rest_route=/wp/v2/posts/{id}")
                .then()
                .statusCode(404)
                .extract()
                .response();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_no_route", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Подходящий маршрут для URL и метода запроса не найден.", "Полученный текст сообщения не соответствует ожидаемому");

        softAssert.assertTrue(PostQueries.isPostExists(postId), "Запись о посте была удалена");
        softAssert.assertAll();
    }

    @Test
    @Description("Удаление поста без авторизации")
    @Severity(SeverityLevel.NORMAL)
    public void testNoAuthDeletePost() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        softAssert.assertTrue(PostQueries.isPostExists(postId), "Запись о посте не была создана");
        PostRequest postRequest = PostRequest.builder()
                .force(true)
                .build();
        Response response = given(spec)
                .body(postRequest)
                .pathParam("id", postId)
                .delete("?rest_route=/wp/v2/posts/{id}")
                .then()
                .statusCode(401)
                .extract()
                .response();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_cannot_delete", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Извините, вам не разрешено удалять эту запись.", "Полученный текст сообщения не соответствует ожидаемому");

        softAssert.assertTrue(PostQueries.isPostExists(postId), "Запись о посте была удалена");
        softAssert.assertAll();
    }
}

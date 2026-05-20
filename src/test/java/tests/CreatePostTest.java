package tests;

import database.PostQueries;
import dto.entity.PostEntity;
import dto.request.PostRequest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.sql.SQLException;

import static io.restassured.RestAssured.given;

@Epic("WordPress API Tests")
@Feature("Создание поста")
public class CreatePostTest extends BaseTest{
    @Test
    @Description("Создание нового поста")
    @Severity(SeverityLevel.CRITICAL)
    public void testSuccessCreatePost() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        PostRequest postRequest = PostRequest.builder()
                .title("First post test")
                .content("Some content")
                .status("publish")
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(postRequest)
                .post("?rest_route=/wp/v2/posts")
                .then()
                .statusCode(201)
                .extract()
                .response();
        int postId = response.jsonPath().getInt("id");
        boolean isPostCreated = PostQueries.isPostExists(postId);
        PostEntity postEntity = PostQueries.getPostById(postId);
        String postTitle = response.jsonPath().getString("title.raw");
        String postContent = response.jsonPath().getString("content.raw");
        String postStatus = response.jsonPath().getString("status");
        softAssert.assertTrue(isPostCreated, "Пост не был создан");
        Assert.assertNotNull(postEntity);
        softAssert.assertEquals(postEntity.getTitle(), postTitle);
        softAssert.assertEquals(postEntity.getContent(), postContent);
        softAssert.assertEquals(postEntity.getStatus(), postStatus);
        softAssert.assertAll();
    }

    @Test
    @Description("Создание нового поста с указание невалидного статуса")
    @Severity(SeverityLevel.CRITICAL)
    public void testInvalidStatusCreatePost() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postCountBefore = PostQueries.getPostsCount();
        PostRequest postRequest = PostRequest.builder()
                .title("First post test")
                .content("Some content")
                .status("abc")
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(postRequest)
                .post("?rest_route=/wp/v2/posts")
                .then()
                .statusCode(400)
                .extract()
                .response();
        int postCountAfter = PostQueries.getPostsCount();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_invalid_param", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Неверный параметр: status", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertTrue(postCountBefore == postCountAfter, "Запись о данном посте базе данных была создана");
        softAssert.assertAll();
    }

    @Test
    @Description("Создание нового поста без авторизации")
    @Severity(SeverityLevel.CRITICAL)
    public void testNoAuthCreatePost() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postCountBefore = PostQueries.getPostsCount();
        PostRequest postRequest = PostRequest.builder()
                .title("First post test")
                .content("Some content")
                .status("publish")
                .build();
        Response response = given(spec)
                .body(postRequest)
                .post("?rest_route=/wp/v2/posts")
                .then()
                .statusCode(401)
                .extract()
                .response();
        int postCountAfter = PostQueries.getPostsCount();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_cannot_create", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertEquals(message, "Извините, вам не разрешено создавать записи от лица этого пользователя.", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertTrue(postCountBefore == postCountAfter, "Запись о данном посте базе данных была создана");
        softAssert.assertAll();
    }
}

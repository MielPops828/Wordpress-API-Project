package tests;

import database.PostQueries;
import dto.entity.PostEntity;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.sql.SQLException;
import java.util.List;

import static io.restassured.RestAssured.given;

@Epic("Wordpress API Tests")
@Feature("Получение поста и списка постов")
public class GetPostTest extends BaseTest{
    @Test
    @Description("Проверка успешного получения поста по id")
    @Severity(SeverityLevel.NORMAL)
    public void testGetPost() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostQueries.createPostInDb("API_TEST_POST", "Some content", "publish");
        createdPostIds.add(postId);
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .pathParam("id", postId)
                .get("?rest_route=/wp/v2/posts/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
        PostEntity post = PostQueries.getPostById(postId);
        softAssert.assertNotNull(post);
        softAssert.assertEquals(postId, response.jsonPath().getInt("id"));
        softAssert.assertEquals(post.getTitle(), response.jsonPath().getString("title.rendered"));
        softAssert.assertTrue(response.jsonPath().getString("content.rendered").contains(post.getContent()));
        softAssert.assertEquals(post.getStatus(), response.jsonPath().getString("status"));
        softAssert.assertAll();
    }

    @Test
    @Description("Проверка успешного получения списка постов")
    @Severity(SeverityLevel.NORMAL)
    public void testGetListPosts() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        List<Integer> createdIds = PostQueries.createPostsInDb(config.createPostsCount(), "API_TEST_POST", "Some content", "publish");
        createdPostIds.addAll(createdIds);
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .queryParam("per_page", config.getPerPage())
                .get("?rest_route=/wp/v2/posts")
                .then()
                .statusCode(200)
                .extract()
                .response();
        List<Integer> responsePostIds = response.jsonPath().getList("id", Integer.class);
        for (Integer createdId : createdIds) {
            softAssert.assertTrue(responsePostIds.contains(createdId), "Пост с id " + createdId + " отсутствует в списке");
        }
        softAssert.assertAll();
    }

    @Test
    @Description("Проверка получения поста с указанием несуществующего id")
    @Severity(SeverityLevel.NORMAL)
    public void testGetPostNonExistsId() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .pathParam("id", config.getPostNonExistsId())
                .get("?rest_route=/wp/v2/posts/{id}")
                .then()
                .statusCode(404)
                .extract()
                .response();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_post_invalid_id", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Неверный ID записи.", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertAll();
    }

    @Test
    @Description("Проверка получения поста с указанием невалидного id")
    @Severity(SeverityLevel.NORMAL)
    public void testGetPostInvalidId() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .pathParam("id", config.getPostInvalidId())
                .get("?rest_route=/wp/v2/posts/{id}")
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
    @Description("Проверка получения существующего поста с указанием id без авторизации")
    @Severity(SeverityLevel.NORMAL)
    public void testGetPostNoAuth() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostQueries.createPostInDb("API_TEST_POST", "Some content", "publish");
        createdPostIds.add(postId);
        Response response = given(spec)
                .pathParam("id", postId)
                .get("?rest_route=/wp/v2/posts/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
        PostEntity post = PostQueries.getPostById(postId);
        softAssert.assertNotNull(post);
        softAssert.assertEquals(postId, response.jsonPath().getInt("id"));
        softAssert.assertEquals(post.getTitle(), response.jsonPath().getString("title.rendered"));
        softAssert.assertTrue(response.jsonPath().getString("content.rendered").contains(post.getContent()));
        softAssert.assertEquals(post.getStatus(), response.jsonPath().getString("status"));
        softAssert.assertAll();
    }

    @Test
    @Description("Проверка получения удаленного поста с указанием id без авторизации")
    @Severity(SeverityLevel.NORMAL)
    public void testGetDeletedPostNoAuth() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostQueries.createPostInDb("API_TEST_POST", "Some content", "publish");
        createdPostIds.add(postId);
        PostQueries.movePostToTrash(postId);
        Response response = given(spec)
                .pathParam("id", postId)
                .get("?rest_route=/wp/v2/posts/{id}")
                .then()
                .statusCode(401)
                .extract()
                .response();
        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertEquals(code, "rest_forbidden", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Извините, вам не разрешено выполнять данное действие.", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertAll();
    }

    @Test
    @Description("Проверка получения списка постов без авторизации")
    @Severity(SeverityLevel.NORMAL)
    public void testGetListPostsNoAuth() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        List<Integer> createdIds = PostQueries.createPostsInDb(config.createPostsCount(), "API_TEST_POST", "Some content", "publish");
        createdPostIds.addAll(createdIds);
        Response response = given(spec)
                .queryParam("per_page", config.getPerPage())
                .get("?rest_route=/wp/v2/posts")
                .then()
                .statusCode(200)
                .extract()
                .response();
        List<Integer> responsePostIds = response.jsonPath().getList("id", Integer.class);

        for (Integer createdId : createdIds) {
            softAssert.assertTrue(responsePostIds.contains(createdId), "Пост с id " + createdId + " отсутствует в списке");
        }
        softAssert.assertAll();
    }
}

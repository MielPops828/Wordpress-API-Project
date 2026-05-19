package tests;

import database.PostQueries;
import dto.entity.PostEntity;
import dto.request.PostRequest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.PostSteps;

import java.sql.SQLException;

import static io.restassured.RestAssured.given;

@Epic("WordPress API Tests")
@Feature("Изменение существующего поста")
public class UpdatePostTest extends BaseTest{
    @Test
    @Description("Изменение существующего поста с указанием валадных данных")
    @Severity(SeverityLevel.NORMAL)
    public void testSuccessUpdatePost() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        PostRequest postRequest = PostRequest.builder()
                .title(config.getUpdateTitle())
                .content(config.getUpdateContent())
                .status(config.getUpdateValidStatus())
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(postRequest)
                .pathParam("id", postId)
                .post("?rest_route=/wp/v2/posts/{id}")
                .then()
                .statusCode(200)
                .extract()
                .response();
        int postIdAfter = response.jsonPath().getInt("id");
        PostEntity post = PostQueries.getPostById(postIdAfter);
        softAssert.assertNotNull(post);
        softAssert.assertEquals(post.getTitle(), config.getUpdateTitle());
        softAssert.assertEquals(post.getContent(), config.getUpdateContent());
        softAssert.assertEquals(post.getStatus(), config.getUpdateValidStatus());
        softAssert.assertAll();
    }

    @Test
    @Description("Изменение существующего поста с указанием невалидного статуса")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidStatusUpdatePost() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        PostEntity postBefore = PostQueries.getPostById(postId);
        PostRequest postRequest = PostRequest.builder()
                .title(config.getUpdateTitle())
                .content(config.getUpdateContent())
                .status(config.getUpdateInvalidStatus())
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(postRequest)
                .pathParam("id", postId)
                .post("?rest_route=/wp/v2/posts/{id}")
                .then()
                .statusCode(400)
                .extract()
                .response();
        PostEntity postAfter = PostQueries.getPostById(postId);

        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertNotNull(postBefore);
        softAssert.assertNotNull(postAfter);
        softAssert.assertEquals(code, "rest_invalid_param", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Неверный параметр: status", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertEquals(postBefore.getTitle(), postAfter.getTitle());
        softAssert.assertEquals(postBefore.getContent(), postAfter.getContent());
        softAssert.assertEquals(postBefore.getStatus(), postAfter.getStatus());
        softAssert.assertAll();
    }

    @Test
    @Description("Изменение существующего поста с указанием несуществующего id")
    @Severity(SeverityLevel.NORMAL)
    public void testNotExistsIdStatusUpdatePost() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        PostEntity postBefore = PostQueries.getPostById(postId);
        PostRequest postRequest = PostRequest.builder()
                .title(config.getUpdateTitle())
                .content(config.getUpdateContent())
                .status(config.getUpdateValidStatus())
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(config.getUsername(), config.getPassword())
                .body(postRequest)
                .pathParam("id", config.getUpdateNonExistsId())
                .post("?rest_route=/wp/v2/posts/{id}")
                .then()
                .statusCode(404)
                .extract()
                .response();
        PostEntity postAfter = PostQueries.getPostById(postId);

        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertNotNull(postBefore);
        softAssert.assertNotNull(postAfter);
        softAssert.assertEquals(code, "rest_post_invalid_id", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Неверный ID записи.", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertEquals(postBefore.getTitle(), postAfter.getTitle());
        softAssert.assertEquals(postBefore.getContent(), postAfter.getContent());
        softAssert.assertEquals(postBefore.getStatus(), postAfter.getStatus());
        softAssert.assertAll();
    }

    @Test
    @Description("Изменение существующего поста без авторизации")
    @Severity(SeverityLevel.CRITICAL)
    public void testNoAuthStatusUpdatePost() throws SQLException {
        SoftAssert softAssert = new SoftAssert();
        int postId = PostSteps.createPost(spec, config);
        PostEntity postBefore = PostQueries.getPostById(postId);
        PostRequest postRequest = PostRequest.builder()
                .title(config.getUpdateTitle())
                .content(config.getUpdateContent())
                .status(config.getUpdateValidStatus())
                .build();
        Response response = given(spec)
                .body(postRequest)
                .pathParam("id", config.getUpdatePostId())
                .post("?rest_route=/wp/v2/posts/{id}")
                .then()
                .statusCode(401)
                .extract()
                .response();
        PostEntity postAfter = PostQueries.getPostById(postId);

        String code = response.jsonPath().getString("code");
        String message = response.jsonPath().getString("message");
        softAssert.assertNotNull(postBefore);
        softAssert.assertNotNull(postAfter);
        softAssert.assertEquals(code, "rest_cannot_edit", "Полученный код не соответствует ожидаемому");
        softAssert.assertEquals(message, "Извините, вам не разрешено редактировать эту запись.", "Полученный текст сообщения не соответствует ожидаемому");
        softAssert.assertEquals(postBefore.getTitle(), postAfter.getTitle());
        softAssert.assertEquals(postBefore.getContent(), postAfter.getContent());
        softAssert.assertEquals(postBefore.getStatus(), postAfter.getStatus());
        softAssert.assertAll();
    }
}

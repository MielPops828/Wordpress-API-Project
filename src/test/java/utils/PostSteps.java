package utils;

import dto.request.PostRequest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.UUID;

import static io.restassured.RestAssured.given;

public class PostSteps {
    public static int createPost(
            RequestSpecification spec,
            Configuration config
    ) {
        PostRequest request = PostRequest.builder()
                .title("Test " + UUID.randomUUID())
                .content("Some content")
                .status("publish")
                .build();

        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(
                        config.getUsername(),
                        config.getPassword()
                )
                .body(request)
                .post("/index.php?rest_route=/wp/v2/posts");

        return response.jsonPath().getInt("id");
    }
}

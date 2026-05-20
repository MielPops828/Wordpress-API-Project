package utils;

import dto.request.CommentRequest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.UUID;

import static io.restassured.RestAssured.given;

public class CommentSteps {
    public static int createComment (
            int postId,
            RequestSpecification spec,
            Configuration config
    ) {
        CommentRequest request = CommentRequest.builder()
                .post(postId)
                .content("Comment " + UUID.randomUUID())
                .build();
        Response response = given(spec)
                .auth()
                .preemptive()
                .basic(
                        config.getUsername(),
                        config.getPassword()
                )
                .body(request)
                .post("/index.php?rest_route=/wp/v2/comments");
        return response.jsonPath().getInt("id");
    }
}

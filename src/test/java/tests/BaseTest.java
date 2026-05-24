package tests;

import database.CommentQueries;
import database.PostQueries;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import utils.Configuration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseTest {

    protected Configuration config;
    protected RequestSpecification spec;
    protected List<Integer> createdPostIds = new ArrayList<>();
    protected List<Integer> createdCommentIds = new ArrayList<>();

    @BeforeClass
    public void setup(){
        config = ConfigFactory.create(Configuration.class);
        spec = new RequestSpecBuilder()
                .setBaseUri(config.getUrl())
                .setBasePath(config.getPath())
                .setContentType(ContentType.JSON)
                .build();
    }

    @AfterMethod
    public void cleanup() throws SQLException {
        for (Integer id : createdCommentIds){
            CommentQueries.deleteCommentById(id);
        }
        for (Integer id : createdPostIds){
            PostQueries.deletePostById(id);
        }
        createdCommentIds.clear();
        createdPostIds.clear();
    }
}

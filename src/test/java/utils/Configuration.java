package utils;

import org.aeonbits.owner.Config;

@Config.Sources({"classpath:config.properties"})
public interface Configuration extends Config {
    @Key("base.url")
    String getUrl();

    @Key("path")
    String getPath();

    @Key("basic.username")
    String getUsername();

    @Key("basic.password")
    String getPassword();

    @Key("update.title")
    String getUpdateTitle();

    @Key("update.content")
    String getUpdateContent();

    @Key("update.valid.status")
    String getUpdateValidStatus();

    @Key("update.invalid.status")
    String getUpdateInvalidStatus();

    @Key("update.postId")
    int getUpdatePostId();

    @Key("update.non.exists.id")
    int getUpdateNonExistsId();

    @Key("comment.non.exists.id")
    int getCommentNonExistsId();

    @Key("comment.invalid.id")
    String getCommentInvalidId();

    @Key("delete.non.exists.id")
    int getNonExistsIdDelete();

    @Key("delete.invalid.id")
    String getInvalidIdDelete();

    @Key("post.data.count")
    int createPostsCount();

    @Key("post.invalid.id")
    String getPostInvalidId();

    @Key("post.non.exists.id")
    int getPostNonExistsId();

    @Key("per.page")
    int getPerPage();
}

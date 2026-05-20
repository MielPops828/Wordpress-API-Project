package dto.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentEntity {
    private int post;
    private String content;
}

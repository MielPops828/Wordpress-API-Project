package dto.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostEntity {
    private String title;
    private String content;
    private String status;
}

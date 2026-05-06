package org.blueseacode.service.resource.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentCreateRequest {

    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long parentId;
}

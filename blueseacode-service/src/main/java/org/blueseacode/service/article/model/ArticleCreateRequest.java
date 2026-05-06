package org.blueseacode.service.article.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ArticleCreateRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String summary;

    private String coverImage;

    private Long categoryId;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    private List<Long> tagIds;

    private Integer isComment;      // 是否允许评论

    private Integer status;         // 0草稿 1提交审核
}

package org.blueseacode.service.resource.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ResourceCreateRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    private String coverImage;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "文件不能为空")
    private String fileUrl;

    private String fileName;

    private Integer downloadPoints;

    private List<Long> tagIds;
}

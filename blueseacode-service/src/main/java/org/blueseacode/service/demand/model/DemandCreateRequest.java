package org.blueseacode.service.demand.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DemandCreateRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "描述不能为空")
    private String description;

    @NotBlank(message = "分类不能为空")
    private String category;

    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private LocalDateTime deadline;
    private String contact;
    private List<DemandAttachmentDTO> attachments;
}

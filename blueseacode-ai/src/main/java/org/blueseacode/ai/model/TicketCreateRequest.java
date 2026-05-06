package org.blueseacode.ai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketCreateRequest {

    @NotBlank(message = "工单标题不能为空")
    private String title;

    private String description;

    private String category;     // ACCOUNT/TECH/REPORT/OTHER

    private Integer priority;    // 1低 2中 3高 4紧急
}

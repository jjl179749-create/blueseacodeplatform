package org.blueseacode.dao.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.blueseacode.common.response.PageParam;

@Data
@EqualsAndHashCode(callSuper = true)
public class DemandQueryDTO extends PageParam {

    private String keyword;
    private String category;
    private Integer status;            // 管理端使用
    private String sortBy;             // latest / budget / deadline
    private Long userId;               // 我的需求
    private Long takerId;              // 我接的单
    private Long currentUserId;        // 当前登录用户（用于填充 isTaker）
}

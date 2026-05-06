package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dem_demand")
public class DemDemand extends BaseEntity {

    private Long userId;
    private String title;
    private String description;
    private String category;          // PROJECT/TECH_CONSULT/TEAM_UP/OTHER
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private LocalDateTime deadline;
    private String contact;
    private Integer viewCount;
    private Integer orderCount;
    private Long takerId;             // 接单用户ID
    private Integer status;           // 0待审 1招募中 2进行中 3已完成 4已关闭 5驳回
    private String rejectReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;

    // ===== 非数据库字段（联表查询填充） =====

    @TableField(exist = false)
    private String nickname;          // 发布者昵称

    @TableField(exist = false)
    private String avatar;            // 发布者头像

    @TableField(exist = false)
    private String takerNickname;     // 接单者昵称

    @TableField(exist = false)
    private String takerAvatar;       // 接单者头像

    @TableField(exist = false)
    private List<DemAttachment> attachments;  // 附件列表

    @TableField(exist = false)
    private Boolean isTaker;          // 当前用户是否已接单
}

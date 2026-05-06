package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("res_resource")
public class ResResource extends BaseEntity {

    private Long userId;
    private String title;
    private String description;
    private String coverImage;
    private Long categoryId;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String fileFormat;
    private Integer downloadPoints;
    private Integer downloadCount;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer status;
    private String rejectReason;
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    // ===== 联表字段（非数据库字段） =====
    @TableField(exist = false)
    private String nickname;          // 发布者昵称

    @TableField(exist = false)
    private String avatar;            // 发布者头像

    @TableField(exist = false)
    private String categoryName;      // 分类名称

    @TableField(exist = false)
    private List<String> tags;        // 标签列表

    @TableField(exist = false)
    private Boolean isCollected;      // 当前用户是否收藏
}

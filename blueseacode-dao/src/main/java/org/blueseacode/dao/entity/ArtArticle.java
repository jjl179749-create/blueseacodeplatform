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
@TableName("art_article")
public class ArtArticle extends BaseEntity {

    private Long userId;
    private String title;
    private String summary;
    private String coverImage;
    private String content;
    private Long categoryId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer isComment;
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
    private String nickname;          // 作者昵称

    @TableField(exist = false)
    private String avatar;            // 作者头像

    @TableField(exist = false)
    private String categoryName;      // 分类名称

    @TableField(exist = false)
    private List<String> tags;        // 标签列表

    @TableField(exist = false)
    private Boolean isLiked;          // 当前用户是否已点赞

    @TableField(exist = false)
    private Boolean isCollected;      // 当前用户是否已收藏
}

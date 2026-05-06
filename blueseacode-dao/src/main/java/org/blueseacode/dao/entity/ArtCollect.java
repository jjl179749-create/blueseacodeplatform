package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("art_collect")
public class ArtCollect extends BaseEntity {

    private Long userId;
    private Long articleId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

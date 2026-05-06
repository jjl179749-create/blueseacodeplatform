package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("res_tag")
public class ResTag extends BaseEntity {

    private String name;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

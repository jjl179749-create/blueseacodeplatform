package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dem_order")
public class DemOrder extends BaseEntity {

    private Long demandId;
    private Long userId;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private String avatar;
}

package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dem_progress")
public class DemProgress extends BaseEntity {

    private Long demandId;
    private String content;
    private Long createBy;
    private LocalDateTime createTime;
}

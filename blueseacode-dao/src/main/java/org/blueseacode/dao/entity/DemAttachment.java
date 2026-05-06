package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dem_attachment")
public class DemAttachment extends BaseEntity {

    private Long demandId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private LocalDateTime createTime;
}

package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("res_resource_tag")
public class ResResourceTag extends BaseEntity {

    private Long resourceId;
    private Long tagId;
}

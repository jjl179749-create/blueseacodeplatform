package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("art_tag")
public class ArtTag extends BaseEntity {

    private String name;
}

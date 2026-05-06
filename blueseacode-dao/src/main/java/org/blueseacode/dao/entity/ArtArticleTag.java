package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("art_article_tag")
public class ArtArticleTag extends BaseEntity {

    private Long articleId;
    private Long tagId;
}

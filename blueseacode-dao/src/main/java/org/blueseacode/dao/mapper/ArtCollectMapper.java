package org.blueseacode.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.blueseacode.dao.entity.ArtCollect;

public interface ArtCollectMapper extends BaseMapper<ArtCollect> {

    /** 查询用户是否已收藏某文章 */
    @Select("SELECT COUNT(1) FROM art_collect WHERE user_id = #{userId} AND article_id = #{articleId}")
    long countByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);
}

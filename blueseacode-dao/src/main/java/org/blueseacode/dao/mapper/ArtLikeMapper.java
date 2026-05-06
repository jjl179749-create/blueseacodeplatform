package org.blueseacode.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.blueseacode.dao.entity.ArtLike;

public interface ArtLikeMapper extends BaseMapper<ArtLike> {

    /** 查询用户是否已点赞某文章 */
    @Select("SELECT COUNT(1) FROM art_like WHERE user_id = #{userId} AND article_id = #{articleId}")
    long countByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);
}

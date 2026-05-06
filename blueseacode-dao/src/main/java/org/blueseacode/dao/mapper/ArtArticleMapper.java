package org.blueseacode.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.blueseacode.dao.dto.ArticleQueryDTO;
import org.blueseacode.dao.entity.ArtArticle;

public interface ArtArticleMapper extends BaseMapper<ArtArticle> {

    /** 分页查询文章列表（支持搜索+分类+排序） */
    Page<ArtArticle> selectArticlePage(IPage<?> page,
                                        @Param("query") ArticleQueryDTO query);

    /** 更新浏览量+1 */
    @Update("UPDATE art_article SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    /** 更新点赞数+1 */
    @Update("UPDATE art_article SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(@Param("id") Long id);

    /** 更新点赞数-1 */
    @Update("UPDATE art_article SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{id}")
    void decrementLikeCount(@Param("id") Long id);

    /** 更新评论数+1 */
    @Update("UPDATE art_article SET comment_count = comment_count + 1 WHERE id = #{id}")
    void incrementCommentCount(@Param("id") Long id);

    /** 更新评论数-1 */
    @Update("UPDATE art_article SET comment_count = GREATEST(comment_count - 1, 0) WHERE id = #{id}")
    void decrementCommentCount(@Param("id") Long id);

    /** 更新收藏数+1 */
    @Update("UPDATE art_article SET collect_count = collect_count + 1 WHERE id = #{id}")
    void incrementCollectCount(@Param("id") Long id);

    /** 更新收藏数-1 */
    @Update("UPDATE art_article SET collect_count = GREATEST(collect_count - 1, 0) WHERE id = #{id}")
    void decrementCollectCount(@Param("id") Long id);
}

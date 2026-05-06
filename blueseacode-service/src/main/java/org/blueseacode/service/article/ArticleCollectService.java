package org.blueseacode.service.article;

import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.entity.ArtArticle;

public interface ArticleCollectService {

    /** 收藏文章 */
    void collect(Long userId, Long articleId);

    /** 取消收藏 */
    void cancel(Long userId, Long articleId);

    /** 查询用户是否已收藏 */
    boolean isCollected(Long userId, Long articleId);

    /** 获取用户收藏的文章列表（分页） */
    PageResult<ArtArticle> listByUser(Long userId, int page, int size);
}

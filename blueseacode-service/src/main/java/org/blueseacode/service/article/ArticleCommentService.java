package org.blueseacode.service.article;

import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.entity.ArtComment;
import org.blueseacode.service.article.model.ArticleCommentCreateRequest;

public interface ArticleCommentService {

    /** 获取文章评论列表（分页） */
    PageResult<ArtComment> list(Long articleId, int page, int size);

    /** 发表评论 */
    void create(Long userId, ArticleCommentCreateRequest request);

    /** 删除评论 */
    void delete(Long userId, Long commentId);
}

package org.blueseacode.service.article;

public interface ArticleLikeService {

    /** 点赞 */
    void like(Long userId, Long articleId);

    /** 取消点赞 */
    void cancel(Long userId, Long articleId);

    /** 查询用户是否已点赞 */
    boolean isLiked(Long userId, Long articleId);
}

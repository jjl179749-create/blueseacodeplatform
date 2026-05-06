package org.blueseacode.service.article.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.enums.ResultCode;
import org.blueseacode.common.exception.BusinessException;
import org.blueseacode.dao.entity.ArtArticle;
import org.blueseacode.dao.entity.ArtLike;
import org.blueseacode.dao.mapper.ArtArticleMapper;
import org.blueseacode.dao.mapper.ArtLikeMapper;
import org.blueseacode.service.article.ArticleLikeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleLikeServiceImpl implements ArticleLikeService {

    private final ArtLikeMapper likeMapper;
    private final ArtArticleMapper articleMapper;

    @Override
    @Transactional
    public void like(Long userId, Long articleId) {
        // 检查是否已点赞
        long count = likeMapper.countByUserAndArticle(userId, articleId);
        if (count > 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "已经点过赞了");
        }

        // 检查文章是否存在
        ArtArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        ArtLike like = new ArtLike();
        like.setUserId(userId);
        like.setArticleId(articleId);
        likeMapper.insert(like);

        articleMapper.incrementLikeCount(articleId);
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long articleId) {
        likeMapper.delete(new LambdaQueryWrapper<ArtLike>()
                .eq(ArtLike::getUserId, userId)
                .eq(ArtLike::getArticleId, articleId));
        articleMapper.decrementLikeCount(articleId);
    }

    @Override
    public boolean isLiked(Long userId, Long articleId) {
        if (userId == null) return false;
        return likeMapper.countByUserAndArticle(userId, articleId) > 0;
    }
}

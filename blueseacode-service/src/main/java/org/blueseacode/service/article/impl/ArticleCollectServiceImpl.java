package org.blueseacode.service.article.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.enums.ResultCode;
import org.blueseacode.common.exception.BusinessException;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.entity.ArtArticle;
import org.blueseacode.dao.entity.ArtCollect;
import org.blueseacode.dao.mapper.ArtArticleMapper;
import org.blueseacode.dao.mapper.ArtCollectMapper;
import org.blueseacode.service.article.ArticleCollectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleCollectServiceImpl implements ArticleCollectService {

    private final ArtCollectMapper collectMapper;
    private final ArtArticleMapper articleMapper;

    @Override
    @Transactional
    public void collect(Long userId, Long articleId) {
        // 检查是否已收藏
        long count = collectMapper.countByUserAndArticle(userId, articleId);
        if (count > 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "已收藏过该文章");
        }

        // 检查文章是否存在
        ArtArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        ArtCollect collect = new ArtCollect();
        collect.setUserId(userId);
        collect.setArticleId(articleId);
        collectMapper.insert(collect);

        articleMapper.incrementCollectCount(articleId);
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long articleId) {
        collectMapper.delete(
                new LambdaQueryWrapper<ArtCollect>()
                        .eq(ArtCollect::getUserId, userId)
                        .eq(ArtCollect::getArticleId, articleId));

        // 重新统计收藏数
        long count = collectMapper.selectCount(
                new LambdaQueryWrapper<ArtCollect>()
                        .eq(ArtCollect::getArticleId, articleId));
        ArtArticle article = new ArtArticle();
        article.setId(articleId);
        article.setCollectCount((int) count);
        articleMapper.updateById(article);
    }

    @Override
    public boolean isCollected(Long userId, Long articleId) {
        return collectMapper.countByUserAndArticle(userId, articleId) > 0;
    }

    @Override
    public PageResult<ArtArticle> listByUser(Long userId, int page, int size) {
        // 查询用户收藏的文章ID列表
        Page<ArtCollect> collectPage = collectMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<ArtCollect>()
                        .eq(ArtCollect::getUserId, userId)
                        .orderByDesc(ArtCollect::getCreateTime));

        List<Long> articleIds = collectPage.getRecords().stream()
                .map(ArtCollect::getArticleId)
                .collect(Collectors.toList());

        // 查询文章详情
        List<ArtArticle> articles = articleIds.isEmpty()
                ? List.of()
                : articleMapper.selectBatchIds(articleIds);

        return new PageResult<>(articles, collectPage.getTotal(), page, size);
    }
}

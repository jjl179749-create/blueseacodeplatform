package org.blueseacode.service.article.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.enums.ResultCode;
import org.blueseacode.common.exception.BusinessException;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.dto.ArticleQueryDTO;
import org.blueseacode.dao.entity.*;
import org.blueseacode.dao.mapper.*;
import org.blueseacode.dao.util.PageUtil;
import org.blueseacode.service.article.ArticleService;
import org.blueseacode.service.article.model.ArticleCreateRequest;
import org.blueseacode.service.message.NotificationService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArtArticleMapper articleMapper;
    private final ArtCategoryMapper categoryMapper;
    private final ArtTagMapper tagMapper;
    private final ArtArticleTagMapper articleTagMapper;
    private final ArtLikeMapper likeMapper;
    private final ArtCollectMapper collectMapper;
    private final SysUserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    @CacheEvict(value = "hotData", key = "'hot_articles'", condition = "#request.status != null && #request.status == 1")
    public Long createArticle(Long userId, ArticleCreateRequest request) {
        ArtArticle article = new ArtArticle();
        article.setUserId(userId);
        article.setTitle(request.getTitle());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setContent(request.getContent());
        article.setCategoryId(request.getCategoryId());
        article.setIsComment(request.getIsComment() != null ? request.getIsComment() : 1);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setCollectCount(0);
        article.setSort(0);

        // 如果前端提交的是 "发布" 则状态为待审，否则为草稿
        if (request.getStatus() != null && request.getStatus() == 1) {
            article.setStatus(1); // 待审核
        } else {
            article.setStatus(0); // 草稿
        }
        articleMapper.insert(article);

        // 保存标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                ArtArticleTag at = new ArtArticleTag();
                at.setArticleId(article.getId());
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }

        return article.getId();
    }

    @Override
    @Transactional
    public void updateArticle(Long userId, Long articleId, ArticleCreateRequest request) {
        ArtArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        // 只能编辑自己的文章
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        // 只能编辑草稿或待审核状态的文章
        if (article.getStatus() > 1) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "已通过或已驳回的文章不可编辑");
        }

        article.setTitle(request.getTitle());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setContent(request.getContent());
        article.setCategoryId(request.getCategoryId());
        article.setIsComment(request.getIsComment() != null ? request.getIsComment() : 1);
        // 如果重新提交审核
        if (request.getStatus() != null && request.getStatus() == 1) {
            article.setStatus(1);
        }
        articleMapper.updateById(article);

        // 更新标签关联：先删后插
        articleTagMapper.delete(
                new LambdaQueryWrapper<ArtArticleTag>()
                        .eq(ArtArticleTag::getArticleId, articleId));
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                ArtArticleTag at = new ArtArticleTag();
                at.setArticleId(articleId);
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
    }

    @Override
    @Transactional
    public void deleteArticle(Long userId, Long articleId) {
        ArtArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        // 只能删除自己的文章
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        articleMapper.deleteById(articleId); // 逻辑删除
    }

    @Override
    public PageResult<ArtArticle> list(ArticleQueryDTO query) {
        // 查询文章分页
        Page<ArtArticle> pageResult = articleMapper.selectArticlePage(
                new Page<>(query.getPage(), query.getSize()), query);

        // 为每条文章填充标签、点赞/收藏状态
        for (ArtArticle article : pageResult.getRecords()) {
            fillArticleExtra(article, query.getCurrentUserId());
        }

        return PageUtil.of(pageResult);
    }

    @Override
    public ArtArticle getDetail(Long articleId, Long currentUserId) {
        ArtArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        // 浏览量+1
        articleMapper.incrementViewCount(articleId);
        article.setViewCount(article.getViewCount() + 1);

        // 补充关联信息
        SysUser user = userMapper.selectById(article.getUserId());
        if (user != null) {
            article.setNickname(user.getNickname());
            article.setAvatar(user.getAvatar());
        }
        ArtCategory category = categoryMapper.selectById(article.getCategoryId());
        article.setCategoryName(category != null ? category.getName() : null);

        // 补充标签、点赞/收藏状态
        fillArticleExtra(article, currentUserId);

        return article;
    }

    @Override
    @Transactional
    public void updateStatus(Long articleId, Integer status, String rejectReason) {
        ArtArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        article.setStatus(status);
        article.setRejectReason(rejectReason);
        articleMapper.updateById(article);

        // 发送审核通知
        String title = status == 2 ? "文章审核通过" : "文章审核未通过";
        String content = status == 2
                ? "您的文章《" + article.getTitle() + "》已通过审核并发布"
                : "您的文章《" + article.getTitle() + "》未通过审核，原因: " + rejectReason;
        notificationService.send(article.getUserId(), "AUDIT", title, content,
                articleId, "ARTICLE");
    }

    @Override
    @Cacheable(value = "hotData", key = "'hot_articles'")
    public List<ArtArticle> getHotArticles(int limit) {
        return articleMapper.selectPage(
                new Page<>(1, limit),
                new LambdaQueryWrapper<ArtArticle>()
                        .eq(ArtArticle::getStatus, 2)
                        .orderByDesc(ArtArticle::getViewCount))
                .getRecords();
    }

    @Override
    @Cacheable(value = "categories", key = "'article_categories'")
    public List<ArtCategory> listCategories() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<ArtCategory>()
                        .orderByAsc(ArtCategory::getSort));
    }

    @Override
    @Cacheable(value = "tags", key = "'article_tags'")
    public List<ArtTag> listTags() {
        return tagMapper.selectList(null);
    }

    // ===== 私有方法 =====

    private void fillArticleExtra(ArtArticle article, Long currentUserId) {
        // 填充标签
        List<String> tags = getTagsByArticleId(article.getId());
        article.setTags(tags);

        // 判断当前用户是否已点赞/收藏
        if (currentUserId != null) {
            long liked = likeMapper.countByUserAndArticle(currentUserId, article.getId());
            article.setIsLiked(liked > 0);

            long collected = collectMapper.countByUserAndArticle(currentUserId, article.getId());
            article.setIsCollected(collected > 0);
        }
    }

    private List<String> getTagsByArticleId(Long articleId) {
        return articleTagMapper.selectList(
                        new LambdaQueryWrapper<ArtArticleTag>()
                                .eq(ArtArticleTag::getArticleId, articleId))
                .stream()
                .map(at -> {
                    ArtTag tag = tagMapper.selectById(at.getTagId());
                    return tag != null ? tag.getName() : null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }
}

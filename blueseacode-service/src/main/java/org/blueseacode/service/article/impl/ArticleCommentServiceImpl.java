package org.blueseacode.service.article.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.enums.ResultCode;
import org.blueseacode.common.exception.BusinessException;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.entity.ArtArticle;
import org.blueseacode.dao.entity.ArtComment;
import org.blueseacode.dao.entity.SysUser;
import org.blueseacode.dao.mapper.ArtArticleMapper;
import org.blueseacode.dao.mapper.ArtCommentMapper;
import org.blueseacode.dao.mapper.SysUserMapper;
import org.blueseacode.dao.util.PageUtil;
import org.blueseacode.service.article.ArticleCommentService;
import org.blueseacode.service.article.model.ArticleCommentCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleCommentServiceImpl implements ArticleCommentService {

    private final ArtCommentMapper commentMapper;
    private final ArtArticleMapper articleMapper;
    private final SysUserMapper userMapper;

    @Override
    public PageResult<ArtComment> list(Long articleId, int page, int size) {
        Page<ArtComment> pageResult = commentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<ArtComment>()
                        .eq(ArtComment::getArticleId, articleId)
                        .eq(ArtComment::getStatus, 1)
                        .orderByAsc(ArtComment::getCreateTime));

        // 填充用户昵称和头像，以及回复目标昵称
        for (ArtComment comment : pageResult.getRecords()) {
            SysUser user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                comment.setNickname(user.getNickname());
                comment.setAvatar(user.getAvatar());
            }
            // 如果是回复，填充被回复者的昵称
            if (comment.getParentId() != null) {
                ArtComment parentComment = commentMapper.selectById(comment.getParentId());
                if (parentComment != null) {
                    SysUser parentUser = userMapper.selectById(parentComment.getUserId());
                    if (parentUser != null) {
                        comment.setReplyToNickname(parentUser.getNickname());
                    }
                }
            }
        }
        return PageUtil.of(pageResult);
    }

    @Override
    @Transactional
    public void create(Long userId, ArticleCommentCreateRequest request) {
        ArtComment comment = new ArtComment();
        comment.setArticleId(request.getArticleId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setLikeCount(0);
        comment.setStatus(1);
        commentMapper.insert(comment);

        // 更新文章的评论数
        articleMapper.incrementCommentCount(request.getArticleId());
    }

    @Override
    @Transactional
    public void delete(Long userId, Long commentId) {
        ArtComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 只能删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        commentMapper.deleteById(commentId);

        // 减少文章的评论数
        articleMapper.decrementCommentCount(comment.getArticleId());
    }
}

package org.blueseacode.service.resource.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.enums.ResultCode;
import org.blueseacode.common.exception.BusinessException;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.entity.ResComment;
import org.blueseacode.dao.entity.SysUser;
import org.blueseacode.dao.mapper.ResCommentMapper;
import org.blueseacode.dao.mapper.ResResourceMapper;
import org.blueseacode.dao.mapper.SysUserMapper;
import org.blueseacode.dao.util.PageUtil;
import org.blueseacode.service.resource.CommentService;
import org.blueseacode.service.resource.model.CommentCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final ResCommentMapper commentMapper;
    private final ResResourceMapper resourceMapper;
    private final SysUserMapper userMapper;

    @Override
    public PageResult<ResComment> list(Long resourceId, int page, int size) {
        Page<ResComment> pageResult = commentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<ResComment>()
                        .eq(ResComment::getResourceId, resourceId)
                        .eq(ResComment::getStatus, 1)
                        .orderByAsc(ResComment::getCreateTime));

        // 填充用户昵称和头像，以及回复目标昵称
        for (ResComment comment : pageResult.getRecords()) {
            SysUser user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                comment.setNickname(user.getNickname());
                comment.setAvatar(user.getAvatar());
            }
            // 如果是回复，填充被回复者的昵称
            if (comment.getParentId() != null) {
                ResComment parentComment = commentMapper.selectById(comment.getParentId());
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
    public void create(Long userId, CommentCreateRequest request) {
        ResComment comment = new ResComment();
        comment.setResourceId(request.getResourceId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setLikeCount(0);
        comment.setStatus(1);
        commentMapper.insert(comment);

        // 更新资源的评论数
        resourceMapper.incrementCommentCount(request.getResourceId());
    }

    @Override
    @Transactional
    public void delete(Long userId, Long commentId) {
        ResComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 只能删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        commentMapper.deleteById(commentId);

        // 减少资源的评论数
        resourceMapper.decrementCommentCount(comment.getResourceId());
    }
}

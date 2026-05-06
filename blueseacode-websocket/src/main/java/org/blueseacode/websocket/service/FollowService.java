package org.blueseacode.websocket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.exception.BusinessException;
import org.blueseacode.dao.entity.MsgUserFollow;
import org.blueseacode.dao.entity.SysUser;
import org.blueseacode.dao.mapper.MsgUserFollowMapper;
import org.blueseacode.dao.mapper.SysUserMapper;
import org.blueseacode.service.message.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final MsgUserFollowMapper followMapper;
    private final SysUserMapper userMapper;
    private final NotificationService notificationService;

    /**
     * 关注用户
     */
    @Transactional
    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new BusinessException(40000, "不能关注自己");
        }

        // 检查被关注者是否存在
        if (userMapper.selectById(followeeId) == null) {
            throw new BusinessException(404, "被关注用户不存在");
        }

        // 检查是否已关注
        Long count = followMapper.selectCount(
                new LambdaQueryWrapper<MsgUserFollow>()
                        .eq(MsgUserFollow::getFollowerId, followerId)
                        .eq(MsgUserFollow::getFolloweeId, followeeId));
        if (count > 0) {
            throw new BusinessException(40000, "已经关注过了");
        }

        // 添加关注
        MsgUserFollow follow = new MsgUserFollow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        followMapper.insert(follow);

        // 发送通知
        notificationService.send(followeeId, "FOLLOW", "新粉丝",
                "有人关注了你", followerId, "USER");
    }

    /**
     * 取消关注
     */
    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        followMapper.delete(
                new LambdaQueryWrapper<MsgUserFollow>()
                        .eq(MsgUserFollow::getFollowerId, followerId)
                        .eq(MsgUserFollow::getFolloweeId, followeeId));
    }

    /**
     * 是否已关注
     */
    public boolean isFollowing(Long followerId, Long followeeId) {
        return followMapper.selectCount(
                new LambdaQueryWrapper<MsgUserFollow>()
                        .eq(MsgUserFollow::getFollowerId, followerId)
                        .eq(MsgUserFollow::getFolloweeId, followeeId)) > 0;
    }

    /**
     * 获取粉丝数
     */
    public long getFollowerCount(Long userId) {
        return followMapper.selectCount(
                new LambdaQueryWrapper<MsgUserFollow>()
                        .eq(MsgUserFollow::getFolloweeId, userId));
    }

    /**
     * 获取关注数
     */
    public long getFolloweeCount(Long userId) {
        return followMapper.selectCount(
                new LambdaQueryWrapper<MsgUserFollow>()
                        .eq(MsgUserFollow::getFollowerId, userId));
    }

    /**
     * 获取当前用户关注的用户ID列表
     */
    public List<Long> listFolloweeIds(Long userId) {
        List<MsgUserFollow> follows = followMapper.selectList(
                new LambdaQueryWrapper<MsgUserFollow>()
                        .eq(MsgUserFollow::getFollowerId, userId)
                        .orderByDesc(MsgUserFollow::getCreateTime));
        return follows.stream()
                .map(MsgUserFollow::getFolloweeId)
                .collect(Collectors.toList());
    }

    /**
     * 获取关注的用户列表（含昵称、头像）
     */
    public List<SysUser> listFollowees(Long userId) {
        List<Long> ids = listFolloweeIds(userId);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.selectBatchIds(ids);
    }

    /**
     * 获取粉丝ID列表
     */
    public List<Long> listFollowerIds(Long userId) {
        List<MsgUserFollow> follows = followMapper.selectList(
                new LambdaQueryWrapper<MsgUserFollow>()
                        .eq(MsgUserFollow::getFolloweeId, userId)
                        .orderByDesc(MsgUserFollow::getCreateTime));
        return follows.stream()
                .map(MsgUserFollow::getFollowerId)
                .collect(Collectors.toList());
    }

    /**
     * 获取粉丝列表（含昵称、头像）
     */
    public List<SysUser> listFollowers(Long userId) {
        List<Long> ids = listFollowerIds(userId);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.selectBatchIds(ids);
    }
}

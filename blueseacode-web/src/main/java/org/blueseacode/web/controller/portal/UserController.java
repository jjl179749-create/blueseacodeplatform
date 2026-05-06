package org.blueseacode.web.controller.portal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.MsgNotification;
import org.blueseacode.dao.entity.SysAnnouncement;
import org.blueseacode.dao.entity.SysUser;
import org.blueseacode.dao.mapper.SysAnnouncementMapper;
import org.blueseacode.dao.mapper.SysUserMapper;
import org.blueseacode.service.message.NotificationService;
import org.blueseacode.service.user.UserService;
import org.blueseacode.service.user.model.PasswordUpdateRequest;
import org.blueseacode.service.user.model.UserProfileUpdateRequest;
import org.blueseacode.service.user.model.UserProfileVO;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(AppConstant.PORTAL_PREFIX + "/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final SysAnnouncementMapper announcementMapper;
    private final SysUserMapper userMapper;

    // ===== 个人信息 =====

    /**
     * 获取当前用户个人信息
     */
    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(userService.getProfile(userId));
    }

    /**
     * 获取他人主页信息
     */
    @GetMapping("/profile/{userId}")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        return Result.success(userService.getUserProfile(userId));
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(HttpServletRequest request,
                                      @Valid @RequestBody UserProfileUpdateRequest req) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        userService.updateProfile(userId, req);
        return Result.success("更新成功");
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(HttpServletRequest request,
                                       @Valid @RequestBody PasswordUpdateRequest req) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        userService.updatePassword(userId, req);
        return Result.success("密码修改成功");
    }

    /**
     * 更新头像（前端先上传文件获取URL，再调用此接口更新）
     */
    @PutMapping("/avatar")
    public Result<Void> updateAvatar(HttpServletRequest request,
                                     @RequestParam String avatarUrl) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        userService.updateAvatar(userId, avatarUrl);
        return Result.success("头像更新成功");
    }

    // ===== 消息通知 =====

    /**
     * 获取用户通知列表
     */
    @GetMapping("/notifications")
    public Result<PageResult<MsgNotification>> notifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean isRead) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(notificationService.list(userId, page, size, isRead));
    }

    /**
     * 标记单条通知已读
     */
    @PutMapping("/notifications/{id}/read")
    public Result<Void> markRead(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        notificationService.markRead(id, userId);
        return Result.success();
    }

    /**
     * 一键已读所有通知
     */
    @PutMapping("/notifications/read-all")
    public Result<Void> markAllRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        notificationService.markAllRead(userId);
        return Result.success();
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/notifications/unread-count")
    public Result<Long> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(notificationService.countUnread(userId));
    }

    // ===== 批量用户信息 =====

    /**
     * 批量获取用户基本信息（昵称、头像）
     */
    @PostMapping("/batch")
    public Result<Map<Long, Map<String, Object>>> batch(@RequestBody List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.success(new HashMap<>());
        }
        List<SysUser> users = userMapper.selectBatchIds(userIds);
        Map<Long, Map<String, Object>> result = users.stream().collect(Collectors.toMap(
                SysUser::getId,
                u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("nickname", u.getNickname() != null ? u.getNickname() : u.getUsername());
                    m.put("avatar", u.getAvatar());
                    return m;
                }
        ));
        return Result.success(result);
    }

    // ===== 公告 =====

    /**
     * 获取已发布的公告列表（用户端）
     */
    @GetMapping("/announcements")
    public Result<List<SysAnnouncement>> announcements() {
        List<SysAnnouncement> list = announcementMapper.selectList(
                new LambdaQueryWrapper<SysAnnouncement>()
                        .eq(SysAnnouncement::getStatus, 1)
                        .orderByDesc(SysAnnouncement::getSort)
                        .orderByDesc(SysAnnouncement::getCreateTime));
        return Result.success(list);
    }
}

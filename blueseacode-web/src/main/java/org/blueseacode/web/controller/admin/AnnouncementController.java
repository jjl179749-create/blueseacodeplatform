package org.blueseacode.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.SysAnnouncement;
import org.blueseacode.dao.entity.SysUser;
import org.blueseacode.dao.mapper.SysAnnouncementMapper;
import org.blueseacode.dao.mapper.SysUserMapper;
import org.blueseacode.dao.util.PageUtil;
import org.blueseacode.service.message.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(AppConstant.ADMIN_PREFIX + "/announcements")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
@RequiredArgsConstructor
public class AnnouncementController {

    private final SysAnnouncementMapper announcementMapper;
    private final NotificationService notificationService;
    private final SysUserMapper userMapper;

    /**
     * 公告列表
     */
    @GetMapping
    public Result<PageResult<SysAnnouncement>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<SysAnnouncement> p = announcementMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SysAnnouncement>()
                        .eq(status != null, SysAnnouncement::getStatus, status)
                        .orderByDesc(SysAnnouncement::getSort)
                        .orderByDesc(SysAnnouncement::getCreateTime));
        return Result.success(PageUtil.of(p));
    }

    /**
     * 公告详情
     */
    @GetMapping("/{id}")
    public Result<SysAnnouncement> detail(@PathVariable Long id) {
        SysAnnouncement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            return Result.failed(404, "公告不存在");
        }
        return Result.success(announcement);
    }

    /**
     * 创建公告
     */
    @PostMapping
    public Result<Void> create(@RequestBody SysAnnouncement announcement) {
        announcementMapper.insert(announcement);
        // 发布状态时给所有活跃用户发送系统通知
        if (announcement.getStatus() != null && announcement.getStatus() == 1) {
            notifyAllUsers(announcement.getTitle(), announcement.getContent());
        }
        return Result.success("公告创建成功");
    }

    /**
     * 编辑公告
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysAnnouncement announcement) {
        SysAnnouncement old = announcementMapper.selectById(id);
        announcement.setId(id);
        announcementMapper.updateById(announcement);
        // 状态从非发布变为发布时，发送通知
        if (announcement.getStatus() != null && announcement.getStatus() == 1
                && old != null && old.getStatus() != 1) {
            notifyAllUsers(announcement.getTitle(), announcement.getContent());
        }
        return Result.success("公告更新成功");
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementMapper.deleteById(id);
        return Result.success("公告删除成功");
    }

    /**
     * 给所有用户发送系统通知（仅排除已逻辑删除的用户）
     */
    private void notifyAllUsers(String title, String content) {
        List<Long> userIds = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .select(SysUser::getId))
                .stream()
                .map(SysUser::getId)
                .collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            notificationService.sendBatch(userIds, title, content);
        }
    }
}

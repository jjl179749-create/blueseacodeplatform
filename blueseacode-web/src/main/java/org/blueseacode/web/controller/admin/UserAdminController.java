package org.blueseacode.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.*;
import org.blueseacode.dao.mapper.*;
import org.blueseacode.dao.util.PageUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstant.ADMIN_PREFIX + "/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserAdminController {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    /**
     * 用户列表（支持搜索/状态筛选）
     */
    @GetMapping
    public Result<PageResult<SysUser>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<SysUser> p = userMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SysUser>()
                        .and(keyword != null && !keyword.isEmpty(),
                                w -> w.like(SysUser::getUsername, keyword)
                                        .or().like(SysUser::getNickname, keyword)
                                        .or().like(SysUser::getEmail, keyword))
                        .eq(status != null, SysUser::getStatus, status)
                        .orderByDesc(SysUser::getCreateTime));
        return Result.success(PageUtil.of(p));
    }

    /**
     * 用户详情
     */
    @GetMapping("/{id}")
    public Result<SysUser> detail(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return Result.failed(404, "用户不存在");
        }
        user.setRoles(userMapper.selectRoleCodesByUserId(id));
        return Result.success(user);
    }

    /**
     * 禁用/启用用户
     */
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return Result.failed(404, "用户不存在");
        }
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
        String msg = status == 1 ? "用户已禁用" : "用户已启用";
        return Result.success(msg);
    }

    /**
     * 分配角色
     */
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return Result.failed(404, "用户不存在");
        }
        // 先删除原角色
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));
        // 再插入新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                userRoleMapper.insert(new SysUserRole(id, roleId));
            }
        }
        return Result.success("角色分配成功");
    }
}

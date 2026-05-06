package org.blueseacode.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.*;
import org.blueseacode.dao.mapper.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(AppConstant.ADMIN_PREFIX + "/roles")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class RoleAdminController {

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    /**
     * 角色列表
     */
    @GetMapping
    public Result<List<SysRole>> list() {
        return Result.success(roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSort)));
    }

    /**
     * 创建角色
     */
    @PostMapping
    public Result<Void> create(@RequestBody SysRole role) {
        roleMapper.insert(role);
        return Result.success("创建成功");
    }

    /**
     * 编辑角色
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        roleMapper.updateById(role);
        return Result.success("更新成功");
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleMapper.deleteById(id);
        // 同时删除角色-权限关联
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, id));
        return Result.success("删除成功");
    }

    /**
     * 权限树
     */
    @GetMapping("/permissions")
    public Result<List<SysPermission>> permissions() {
        List<SysPermission> all = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .orderByAsc(SysPermission::getSort));
        return Result.success(buildTree(all));
    }

    /**
     * 角色的权限ID列表
     */
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> rolePermissions(@PathVariable Long id) {
        List<SysRolePermission> list = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, id));
        return Result.success(list.stream().map(SysRolePermission::getPermissionId).toList());
    }

    /**
     * 分配权限
     */
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id,
                                           @RequestBody List<Long> permissionIds) {
        // 先删除原有的权限关联
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, id));
        // 再插入新的权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permId : permissionIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(id);
                rp.setPermissionId(permId);
                rolePermissionMapper.insert(rp);
            }
        }
        return Result.success("权限分配成功");
    }

    /**
     * 构建菜单树
     */
    private List<SysPermission> buildTree(List<SysPermission> flatList) {
        Map<Long, SysPermission> map = flatList.stream()
                .collect(Collectors.toMap(SysPermission::getId, p -> p));
        List<SysPermission> roots = new ArrayList<>();
        for (SysPermission p : flatList) {
            if (p.getParentId() == null || p.getParentId() == 0) {
                roots.add(p);
            } else {
                SysPermission parent = map.get(p.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(p);
                }
            }
        }
        return roots;
    }
}

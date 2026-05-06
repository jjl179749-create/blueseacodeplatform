package org.blueseacode.web.controller.admin;

import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.dto.ResourceQueryDTO;
import org.blueseacode.dao.entity.ResCategory;
import org.blueseacode.dao.entity.ResResource;
import org.blueseacode.dao.entity.ResTag;
import org.blueseacode.service.resource.ResourceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstant.ADMIN_PREFIX + "/resources")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
@RequiredArgsConstructor
public class ResourceAdminController {

    private final ResourceService resourceService;

    /**
     * 资源列表（管理端查看所有状态）
     */
    @GetMapping
    public Result<PageResult<ResResource>> list(ResourceQueryDTO query) {
        // 管理端不过滤状态，可查看全部
        return Result.success(resourceService.list(query));
    }

    /**
     * 编辑资源
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ResResource resource) {
        resource.setId(id);
        resourceService.updateStatus(id, resource.getStatus(), resource.getRejectReason());
        return Result.success("更新成功");
    }

    /**
     * 上下架资源
     */
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id,
                                      @RequestParam Integer status,
                                      @RequestParam(required = false) String rejectReason) {
        resourceService.updateStatus(id, status, rejectReason);
        String msg = status == 1 ? "已上架" : status == 3 ? "已下架" : "状态变更成功";
        return Result.success(msg);
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        resourceService.deleteResource(0L, id);
        return Result.success("删除成功");
    }

    /**
     * 分类列表
     */
    @GetMapping("/categories")
    public Result<List<ResCategory>> categories() {
        return Result.success(resourceService.listCategories());
    }

    /**
     * 标签列表
     */
    @GetMapping("/tags")
    public Result<List<ResTag>> tags() {
        return Result.success(resourceService.listTags());
    }
}

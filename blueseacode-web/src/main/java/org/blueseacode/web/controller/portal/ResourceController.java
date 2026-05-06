package org.blueseacode.web.controller.portal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.dto.ResourceQueryDTO;
import org.blueseacode.dao.entity.ResCategory;
import org.blueseacode.dao.entity.ResResource;
import org.blueseacode.dao.entity.ResTag;
import org.blueseacode.service.resource.CollectService;
import org.blueseacode.service.resource.CommentService;
import org.blueseacode.service.resource.ResourceService;
import org.blueseacode.service.resource.model.ResourceCreateRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstant.PORTAL_PREFIX + "/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final CommentService commentService;
    private final CollectService collectService;

    /**
     * 资源列表（分页+搜索+分类+排序）
     */
    @GetMapping
    public Result<PageResult<ResResource>> list(ResourceQueryDTO query,
                                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        query.setCurrentUserId(userId);
        return Result.success(resourceService.list(query));
    }

    /**
     * 资源详情
     */
    @GetMapping("/{id}")
    public Result<ResResource> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(resourceService.getDetail(id, userId));
    }

    /**
     * 发布资源
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ResourceCreateRequest request,
                                HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success("发布成功，等待审核", resourceService.createResource(userId, request));
    }

    /**
     * 编辑资源（仅待审核状态可编辑）
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                                @Valid @RequestBody ResourceCreateRequest request,
                                HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        resourceService.updateResource(userId, id, request);
        return Result.success("更新成功");
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        resourceService.deleteResource(userId, id);
        return Result.success("删除成功");
    }

    /**
     * 下载资源（积分校验）
     */
    @PostMapping("/{id}/download")
    public Result<String> download(@PathVariable Long id, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        String fileUrl = resourceService.downloadResource(userId, id);
        return Result.success(fileUrl);
    }

    /**
     * 获取用户的资源列表（我的发布）
     */
    @GetMapping("/my")
    public Result<PageResult<ResResource>> myResources(ResourceQueryDTO query,
                                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        query.setCurrentUserId(userId);
        query.setUserId(userId);
        // 用户查看自己的资源时不过滤状态（可以看待审核的）
        query.setStatus(null);
        return Result.success(resourceService.list(query));
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

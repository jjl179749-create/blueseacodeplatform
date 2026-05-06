package org.blueseacode.web.controller.portal;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.ResResource;
import org.blueseacode.service.resource.CollectService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.PORTAL_PREFIX + "/collects")
@RequiredArgsConstructor
public class ResourceCollectController {

    private final CollectService collectService;

    /**
     * 收藏资源
     */
    @PostMapping("/resources/{resourceId}")
    public Result<Void> collect(@PathVariable Long resourceId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        collectService.collect(userId, resourceId);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/resources/{resourceId}")
    public Result<Void> cancel(@PathVariable Long resourceId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        collectService.cancel(userId, resourceId);
        return Result.success("取消收藏");
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/resources/{resourceId}/check")
    public Result<Boolean> check(@PathVariable Long resourceId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(collectService.isCollected(userId, resourceId));
    }

    /**
     * 获取我已收藏的资源列表
     */
    @GetMapping("/resources")
    public Result<PageResult<ResResource>> myCollectedResources(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(collectService.listByUser(userId, page, size));
    }
}

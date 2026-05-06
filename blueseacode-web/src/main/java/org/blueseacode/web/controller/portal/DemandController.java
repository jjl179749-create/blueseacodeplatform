package org.blueseacode.web.controller.portal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.dto.DemandQueryDTO;
import org.blueseacode.dao.entity.DemAttachment;
import org.blueseacode.dao.entity.DemDemand;
import org.blueseacode.dao.entity.DemOrder;
import org.blueseacode.service.demand.DemandService;
import org.blueseacode.service.demand.model.DemandCreateRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstant.PORTAL_PREFIX + "/demands")
@RequiredArgsConstructor
public class DemandController {

    private final DemandService demandService;

    /**
     * 需求列表（分页+搜索+分类+排序）
     */
    @GetMapping
    public Result<PageResult<DemDemand>> list(DemandQueryDTO query,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        query.setCurrentUserId(userId);
        return Result.success(demandService.list(query));
    }

    /**
     * 我的发布（我的需求列表，所有状态）
     */
    @GetMapping("/my")
    public Result<PageResult<DemDemand>> myDemands(DemandQueryDTO query,
                                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        query.setCurrentUserId(userId);
        query.setUserId(userId);
        query.setStatus(null); // 不过滤状态
        return Result.success(demandService.list(query));
    }

    /**
     * 我接的单（接单列表）
     */
    @GetMapping("/my-orders")
    public Result<PageResult<DemDemand>> myOrders(DemandQueryDTO query,
                                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        query.setCurrentUserId(userId);
        query.setTakerId(userId);
        return Result.success(demandService.list(query));
    }

    /**
     * 需求详情
     */
    @GetMapping("/{id}")
    public Result<DemDemand> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(demandService.getDetail(id, userId));
    }

    /**
     * 发布需求
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody DemandCreateRequest req,
                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success("发布成功，等待审核", demandService.createDemand(userId, req));
    }

    /**
     * 编辑需求
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                                @Valid @RequestBody DemandCreateRequest req,
                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        demandService.updateDemand(userId, id, req);
        return Result.success("更新成功");
    }

    /**
     * 删除需求
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        demandService.deleteDemand(userId, id);
        return Result.success("删除成功");
    }

    // ===== 接单 =====

    /**
     * 接单（使用Redis分布式锁防止并发）
     */
    @PostMapping("/{id}/take")
    public Result<Void> takeOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        demandService.takeOrder(id, userId);
        return Result.success("接单成功");
    }

    /**
     * 确认完成
     */
    @PostMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        demandService.completeDemand(userId, id);
        return Result.success("需求已完成");
    }

    // ===== 附件 & 接单记录 =====

    /**
     * 获取附件列表
     */
    @GetMapping("/{id}/attachments")
    public Result<List<DemAttachment>> attachments(@PathVariable Long id) {
        return Result.success(demandService.getAttachments(id));
    }

    /**
     * 获取接单记录
     */
    @GetMapping("/{id}/orders")
    public Result<PageResult<DemOrder>> orders(@PathVariable Long id,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return Result.success(demandService.getOrders(id, page, size));
    }
}

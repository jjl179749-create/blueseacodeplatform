package org.blueseacode.web.controller.admin;

import lombok.RequiredArgsConstructor;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.dto.DemandQueryDTO;
import org.blueseacode.dao.entity.DemDemand;
import org.blueseacode.service.demand.DemandService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/demands")
@RequiredArgsConstructor
public class DemandAdminController {

    private final DemandService demandService;

    /**
     * 需求列表（管理端查看所有状态）
     */
    @GetMapping
    public Result<PageResult<DemDemand>> list(DemandQueryDTO query) {
        return Result.success(demandService.list(query));
    }

    /**
     * 审核需求（通过/驳回）
     */
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id,
                               @RequestParam Integer status,
                               @RequestParam(required = false) String rejectReason) {
        demandService.updateStatus(id, status, rejectReason);
        String msg = status == 1 ? "审核通过" : "已驳回";
        return Result.success(msg);
    }
}

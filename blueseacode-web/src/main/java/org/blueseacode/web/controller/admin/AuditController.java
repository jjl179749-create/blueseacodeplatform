package org.blueseacode.web.controller.admin;

import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.dto.ArticleQueryDTO;
import org.blueseacode.dao.dto.DemandQueryDTO;
import org.blueseacode.dao.dto.ResourceQueryDTO;
import org.blueseacode.dao.entity.ArtArticle;
import org.blueseacode.dao.entity.DemDemand;
import org.blueseacode.dao.entity.ResResource;
import org.blueseacode.service.article.ArticleService;
import org.blueseacode.service.demand.DemandService;
import org.blueseacode.service.resource.ResourceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.ADMIN_PREFIX + "/audit")
@PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
@RequiredArgsConstructor
public class AuditController {

    private final ResourceService resourceService;
    private final ArticleService articleService;
    private final DemandService demandService;

    // ==================== 资源审核 ====================

    /**
     * 待审核资源列表
     */
    @GetMapping("/resources")
    public Result<PageResult<ResResource>> pendingResources(ResourceQueryDTO query) {
        query.setStatus(0);  // 待审核
        return Result.success(resourceService.list(query));
    }

    /**
     * 审核资源
     */
    @PutMapping("/resources/{id}")
    public Result<Void> auditResource(@PathVariable Long id,
                                       @RequestParam Integer status,
                                       @RequestParam(required = false) String rejectReason) {
        resourceService.updateStatus(id, status, rejectReason);
        String msg = status == 1 ? "审核通过" : "已驳回";
        return Result.success(msg);
    }

    // ==================== 文章审核 ====================

    /**
     * 待审核文章列表
     */
    @GetMapping("/articles")
    public Result<PageResult<ArtArticle>> pendingArticles(ArticleQueryDTO query) {
        query.setStatus(1);  // 待审核
        return Result.success(articleService.list(query));
    }

    /**
     * 审核文章
     */
    @PutMapping("/articles/{id}")
    public Result<Void> auditArticle(@PathVariable Long id,
                                      @RequestParam Integer status,
                                      @RequestParam(required = false) String rejectReason) {
        articleService.updateStatus(id, status, rejectReason);
        String msg = status == 2 ? "审核通过" : "已驳回";
        return Result.success(msg);
    }

    // ==================== 需求审核 ====================

    /**
     * 待审核需求列表
     */
    @GetMapping("/demands")
    public Result<PageResult<DemDemand>> pendingDemands(DemandQueryDTO query) {
        query.setStatus(0);  // 待审核
        return Result.success(demandService.list(query));
    }

    /**
     * 审核需求
     */
    @PutMapping("/demands/{id}")
    public Result<Void> auditDemand(@PathVariable Long id,
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String rejectReason) {
        demandService.updateStatus(id, status, rejectReason);
        String msg = status == 1 ? "审核通过" : "已驳回";
        return Result.success(msg);
    }
}

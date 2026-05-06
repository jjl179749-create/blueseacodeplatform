package org.blueseacode.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.*;
import org.blueseacode.dao.mapper.*;
import org.blueseacode.dao.dto.DashboardVO;
import org.blueseacode.dao.dto.TrendVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(AppConstant.ADMIN_PREFIX + "/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class DashboardController {

    private final SysUserMapper userMapper;
    private final ResResourceMapper resourceMapper;
    private final ArtArticleMapper articleMapper;
    private final DemDemandMapper demandMapper;

    @GetMapping("/statistics")
    public Result<DashboardVO> statistics() {
        DashboardVO vo = new DashboardVO();

        // 总用户数
        vo.setTotalUsers(userMapper.selectCount(null));

        // 今日新增用户
        vo.setTodayNewUsers(userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .ge(SysUser::getCreateTime, LocalDate.now().atStartOfDay())));

        // 总资源数
        vo.setTotalResources(resourceMapper.selectCount(
                new LambdaQueryWrapper<ResResource>().eq(ResResource::getDeleted, 0)));

        // 总文章数
        vo.setTotalArticles(articleMapper.selectCount(
                new LambdaQueryWrapper<ArtArticle>().eq(ArtArticle::getDeleted, 0)));

        // 总需求数
        vo.setTotalDemands(demandMapper.selectCount(
                new LambdaQueryWrapper<DemDemand>().eq(DemDemand::getDeleted, 0)));

        // 待审核总数（资源待审 + 文章待审 + 需求待审）
        long pendingResources = resourceMapper.selectCount(
                new LambdaQueryWrapper<ResResource>().eq(ResResource::getStatus, 0));
        long pendingArticles = articleMapper.selectCount(
                new LambdaQueryWrapper<ArtArticle>().eq(ArtArticle::getStatus, 1));
        long pendingDemands = demandMapper.selectCount(
                new LambdaQueryWrapper<DemDemand>().eq(DemDemand::getStatus, 0));
        vo.setPendingAudit(pendingResources + pendingArticles + pendingDemands);

        // 今日下载量（简化：从资源表的下载量无法按日统计，使用最新资源总数替代）
        vo.setTodayResourceDownloads(0);

        // 总浏览量（简化：统计所有资源+文章的浏览量总和，需通过SQL聚合）
        vo.setTotalViews(0);

        return Result.success(vo);
    }

    @GetMapping("/trend")
    public Result<List<TrendVO>> trend(@RequestParam(defaultValue = "7") int days) {
        List<TrendVO> list = userMapper.selectTrend(days);
        return Result.success(list);
    }
}

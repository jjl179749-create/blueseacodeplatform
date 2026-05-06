package org.blueseacode.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.SysBanner;
import org.blueseacode.dao.mapper.SysBannerMapper;
import org.blueseacode.dao.util.PageUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.ADMIN_PREFIX + "/banners")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
@RequiredArgsConstructor
public class BannerController {

    private final SysBannerMapper bannerMapper;

    /**
     * 轮播图列表
     */
    @GetMapping
    public Result<PageResult<SysBanner>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysBanner> p = bannerMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SysBanner>()
                        .orderByDesc(SysBanner::getSort)
                        .orderByDesc(SysBanner::getCreateTime));
        return Result.success(PageUtil.of(p));
    }

    /**
     * 轮播图详情
     */
    @GetMapping("/{id}")
    public Result<SysBanner> detail(@PathVariable Long id) {
        SysBanner banner = bannerMapper.selectById(id);
        if (banner == null) {
            return Result.failed(404, "轮播图不存在");
        }
        return Result.success(banner);
    }

    /**
     * 创建轮播图
     */
    @PostMapping
    public Result<Void> create(@RequestBody SysBanner banner) {
        bannerMapper.insert(banner);
        return Result.success("轮播图创建成功");
    }

    /**
     * 编辑轮播图
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysBanner banner) {
        banner.setId(id);
        bannerMapper.updateById(banner);
        return Result.success("轮播图更新成功");
    }

    /**
     * 删除轮播图
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        return Result.success("轮播图删除成功");
    }
}

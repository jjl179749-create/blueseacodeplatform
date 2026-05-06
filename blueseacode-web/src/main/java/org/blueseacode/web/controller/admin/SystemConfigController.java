package org.blueseacode.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.SysConfig;
import org.blueseacode.dao.mapper.SysConfigMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstant.ADMIN_PREFIX + "/config")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SysConfigMapper configMapper;

    /**
     * 配置列表
     */
    @GetMapping
    public Result<List<SysConfig>> list() {
        return Result.success(configMapper.selectList(null));
    }

    /**
     * 更新配置
     */
    @PutMapping
    public Result<Void> update(@RequestBody List<SysConfig> configs) {
        if (configs != null) {
            for (SysConfig c : configs) {
                configMapper.update(c, new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, c.getConfigKey()));
            }
        }
        return Result.success("配置更新成功");
    }

    /**
     * 根据key获取配置值
     */
    @GetMapping("/{key}")
    public Result<String> getByKey(@PathVariable String key) {
        SysConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        return Result.success(config != null ? config.getConfigValue() : null);
    }

    /**
     * 创建配置
     */
    @PostMapping
    public Result<Void> create(@RequestBody SysConfig config) {
        configMapper.insert(config);
        return Result.success("配置创建成功");
    }
}

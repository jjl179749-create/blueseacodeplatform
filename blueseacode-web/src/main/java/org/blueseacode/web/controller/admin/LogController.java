package org.blueseacode.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.SysLoginLog;
import org.blueseacode.dao.entity.SysOperationLog;
import org.blueseacode.dao.mapper.SysLoginLogMapper;
import org.blueseacode.dao.mapper.SysOperationLogMapper;
import org.blueseacode.dao.util.PageUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.ADMIN_PREFIX + "/logs")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
@RequiredArgsConstructor
public class LogController {

    private final SysOperationLogMapper operationLogMapper;
    private final SysLoginLogMapper loginLogMapper;

    /**
     * 操作日志列表
     */
    @GetMapping("/operations")
    public Result<PageResult<SysOperationLog>> operations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Page<SysOperationLog> p = operationLogMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SysOperationLog>()
                        .eq(module != null && !module.isEmpty(), SysOperationLog::getModule, module)
                        .eq(action != null && !action.isEmpty(), SysOperationLog::getAction, action)
                        .ge(startDate != null && !startDate.isEmpty(), SysOperationLog::getCreateTime, startDate)
                        .le(endDate != null && !endDate.isEmpty(), SysOperationLog::getCreateTime, endDate + " 23:59:59")
                        .orderByDesc(SysOperationLog::getCreateTime));
        return Result.success(PageUtil.of(p));
    }

    /**
     * 登录日志列表
     */
    @GetMapping("/login")
    public Result<PageResult<SysLoginLog>> loginLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer result) {
        Page<SysLoginLog> p = loginLogMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SysLoginLog>()
                        .like(username != null && !username.isEmpty(), SysLoginLog::getUsername, username)
                        .eq(result != null, SysLoginLog::getResult, result)
                        .orderByDesc(SysLoginLog::getCreateTime));
        return Result.success(PageUtil.of(p));
    }
}

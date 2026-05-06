package org.blueseacode.web.controller.portal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.blueseacode.ai.model.TicketCreateRequest;
import org.blueseacode.ai.ticket.TicketService;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.ChatTicket;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.PORTAL_PREFIX + "/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /** 创建工单 */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TicketCreateRequest req,
                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success("工单创建成功", ticketService.create(userId, req));
    }

    /** 我的工单列表 */
    @GetMapping
    public Result<PageResult<ChatTicket>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(ticketService.listByUser(userId, page, size));
    }

    /** 工单详情 */
    @GetMapping("/{id}")
    public Result<ChatTicket> detail(@PathVariable Long id) {
        ChatTicket ticket = ticketService.getDetail(id);
        if (ticket == null) {
            return Result.failed(404, "工单不存在");
        }
        return Result.success(ticket);
    }

    /** 回复工单 */
    @PostMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id,
                               @RequestParam String content,
                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        ticketService.reply(id, userId, content, false);
        return Result.success("回复成功");
    }

    /** 关闭工单 */
    @PostMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id,
                               @RequestParam String reason,
                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        ticketService.close(id, reason);
        return Result.success("工单已关闭");
    }
}

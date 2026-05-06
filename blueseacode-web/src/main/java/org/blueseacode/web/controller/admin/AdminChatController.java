package org.blueseacode.web.controller.admin;

import lombok.RequiredArgsConstructor;
import org.blueseacode.ai.faq.FaqService;
import org.blueseacode.ai.faq.KnowledgeBaseService;
import org.blueseacode.ai.ticket.TicketService;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.ChatFaq;
import org.blueseacode.dao.entity.ChatKnowledgeBase;
import org.blueseacode.dao.entity.ChatTicket;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.ADMIN_PREFIX + "/ai")
@RequiredArgsConstructor
public class AdminChatController {

    private final FaqService faqService;
    private final KnowledgeBaseService kbService;
    private final TicketService ticketService;

    // ==================== FAQ 管理 ====================

    /** FAQ列表 */
    @GetMapping("/faq")
    public Result<PageResult<ChatFaq>> listFaq(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(faqService.list(keyword, category, page, size));
    }

    /** 新增FAQ */
    @PostMapping("/faq")
    public Result<Void> createFaq(@RequestBody ChatFaq faq) {
        faqService.create(faq);
        return Result.success("FAQ创建成功");
    }

    /** 编辑FAQ */
    @PutMapping("/faq/{id}")
    public Result<Void> updateFaq(@PathVariable Long id, @RequestBody ChatFaq faq) {
        faq.setId(id);
        faqService.update(faq);
        return Result.success("FAQ更新成功");
    }

    /** 删除FAQ */
    @DeleteMapping("/faq/{id}")
    public Result<Void> deleteFaq(@PathVariable Long id) {
        faqService.delete(id);
        return Result.success("FAQ删除成功");
    }

    // ==================== 知识库管理 ====================

    /** 知识库列表 */
    @GetMapping("/knowledge")
    public Result<PageResult<ChatKnowledgeBase>> listKnowledge(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(kbService.list(keyword, category, page, size));
    }

    /** 新增知识条目 */
    @PostMapping("/knowledge")
    public Result<Void> createKnowledge(@RequestBody ChatKnowledgeBase kb) {
        kbService.create(kb);
        return Result.success("知识条目创建成功");
    }

    /** 编辑知识条目 */
    @PutMapping("/knowledge/{id}")
    public Result<Void> updateKnowledge(@PathVariable Long id, @RequestBody ChatKnowledgeBase kb) {
        kb.setId(id);
        kbService.update(kb);
        return Result.success("知识条目更新成功");
    }

    /** 删除知识条目 */
    @DeleteMapping("/knowledge/{id}")
    public Result<Void> deleteKnowledge(@PathVariable Long id) {
        kbService.delete(id);
        return Result.success("知识条目删除成功");
    }

    // ==================== 工单管理 ====================

    /** 工单列表 */
    @GetMapping("/tickets")
    public Result<PageResult<ChatTicket>> listTickets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return Result.success(ticketService.listAll(page, size, status));
    }

    /** 工单详情 */
    @GetMapping("/tickets/{id}")
    public Result<ChatTicket> ticketDetail(@PathVariable Long id) {
        ChatTicket ticket = ticketService.getDetail(id);
        if (ticket == null) {
            return Result.failed(404, "工单不存在");
        }
        return Result.success(ticket);
    }

    /** 分配工单 */
    @PutMapping("/tickets/{id}/assign")
    public Result<Void> assignTicket(@PathVariable Long id,
                                      @RequestParam Long assigneeId) {
        ticketService.assign(id, assigneeId);
        return Result.success("工单分配成功");
    }

    /** 客服回复 */
    @PostMapping("/tickets/{id}/reply")
    public Result<Void> replyTicket(@PathVariable Long id,
                                     @RequestParam String content,
                                     @RequestParam(defaultValue = "true") boolean isStaff,
                                     @RequestParam(required = false) Long userId) {
        ticketService.reply(id, userId, content, isStaff);
        return Result.success("回复成功");
    }

    /** 关闭工单 */
    @PostMapping("/tickets/{id}/close")
    public Result<Void> closeTicket(@PathVariable Long id,
                                     @RequestParam String reason) {
        ticketService.close(id, reason);
        return Result.success("工单已关闭");
    }
}

package org.blueseacode.ai.ticket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.entity.ChatTicket;
import org.blueseacode.dao.entity.ChatTicketReply;
import org.blueseacode.dao.entity.SysUser;
import org.blueseacode.dao.mapper.ChatTicketMapper;
import org.blueseacode.dao.mapper.ChatTicketReplyMapper;
import org.blueseacode.dao.mapper.SysUserMapper;
import org.blueseacode.dao.util.PageUtil;
import org.blueseacode.ai.model.TicketCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketService {

    private final ChatTicketMapper ticketMapper;
    private final ChatTicketReplyMapper replyMapper;
    private final SysUserMapper userMapper;

    /** 创建工单 */
    public Long create(Long userId, TicketCreateRequest request) {
        ChatTicket ticket = new ChatTicket();
        ticket.setUserId(userId);
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setStatus("PENDING");
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : 2);
        ticketMapper.insert(ticket);
        return ticket.getId();
    }

    /** 分配工单（客服后台操作） */
    public void assign(Long ticketId, Long assigneeId) {
        ChatTicket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) return;
        ticket.setAssigneeId(assigneeId);
        ticket.setStatus("PROCESSING");
        ticketMapper.updateById(ticket);
    }

    /** 回复工单 */
    public void reply(Long ticketId, Long userId, String content, boolean isStaff) {
        ChatTicketReply reply = new ChatTicketReply();
        reply.setTicketId(ticketId);
        reply.setUserId(userId);
        reply.setContent(content);
        reply.setIsStaff(isStaff ? 1 : 0);
        replyMapper.insert(reply);

        // 用户回复时若工单已解决则恢复为处理中
        ChatTicket ticket = ticketMapper.selectById(ticketId);
        if (ticket != null && !isStaff && "RESOLVED".equals(ticket.getStatus())) {
            ticket.setStatus("PROCESSING");
            ticketMapper.updateById(ticket);
        }
    }

    /** 关闭工单 */
    public void close(Long ticketId, String reason) {
        ChatTicket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) return;
        ticket.setStatus("CLOSED");
        ticket.setCloseReason(reason);
        ticketMapper.updateById(ticket);
    }

    /** 用户查看自己的工单列表 */
    public PageResult<ChatTicket> listByUser(Long userId, int page, int size) {
        Page<ChatTicket> p = ticketMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<ChatTicket>()
                        .eq(ChatTicket::getUserId, userId)
                        .orderByDesc(ChatTicket::getCreateTime));
        return PageUtil.of(p);
    }

    /** 客服查看所有工单 */
    public PageResult<ChatTicket> listAll(int page, int size, String status) {
        Page<ChatTicket> p = ticketMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<ChatTicket>()
                        .eq(status != null && !status.isEmpty(), ChatTicket::getStatus, status)
                        .orderByDesc(ChatTicket::getPriority)
                        .orderByAsc(ChatTicket::getCreateTime));
        return PageUtil.of(p);
    }

    /** 查看工单详情（含回复） */
    public ChatTicket getDetail(Long ticketId) {
        ChatTicket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) return null;

        // 补充创建者昵称
        SysUser creator = userMapper.selectById(ticket.getUserId());
        if (creator != null) ticket.setNickname(creator.getNickname());

        // 补充处理人昵称
        if (ticket.getAssigneeId() != null) {
            SysUser assignee = userMapper.selectById(ticket.getAssigneeId());
            if (assignee != null) ticket.setAssigneeName(assignee.getNickname());
        }

        // 查询回复列表
        List<ChatTicketReply> replies = replyMapper.selectList(
                new LambdaQueryWrapper<ChatTicketReply>()
                        .eq(ChatTicketReply::getTicketId, ticketId)
                        .orderByAsc(ChatTicketReply::getCreateTime));
        ticket.setReplies(replies);

        return ticket;
    }
}

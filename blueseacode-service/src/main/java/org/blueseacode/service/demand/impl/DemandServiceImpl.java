package org.blueseacode.service.demand.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blueseacode.common.enums.ResultCode;
import org.blueseacode.common.exception.BusinessException;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.dto.DemandQueryDTO;
import org.blueseacode.dao.entity.*;
import org.blueseacode.dao.mapper.*;
import org.blueseacode.dao.util.PageUtil;
import org.blueseacode.service.demand.DemandService;
import org.blueseacode.service.demand.model.DemandCreateRequest;
import org.blueseacode.service.message.NotificationService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemandServiceImpl implements DemandService {

    private final DemDemandMapper demandMapper;
    private final DemAttachmentMapper attachmentMapper;
    private final DemOrderMapper orderMapper;
    private final DemProgressMapper progressMapper;
    private final SysUserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final NotificationService notificationService;

    // Redis分布式锁前缀
    private static final String DEMAND_LOCK_KEY = "demand:lock:";

    @Override
    @Transactional
    public Long createDemand(Long userId, DemandCreateRequest request) {
        DemDemand demand = new DemDemand();
        demand.setUserId(userId);
        demand.setTitle(request.getTitle());
        demand.setDescription(request.getDescription());
        demand.setCategory(request.getCategory());
        demand.setBudgetMin(request.getBudgetMin());
        demand.setBudgetMax(request.getBudgetMax());
        demand.setDeadline(request.getDeadline());
        demand.setContact(request.getContact());
        demand.setViewCount(0);
        demand.setOrderCount(0);
        demand.setStatus(0); // 待审核
        demandMapper.insert(demand);

        // 保存附件
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (var dto : request.getAttachments()) {
                DemAttachment attachment = new DemAttachment();
                attachment.setDemandId(demand.getId());
                attachment.setFileName(dto.getFileName());
                attachment.setFileUrl(dto.getFileUrl());
                attachment.setFileSize(dto.getFileSize());
                attachmentMapper.insert(attachment);
            }
        }

        return demand.getId();
    }

    @Override
    @Transactional
    public void updateDemand(Long userId, Long demandId, DemandCreateRequest request) {
        DemDemand demand = demandMapper.selectById(demandId);
        if (demand == null) {
            throw new BusinessException(ResultCode.DEMAND_NOT_FOUND);
        }
        if (!demand.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        // 只有待审状态可编辑
        if (demand.getStatus() != 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "当前状态不可编辑");
        }

        demand.setTitle(request.getTitle());
        demand.setDescription(request.getDescription());
        demand.setCategory(request.getCategory());
        demand.setBudgetMin(request.getBudgetMin());
        demand.setBudgetMax(request.getBudgetMax());
        demand.setDeadline(request.getDeadline());
        demand.setContact(request.getContact());
        demandMapper.updateById(demand);

        // 附件：先删后插
        attachmentMapper.delete(
                new LambdaQueryWrapper<DemAttachment>()
                        .eq(DemAttachment::getDemandId, demandId));
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (var dto : request.getAttachments()) {
                DemAttachment attachment = new DemAttachment();
                attachment.setDemandId(demandId);
                attachment.setFileName(dto.getFileName());
                attachment.setFileUrl(dto.getFileUrl());
                attachment.setFileSize(dto.getFileSize());
                attachmentMapper.insert(attachment);
            }
        }
    }

    @Override
    @Transactional
    public void deleteDemand(Long userId, Long demandId) {
        DemDemand demand = demandMapper.selectById(demandId);
        if (demand == null) {
            throw new BusinessException(ResultCode.DEMAND_NOT_FOUND);
        }
        if (!demand.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        demandMapper.deleteById(demandId); // 逻辑删除
    }

    @Override
    public PageResult<DemDemand> list(DemandQueryDTO query) {
        Page<DemDemand> pageResult = demandMapper.selectDemandPage(
                new Page<>(query.getPage(), query.getSize()), query);

        // 为每条需求填充接单状态
        for (DemDemand demand : pageResult.getRecords()) {
            fillDemandExtra(demand, query.getCurrentUserId());
        }

        return PageUtil.of(pageResult);
    }

    @Override
    public DemDemand getDetail(Long demandId, Long currentUserId) {
        DemDemand demand = demandMapper.selectById(demandId);
        if (demand == null) {
            throw new BusinessException(ResultCode.DEMAND_NOT_FOUND);
        }

        // 浏览量+1
        demandMapper.incrementViewCount(demandId);
        demand.setViewCount(demand.getViewCount() + 1);

        // 补充发布者信息
        SysUser user = userMapper.selectById(demand.getUserId());
        if (user != null) {
            demand.setNickname(user.getNickname());
            demand.setAvatar(user.getAvatar());
        }

        // 补充接单者信息
        if (demand.getTakerId() != null) {
            SysUser taker = userMapper.selectById(demand.getTakerId());
            if (taker != null) {
                demand.setTakerNickname(taker.getNickname());
                demand.setTakerAvatar(taker.getAvatar());
            }
        }

        // 补充附件
        demand.setAttachments(getAttachments(demandId));

        // 补充当前用户是否已接单
        fillDemandExtra(demand, currentUserId);

        return demand;
    }

    @Override
    @Transactional
    public void updateStatus(Long demandId, Integer status, String rejectReason) {
        DemDemand demand = demandMapper.selectById(demandId);
        if (demand == null) {
            throw new BusinessException(ResultCode.DEMAND_NOT_FOUND);
        }

        demand.setStatus(status);
        demand.setRejectReason(rejectReason);
        demandMapper.updateById(demand);

        // 发送审核通知
        String title = status == 1 ? "需求审核通过" : "需求审核未通过";
        String content = status == 1
                ? "您的需求《" + demand.getTitle() + "》已通过审核，现在可以接单了"
                : "您的需求《" + demand.getTitle() + "》未通过审核，原因: " + rejectReason;
        notificationService.send(demand.getUserId(), "AUDIT", title, content,
                demandId, "DEMAND");
    }

    @Override
    @Transactional
    public void takeOrder(Long demandId, Long userId) {
        String lockKey = DEMAND_LOCK_KEY + demandId;
        String lockValue = userId.toString();
        boolean locked = false;

        try {
            // 获取分布式锁，超时3秒自动释放
            locked = Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 3, TimeUnit.SECONDS));

            if (!locked) {
                throw new BusinessException(ResultCode.OPERATION_FAILED, "接单操作过于频繁，请稍后再试");
            }

            // 双重检查：需求状态和接单者
            DemDemand demand = demandMapper.selectById(demandId);
            if (demand == null) {
                throw new BusinessException(ResultCode.DEMAND_NOT_FOUND);
            }
            if (demand.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.OPERATION_FAILED, "不能接自己的需求");
            }
            if (demand.getStatus() != 1) {
                throw new BusinessException(ResultCode.OPERATION_FAILED, "当前需求状态不允许接单");
            }
            if (demand.getTakerId() != null) {
                throw new BusinessException(ResultCode.OPERATION_FAILED, "该需求已被接单");
            }

            // 使用原子SQL更新（防止锁释放后的并发竞争）
            int updated = demandMapper.takeOrder(demandId, userId);
            if (updated <= 0) {
                throw new BusinessException(ResultCode.OPERATION_FAILED, "接单失败，该需求可能已被他人接走");
            }

            // 记录接单记录
            DemOrder order = new DemOrder();
            order.setDemandId(demandId);
            order.setUserId(userId);
            orderMapper.insert(order);

            // 通知发布者
            SysUser taker = userMapper.selectById(userId);
            notificationService.send(demand.getUserId(), "SYSTEM", "需求已被接单",
                    "您的需求《" + demand.getTitle() + "》已被 " +
                            (taker != null ? taker.getNickname() : "用户") + " 接单",
                    demandId, "DEMAND");

            log.info("用户{}成功接单需求{}", userId, demandId);

        } finally {
            // 释放锁（只释放自己持有的锁）
            if (locked) {
                String currentValue = redisTemplate.opsForValue().get(lockKey);
                if (lockValue.equals(currentValue)) {
                    redisTemplate.delete(lockKey);
                }
            }
        }
    }

    @Override
    @Transactional
    public void completeDemand(Long userId, Long demandId) {
        DemDemand demand = demandMapper.selectById(demandId);
        if (demand == null) {
            throw new BusinessException(ResultCode.DEMAND_NOT_FOUND);
        }
        if (!demand.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (demand.getStatus() != 2) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "只有进行中的需求才能标记完成");
        }
        demand.setStatus(3); // 已完成
        demandMapper.updateById(demand);

        // 通知接单者
        notificationService.send(demand.getTakerId(), "SYSTEM", "需求已完成",
                "需求《" + demand.getTitle() + "》已被发布者标记为完成",
                demandId, "DEMAND");
    }

    @Override
    public List<DemAttachment> getAttachments(Long demandId) {
        return attachmentMapper.selectList(
                new LambdaQueryWrapper<DemAttachment>()
                        .eq(DemAttachment::getDemandId, demandId));
    }

    @Override
    public PageResult<DemOrder> getOrders(Long demandId, int page, int size) {
        Page<DemOrder> pageResult = orderMapper.selectOrderPage(
                new Page<>(page, size), demandId);
        return PageUtil.of(pageResult);
    }

    // ===== 私有方法 =====

    private void fillDemandExtra(DemDemand demand, Long currentUserId) {
        if (currentUserId != null) {
            // 判断当前用户是否已接单
            long count = orderMapper.selectCount(
                    new LambdaQueryWrapper<DemOrder>()
                            .eq(DemOrder::getDemandId, demand.getId())
                            .eq(DemOrder::getUserId, currentUserId));
            demand.setIsTaker(count > 0);
        }
    }
}

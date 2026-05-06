package org.blueseacode.service.demand;

import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.dto.DemandQueryDTO;
import org.blueseacode.dao.entity.DemAttachment;
import org.blueseacode.dao.entity.DemDemand;
import org.blueseacode.dao.entity.DemOrder;
import org.blueseacode.service.demand.model.DemandCreateRequest;

import java.util.List;

public interface DemandService {

    /** 创建需求 */
    Long createDemand(Long userId, DemandCreateRequest request);

    /** 编辑需求 */
    void updateDemand(Long userId, Long demandId, DemandCreateRequest request);

    /** 删除需求 */
    void deleteDemand(Long userId, Long demandId);

    /** 分页查询 */
    PageResult<DemDemand> list(DemandQueryDTO query);

    /** 需求详情 */
    DemDemand getDetail(Long demandId, Long currentUserId);

    /** 更新状态（审核） */
    void updateStatus(Long demandId, Integer status, String rejectReason);

    /** 接单（使用Redis分布式锁防止并发） */
    void takeOrder(Long demandId, Long userId);

    /** 确认完成 */
    void completeDemand(Long userId, Long demandId);

    /** 获取需求的附件列表 */
    List<DemAttachment> getAttachments(Long demandId);

    /** 获取需求的接单记录 */
    PageResult<DemOrder> getOrders(Long demandId, int page, int size);
}

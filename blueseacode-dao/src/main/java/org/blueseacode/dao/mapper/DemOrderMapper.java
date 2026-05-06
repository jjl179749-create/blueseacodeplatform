package org.blueseacode.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.blueseacode.dao.entity.DemOrder;

public interface DemOrderMapper extends BaseMapper<DemOrder> {

    /** 分页查询接单记录 */
    Page<DemOrder> selectOrderPage(IPage<?> page, @Param("demandId") Long demandId);
}

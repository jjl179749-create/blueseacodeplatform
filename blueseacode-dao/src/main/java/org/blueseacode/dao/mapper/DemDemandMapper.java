package org.blueseacode.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.blueseacode.dao.dto.DemandQueryDTO;
import org.blueseacode.dao.entity.DemDemand;

public interface DemDemandMapper extends BaseMapper<DemDemand> {

    /** 分页查询需求列表（支持搜索+分类+排序） */
    Page<DemDemand> selectDemandPage(IPage<?> page,
                                      @Param("query") DemandQueryDTO query);

    /** 浏览量+1 */
    @Update("UPDATE dem_demand SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    /** 接单数+1 */
    @Update("UPDATE dem_demand SET order_count = order_count + 1 WHERE id = #{id}")
    void incrementOrderCount(@Param("id") Long id);

    /** 更新接单者ID（原子操作+状态判断，防止并发接单） */
    @Update("UPDATE dem_demand SET taker_id = #{takerId}, status = 2, order_count = order_count + 1 " +
            "WHERE id = #{id} AND status = 1 AND taker_id IS NULL")
    int takeOrder(@Param("id") Long id, @Param("takerId") Long takerId);
}

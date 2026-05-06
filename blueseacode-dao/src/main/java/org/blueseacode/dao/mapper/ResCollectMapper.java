package org.blueseacode.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.blueseacode.dao.entity.ResCollect;

public interface ResCollectMapper extends BaseMapper<ResCollect> {

    /** 查询用户是否已收藏某资源 */
    @Select("SELECT COUNT(1) FROM res_collect WHERE user_id = #{userId} AND resource_id = #{resourceId}")
    long countByUserAndResource(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
}

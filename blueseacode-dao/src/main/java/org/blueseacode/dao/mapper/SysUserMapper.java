package org.blueseacode.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.blueseacode.dao.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface SysUserMapper extends BaseMapper<SysUser> {

    /** 根据用户名查询用户（含已删除） */
    @Select("SELECT * FROM sys_user WHERE username = #{username} LIMIT 1")
    SysUser selectByUsername(@Param("username") String username);

    /** 查询用户的角色编码列表 */
    @Select("SELECT r.role_code FROM sys_user_role ur " +
            "JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /** 按日统计用户注册趋势 */
    @Select("SELECT DATE(create_time) AS date, COUNT(*) AS count " +
            "FROM sys_user " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(create_time) ORDER BY date")
    List<org.blueseacode.dao.dto.TrendVO> selectTrend(@Param("days") int days);
}

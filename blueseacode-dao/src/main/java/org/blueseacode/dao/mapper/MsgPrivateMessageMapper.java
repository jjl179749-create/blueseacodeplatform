package org.blueseacode.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.blueseacode.dao.entity.MsgPrivateMessage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MsgPrivateMessageMapper extends BaseMapper<MsgPrivateMessage> {

    /**
     * 查询最近联系人ID列表
     */
    @Select("SELECT t.contact_id FROM ( " +
            "  SELECT " +
            "    CASE WHEN from_user_id = #{userId} THEN to_user_id ELSE from_user_id END AS contact_id, " +
            "    create_time " +
            "  FROM msg_private_message " +
            "  WHERE from_user_id = #{userId} OR to_user_id = #{userId} " +
            "  ORDER BY create_time DESC " +
            ") t " +
            "GROUP BY t.contact_id " +
            "ORDER BY MAX(t.create_time) DESC " +
            "LIMIT 20")
    List<Long> selectRecentContactUserIds(@Param("userId") Long userId);
}

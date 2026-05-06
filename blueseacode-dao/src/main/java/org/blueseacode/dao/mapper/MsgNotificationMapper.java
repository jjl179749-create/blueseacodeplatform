package org.blueseacode.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.blueseacode.dao.entity.MsgNotification;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface MsgNotificationMapper extends BaseMapper<MsgNotification> {

    @Update("UPDATE msg_notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    void markAllReadByUserId(@Param("userId") Long userId);
}

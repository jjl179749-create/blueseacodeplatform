package org.blueseacode.service.resource;

import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.entity.ResResource;

public interface CollectService {

    /** 收藏资源 */
    void collect(Long userId, Long resourceId);

    /** 取消收藏 */
    void cancel(Long userId, Long resourceId);

    /** 查询用户是否已收藏 */
    boolean isCollected(Long userId, Long resourceId);

    /** 获取用户收藏的资源列表（分页） */
    PageResult<ResResource> listByUser(Long userId, int page, int size);
}

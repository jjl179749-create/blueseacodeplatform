package org.blueseacode.service.resource;

import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.entity.ResComment;
import org.blueseacode.service.resource.model.CommentCreateRequest;

public interface CommentService {

    /** 获取资源评论列表（分页） */
    PageResult<ResComment> list(Long resourceId, int page, int size);

    /** 发表评论 */
    void create(Long userId, CommentCreateRequest request);

    /** 删除评论 */
    void delete(Long userId, Long commentId);
}

package org.blueseacode.service.resource;

import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.dto.ResourceQueryDTO;
import org.blueseacode.dao.entity.ResCategory;
import org.blueseacode.dao.entity.ResResource;
import org.blueseacode.dao.entity.ResTag;
import org.blueseacode.service.resource.model.ResourceCreateRequest;

import java.util.List;

public interface ResourceService {

    /** 发布资源 */
    Long createResource(Long userId, ResourceCreateRequest request);

    /** 编辑资源 */
    void updateResource(Long userId, Long resourceId, ResourceCreateRequest request);

    /** 删除资源 */
    void deleteResource(Long userId, Long resourceId);

    /** 分页查询资源列表 */
    PageResult<ResResource> list(ResourceQueryDTO query);

    /** 获取资源详情 */
    ResResource getDetail(Long resourceId, Long currentUserId);

    /** 下载资源（积分校验） */
    String downloadResource(Long userId, Long resourceId);

    /** 更新资源状态（审核/上下架，管理员用） */
    void updateStatus(Long resourceId, Integer status, String rejectReason);

    /** 获取所有分类 */
    List<ResCategory> listCategories();

    /** 获取所有标签 */
    List<ResTag> listTags();
}

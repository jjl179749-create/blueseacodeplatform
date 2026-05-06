package org.blueseacode.service.resource;

import org.blueseacode.dao.entity.ResTag;

import java.util.List;

public interface ResourceTagService {

    /** 获取所有标签 */
    List<ResTag> listAll();

    /** 根据资源ID获取关联标签 */
    List<String> getTagsByResourceId(Long resourceId);

    /** 创建标签 */
    Long create(String name);

    /** 删除标签 */
    void delete(Long id);
}

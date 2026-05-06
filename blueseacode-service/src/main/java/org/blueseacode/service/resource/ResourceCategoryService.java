package org.blueseacode.service.resource;

import org.blueseacode.dao.entity.ResCategory;

import java.util.List;

public interface ResourceCategoryService {

    /** 获取所有分类 */
    List<ResCategory> listAll();

    /** 根据ID获取分类 */
    ResCategory getById(Long id);

    /** 创建分类（管理员） */
    Long create(String name, String icon, Long parentId, Integer sort);

    /** 更新分类（管理员） */
    void update(Long id, String name, String icon, Integer sort);

    /** 删除分类（管理员） */
    void delete(Long id);
}

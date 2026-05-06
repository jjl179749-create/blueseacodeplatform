package org.blueseacode.service.resource.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.blueseacode.dao.entity.ResCategory;
import org.blueseacode.dao.mapper.ResCategoryMapper;
import org.blueseacode.service.resource.ResourceCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceCategoryServiceImpl implements ResourceCategoryService {

    private final ResCategoryMapper categoryMapper;

    @Override
    public List<ResCategory> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<ResCategory>()
                        .orderByAsc(ResCategory::getSort));
    }

    @Override
    public ResCategory getById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public Long create(String name, String icon, Long parentId, Integer sort) {
        ResCategory category = new ResCategory();
        category.setName(name);
        category.setIcon(icon);
        category.setParentId(parentId);
        category.setSort(sort != null ? sort : 0);
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void update(Long id, String name, String icon, Integer sort) {
        ResCategory category = new ResCategory();
        category.setId(id);
        category.setName(name);
        category.setIcon(icon);
        category.setSort(sort);
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}

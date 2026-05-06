package org.blueseacode.service.resource.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.blueseacode.dao.entity.ResTag;
import org.blueseacode.dao.mapper.ResResourceTagMapper;
import org.blueseacode.dao.mapper.ResTagMapper;
import org.blueseacode.service.resource.ResourceTagService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceTagServiceImpl implements ResourceTagService {

    private final ResTagMapper tagMapper;
    private final ResResourceTagMapper resourceTagMapper;

    @Override
    public List<ResTag> listAll() {
        return tagMapper.selectList(null);
    }

    @Override
    public List<String> getTagsByResourceId(Long resourceId) {
        return resourceTagMapper.selectList(
                        new LambdaQueryWrapper<org.blueseacode.dao.entity.ResResourceTag>()
                                .eq(org.blueseacode.dao.entity.ResResourceTag::getResourceId, resourceId))
                .stream()
                .map(rt -> {
                    ResTag tag = tagMapper.selectById(rt.getTagId());
                    return tag != null ? tag.getName() : null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Long create(String name) {
        ResTag tag = new ResTag();
        tag.setName(name);
        tagMapper.insert(tag);
        return tag.getId();
    }

    @Override
    public void delete(Long id) {
        tagMapper.deleteById(id);
    }
}

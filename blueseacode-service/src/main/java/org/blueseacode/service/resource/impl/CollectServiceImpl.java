package org.blueseacode.service.resource.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.enums.ResultCode;
import org.blueseacode.common.exception.BusinessException;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.entity.ResCollect;
import org.blueseacode.dao.entity.ResResource;
import org.blueseacode.dao.mapper.ResCollectMapper;
import org.blueseacode.dao.mapper.ResResourceMapper;
import org.blueseacode.dao.util.PageUtil;
import org.blueseacode.service.resource.CollectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectServiceImpl implements CollectService {

    private final ResCollectMapper collectMapper;
    private final ResResourceMapper resourceMapper;

    @Override
    @Transactional
    public void collect(Long userId, Long resourceId) {
        // 检查是否已收藏
        long count = collectMapper.countByUserAndResource(userId, resourceId);
        if (count > 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "已收藏过该资源");
        }

        ResCollect collect = new ResCollect();
        collect.setUserId(userId);
        collect.setResourceId(resourceId);
        collectMapper.insert(collect);

        // 更新收藏计数
        resourceMapper.updateById(new ResResource() {{
            setId(resourceId);
            setCollectCount((int) (resourceMapper.selectById(resourceId).getCollectCount() + 1));
        }});
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long resourceId) {
        collectMapper.delete(
                new LambdaQueryWrapper<ResCollect>()
                        .eq(ResCollect::getUserId, userId)
                        .eq(ResCollect::getResourceId, resourceId));

        // 重新统计收藏数
        long count = collectMapper.selectCount(
                new LambdaQueryWrapper<ResCollect>()
                        .eq(ResCollect::getResourceId, resourceId));
        ResResource res = new ResResource();
        res.setId(resourceId);
        res.setCollectCount((int) count);
        resourceMapper.updateById(res);
    }

    @Override
    public boolean isCollected(Long userId, Long resourceId) {
        return collectMapper.countByUserAndResource(userId, resourceId) > 0;
    }

    @Override
    public PageResult<ResResource> listByUser(Long userId, int page, int size) {
        // 查询用户收藏的资源ID列表
        Page<ResCollect> collectPage = collectMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<ResCollect>()
                        .eq(ResCollect::getUserId, userId)
                        .orderByDesc(ResCollect::getCreateTime));

        List<Long> resourceIds = collectPage.getRecords().stream()
                .map(ResCollect::getResourceId)
                .collect(Collectors.toList());

        // 查询资源详情
        List<ResResource> resources = resourceIds.isEmpty()
                ? List.of()
                : resourceMapper.selectBatchIds(resourceIds);

        return new PageResult<>(resources, collectPage.getTotal(), page, size);
    }
}

package org.blueseacode.service.resource.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.enums.ResultCode;
import org.blueseacode.common.exception.BusinessException;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.dto.ResourceQueryDTO;
import org.blueseacode.dao.entity.*;
import org.blueseacode.dao.mapper.*;
import org.blueseacode.dao.util.PageUtil;
import org.blueseacode.service.message.NotificationService;
import org.blueseacode.service.points.PointsService;
import org.blueseacode.service.resource.ResourceService;
import org.blueseacode.service.resource.model.ResourceCreateRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResResourceMapper resourceMapper;
    private final ResCategoryMapper categoryMapper;
    private final ResTagMapper tagMapper;
    private final ResResourceTagMapper resourceTagMapper;
    private final SysUserMapper userMapper;
    private final ResCollectMapper collectMapper;
    private final PointsService pointsService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    @CacheEvict(value = "tags", key = "'resource_tags'", condition = "#request.tagIds != null && !#request.tagIds.isEmpty()")
    public Long createResource(Long userId, ResourceCreateRequest request) {
        // 1. 保存资源
        ResResource resource = new ResResource();
        resource.setUserId(userId);
        resource.setTitle(request.getTitle());
        resource.setDescription(request.getDescription());
        resource.setCoverImage(request.getCoverImage());
        resource.setCategoryId(request.getCategoryId());
        resource.setFileUrl(request.getFileUrl());
        resource.setFileName(request.getFileName());
        resource.setDownloadPoints(request.getDownloadPoints() != null ? request.getDownloadPoints() : 0);
        resource.setStatus(0);  // 待审核
        resource.setDownloadCount(0);
        resource.setViewCount(0);
        resource.setCommentCount(0);
        resource.setCollectCount(0);
        resource.setSort(0);
        resourceMapper.insert(resource);

        // 2. 保存标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                ResResourceTag rt = new ResResourceTag();
                rt.setResourceId(resource.getId());
                rt.setTagId(tagId);
                resourceTagMapper.insert(rt);
            }
        }

        return resource.getId();
    }

    @Override
    @Transactional
    public void updateResource(Long userId, Long resourceId, ResourceCreateRequest request) {
        ResResource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }
        // 只能编辑自己的资源
        if (!resource.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        // 只能编辑待审核状态的资源
        if (resource.getStatus() != 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "只能编辑待审核状态的资源");
        }

        resource.setTitle(request.getTitle());
        resource.setDescription(request.getDescription());
        resource.setCoverImage(request.getCoverImage());
        resource.setCategoryId(request.getCategoryId());
        resource.setFileUrl(request.getFileUrl());
        resource.setFileName(request.getFileName());
        resource.setDownloadPoints(request.getDownloadPoints() != null ? request.getDownloadPoints() : 0);
        resourceMapper.updateById(resource);

        // 更新标签关联：先删后插
        resourceTagMapper.delete(
                new LambdaQueryWrapper<ResResourceTag>()
                        .eq(ResResourceTag::getResourceId, resourceId));
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                ResResourceTag rt = new ResResourceTag();
                rt.setResourceId(resourceId);
                rt.setTagId(tagId);
                resourceTagMapper.insert(rt);
            }
        }
    }

    @Override
    @Transactional
    public void deleteResource(Long userId, Long resourceId) {
        ResResource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }
        // 只能删除自己的资源
        if (!resource.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        resourceMapper.deleteById(resourceId);  // 逻辑删除
    }

    @Override
    public PageResult<ResResource> list(ResourceQueryDTO query) {
        // 查询资源分页
        Page<ResResource> pageResult = resourceMapper.selectResourcePage(
                new Page<>(query.getPage(), query.getSize()), query);

        // 为每条资源填充标签
        for (ResResource resource : pageResult.getRecords()) {
            List<String> tags = getTagsByResourceId(resource.getId());
            resource.setTags(tags);

            // 判断当前用户是否已收藏
            if (query.getCurrentUserId() != null) {
                long count = collectMapper.countByUserAndResource(
                        query.getCurrentUserId(), resource.getId());
                resource.setIsCollected(count > 0);
            }
        }

        return PageUtil.of(pageResult);
    }

    @Override
    public ResResource getDetail(Long resourceId, Long currentUserId) {
        ResResource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }

        // 浏览量+1
        resourceMapper.incrementViewCount(resourceId);
        resource.setViewCount(resource.getViewCount() + 1);

        // 补充关联信息
        SysUser user = userMapper.selectById(resource.getUserId());
        resource.setNickname(user != null ? user.getNickname() : null);
        resource.setAvatar(user != null ? user.getAvatar() : null);

        ResCategory category = categoryMapper.selectById(resource.getCategoryId());
        resource.setCategoryName(category != null ? category.getName() : null);

        // 补充标签
        List<String> tags = getTagsByResourceId(resourceId);
        resource.setTags(tags);

        // 当前用户是否已收藏
        if (currentUserId != null) {
            long count = collectMapper.countByUserAndResource(currentUserId, resourceId);
            resource.setIsCollected(count > 0);
        }

        return resource;
    }

    @Override
    @Transactional
    public String downloadResource(Long userId, Long resourceId) {
        ResResource resource = resourceMapper.selectById(resourceId);
        if (resource == null || resource.getStatus() != 1) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }
        // 不允许下载自己的资源（无需扣积分）
        if (resource.getUserId().equals(userId)) {
            resourceMapper.incrementDownloadCount(resourceId);
            return resource.getFileUrl();
        }

        // 积分校验
        if (resource.getDownloadPoints() > 0) {
            SysUser user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND);
            }
            if (user.getScore() < resource.getDownloadPoints()) {
                throw new BusinessException(ResultCode.POINTS_INSUFFICIENT);
            }
            // 扣除下载者积分
            pointsService.deduct(userId, resource.getDownloadPoints(),
                    "下载资源: " + resource.getTitle());
            // 给资源发布者增加积分（80%）
            if (!resource.getUserId().equals(userId)) {
                pointsService.add(resource.getUserId(),
                        (int) (resource.getDownloadPoints() * 0.8),
                        "资源被下载: " + resource.getTitle());
            }
        }

        // 更新下载量
        resourceMapper.incrementDownloadCount(resourceId);
        return resource.getFileUrl();
    }

    @Override
    @Transactional
    public void updateStatus(Long resourceId, Integer status, String rejectReason) {
        ResResource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }

        resource.setStatus(status);
        resource.setRejectReason(rejectReason);
        resourceMapper.updateById(resource);

        // 发送审核结果通知
        String title = status == 1 ? "资源审核通过" : "资源审核未通过";
        String content = status == 1
                ? "您的资源《" + resource.getTitle() + "》已通过审核"
                : "您的资源《" + resource.getTitle() + "》未通过审核，原因: " + rejectReason;
        notificationService.send(resource.getUserId(), "AUDIT", title, content,
                resourceId, "RESOURCE");
    }

    @Override
    @Cacheable(value = "categories", key = "'resource_categories'")
    public List<ResCategory> listCategories() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<ResCategory>()
                        .orderByAsc(ResCategory::getSort));
    }

    @Override
    @Cacheable(value = "tags", key = "'resource_tags'")
    public List<ResTag> listTags() {
        return tagMapper.selectList(null);
    }

    // ===== 私有方法 =====

    private List<String> getTagsByResourceId(Long resourceId) {
        return resourceTagMapper.selectList(
                        new LambdaQueryWrapper<ResResourceTag>()
                                .eq(ResResourceTag::getResourceId, resourceId))
                .stream()
                .map(rt -> {
                    ResTag tag = tagMapper.selectById(rt.getTagId());
                    return tag != null ? tag.getName() : null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }
}

package org.blueseacode.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.enums.ResultCode;
import org.blueseacode.common.exception.BusinessException;
import org.blueseacode.dao.entity.ArtArticle;
import org.blueseacode.dao.entity.DemDemand;
import org.blueseacode.dao.entity.ResResource;
import org.blueseacode.dao.entity.SysUser;
import org.blueseacode.dao.mapper.ArtArticleMapper;
import org.blueseacode.dao.mapper.DemDemandMapper;
import org.blueseacode.dao.mapper.ResResourceMapper;
import org.blueseacode.dao.mapper.SysUserMapper;
import org.blueseacode.dao.mapper.SysUserRoleMapper;
import org.blueseacode.service.user.UserService;
import org.blueseacode.service.user.model.PasswordUpdateRequest;
import org.blueseacode.service.user.model.UserProfileUpdateRequest;
import org.blueseacode.service.user.model.UserProfileVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final ResResourceMapper resourceMapper;
    private final ArtArticleMapper articleMapper;
    private final DemDemandMapper demandMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileVO getProfile(Long userId) {
        SysUser user = getUserOrThrow(userId);
        UserProfileVO vo = convertToVO(user);

        vo.setResourceCount(resourceMapper.selectCount(
                new LambdaQueryWrapper<ResResource>()
                        .eq(ResResource::getUserId, userId)
                        .eq(ResResource::getStatus, 1)).intValue());
        vo.setArticleCount(articleMapper.selectCount(
                new LambdaQueryWrapper<ArtArticle>()
                        .eq(ArtArticle::getUserId, userId)
                        .eq(ArtArticle::getStatus, 2)).intValue());
        vo.setDemandCount(demandMapper.selectCount(
                new LambdaQueryWrapper<DemDemand>()
                        .eq(DemDemand::getUserId, userId)
                        .eq(DemDemand::getStatus, 1)).intValue());

        // 获取角色
        vo.setRoles(userMapper.selectRoleCodesByUserId(userId));
        return vo;
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        SysUser user = getUserOrThrow(userId);
        UserProfileVO vo = convertToVO(user);

        vo.setResourceCount(resourceMapper.selectCount(
                new LambdaQueryWrapper<ResResource>()
                        .eq(ResResource::getUserId, userId)
                        .eq(ResResource::getStatus, 1)).intValue());
        vo.setArticleCount(articleMapper.selectCount(
                new LambdaQueryWrapper<ArtArticle>()
                        .eq(ArtArticle::getUserId, userId)
                        .eq(ArtArticle::getStatus, 2)).intValue());
        vo.setDemandCount(demandMapper.selectCount(
                new LambdaQueryWrapper<DemDemand>()
                        .eq(DemDemand::getUserId, userId)
                        .eq(DemDemand::getStatus, 1)).intValue());

        // 他人主页不展示敏感信息
        vo.setEmail(null);
        vo.setPhone(null);
        vo.setRoles(null);
        return vo;
    }

    @Override
    public void updateProfile(Long userId, UserProfileUpdateRequest request) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setNickname(request.getNickname());
        user.setBio(request.getBio());
        user.setGithub(request.getGithub());
        user.setWebsite(request.getWebsite());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        userMapper.updateById(user);
    }

    @Override
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        SysUser user = getUserOrThrow(userId);

        // 校验原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 更新新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public String updateAvatar(Long userId, String avatarUrl) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
        return avatarUrl;
    }

    // ===== 私有方法 =====

    private SysUser getUserOrThrow(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    private UserProfileVO convertToVO(SysUser user) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setBio(user.getBio());
        vo.setGithub(user.getGithub());
        vo.setWebsite(user.getWebsite());
        vo.setScore(user.getScore());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setLastLoginTime(user.getLastLoginTime());
        return vo;
    }
}

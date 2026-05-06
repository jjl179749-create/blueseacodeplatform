package org.blueseacode.security.impl;

import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.enums.ResultCode;
import org.blueseacode.common.exception.BusinessException;
import org.blueseacode.dao.entity.SysUser;
import org.blueseacode.dao.entity.SysUserRole;
import org.blueseacode.dao.mapper.SysUserMapper;
import org.blueseacode.dao.mapper.SysUserRoleMapper;
import org.blueseacode.security.jwt.JwtTokenProvider;
import org.blueseacode.security.model.LoginRequest;
import org.blueseacode.security.model.RegisterRequest;
import org.blueseacode.security.model.TokenResponse;
import org.blueseacode.security.service.AuthService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 1. 检查用户名唯一性
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 2. 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setScore(0);
        user.setStatus(0);
        userMapper.insert(user);

        // 3. 赋予默认角色 ROLE_USER (id=5)
        userRoleMapper.insert(new SysUserRole(user.getId(), 5L));
    }

    @Override
    public TokenResponse login(LoginRequest request, String ip) {
        // 1. 查询用户
        SysUser user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 检查状态
        if (user.getStatus() == 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 3. 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 4. 获取角色
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());

        // 5. 生成 Token
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getUsername(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // 6. 更新最后登录信息
        user.setLastLoginIp(ip);
        user.setLastLoginTime(java.time.LocalDateTime.now());
        userMapper.updateById(user);

        return new TokenResponse(accessToken, refreshToken,
                user.getId(), user.getUsername(), roles);
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        // 1. 校验 RefreshToken
        if (!jwtTokenProvider.validateToken(refreshToken) ||
                !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_REFRESH_FAILED);
        }

        // 2. 获取用户信息重新生成
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 1) {
            throw new BusinessException(ResultCode.TOKEN_REFRESH_FAILED);
        }

        List<String> roles = userMapper.selectRoleCodesByUserId(userId);
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                userId, user.getUsername(), roles);
        long remainingMs = jwtTokenProvider.getRefreshTokenRemainingMs(refreshToken);

        // 如果剩余时间不足7天，刷新RefreshToken
        String newRefreshToken = refreshToken;
        if (remainingMs < 7 * 24 * 60 * 60 * 1000L) {
            newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);
        }

        return new TokenResponse(newAccessToken, newRefreshToken,
                userId, user.getUsername(), roles);
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        // 将 Token 加入 Redis 黑名单，有效期到 Token 过期
        if (accessToken != null) {
            long accessExp = jwtTokenProvider.getExpiration(accessToken).getTime()
                    - System.currentTimeMillis();
            if (accessExp > 0) {
                redisTemplate.opsForValue().set(
                        AppConstant.CACHE_TOKEN_BLACKLIST + accessToken,
                        "1", accessExp, TimeUnit.MILLISECONDS);
            }
        }
        if (refreshToken != null) {
            long refreshExp = jwtTokenProvider.getExpiration(refreshToken).getTime()
                    - System.currentTimeMillis();
            if (refreshExp > 0) {
                redisTemplate.opsForValue().set(
                        AppConstant.CACHE_TOKEN_BLACKLIST + refreshToken,
                        "1", refreshExp, TimeUnit.MILLISECONDS);
            }
        }
    }
}

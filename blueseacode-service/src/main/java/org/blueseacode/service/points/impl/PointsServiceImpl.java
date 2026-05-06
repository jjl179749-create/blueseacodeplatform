package org.blueseacode.service.points.impl;

import lombok.RequiredArgsConstructor;
import org.blueseacode.common.enums.ResultCode;
import org.blueseacode.common.exception.BusinessException;
import org.blueseacode.dao.entity.SysUser;
import org.blueseacode.dao.mapper.SysUserMapper;
import org.blueseacode.service.points.PointsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final SysUserMapper userMapper;

    @Override
    @Transactional
    public void add(Long userId, int points, String reason) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        userMapper.updateById(new SysUser() {{
            setId(userId);
            setScore(user.getScore() + points);
        }});
    }

    @Override
    @Transactional
    public void deduct(Long userId, int points, String reason) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getScore() < points) {
            throw new BusinessException(ResultCode.POINTS_INSUFFICIENT);
        }
        userMapper.updateById(new SysUser() {{
            setId(userId);
            setScore(user.getScore() - points);
        }});
    }

    @Override
    public int getPoints(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user.getScore();
    }
}

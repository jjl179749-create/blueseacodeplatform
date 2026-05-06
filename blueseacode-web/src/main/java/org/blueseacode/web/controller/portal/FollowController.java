package org.blueseacode.web.controller.portal;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.SysUser;
import org.blueseacode.websocket.service.FollowService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(AppConstant.PORTAL_PREFIX + "/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * 关注用户
     */
    @PostMapping("/{userId}")
    public Result<Void> follow(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        followService.follow(currentUserId, userId);
        return Result.success("关注成功");
    }

    /**
     * 取消关注
     */
    @DeleteMapping("/{userId}")
    public Result<Void> unfollow(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        followService.unfollow(currentUserId, userId);
        return Result.success("取消关注");
    }

    /**
     * 获取粉丝数
     */
    @GetMapping("/{userId}/followers/count")
    public Result<Long> followerCount(@PathVariable Long userId) {
        return Result.success(followService.getFollowerCount(userId));
    }

    /**
     * 获取关注数
     */
    @GetMapping("/{userId}/followees/count")
    public Result<Long> followeeCount(@PathVariable Long userId) {
        return Result.success(followService.getFolloweeCount(userId));
    }

    /**
     * 判断当前用户是否已关注
     */
    @GetMapping("/check/{userId}")
    public Result<Boolean> isFollowing(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(followService.isFollowing(currentUserId, userId));
    }

    /**
     * 获取当前用户关注的用户列表
     */
    @GetMapping("/followees")
    public Result<List<SysUser>> followees(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(followService.listFollowees(currentUserId));
    }

    /**
     * 获取当前用户的粉丝列表
     */
    @GetMapping("/followers")
    public Result<List<SysUser>> followers(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(followService.listFollowers(currentUserId));
    }

    /**
     * 获取指定用户的关注/粉丝数量
     */
    @GetMapping("/count/{userId}")
    public Result<Map<String, Integer>> count(@PathVariable Long userId) {
        Map<String, Integer> map = new HashMap<>();
        map.put("followeeCount", followService.listFolloweeIds(userId).size());
        map.put("followerCount", followService.listFollowerIds(userId).size());
        return Result.success(map);
    }
}

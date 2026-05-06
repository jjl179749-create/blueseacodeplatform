package org.blueseacode.web.controller.portal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.dto.ArticleQueryDTO;
import org.blueseacode.dao.entity.ArtArticle;
import org.blueseacode.dao.entity.ArtCategory;
import org.blueseacode.dao.entity.ArtTag;
import org.blueseacode.service.article.*;
import org.blueseacode.service.article.model.ArticleCreateRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstant.PORTAL_PREFIX + "/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleLikeService likeService;
    private final ArticleCollectService collectService;

    /**
     * 文章列表（分页+搜索+分类+排序）
     */
    @GetMapping
    public Result<PageResult<ArtArticle>> list(ArticleQueryDTO query,
                                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        query.setCurrentUserId(userId);
        return Result.success(articleService.list(query));
    }

    /**
     * 文章详情
     */
    @GetMapping("/{id}")
    public Result<ArtArticle> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(articleService.getDetail(id, userId));
    }

    /**
     * 发布文章
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ArticleCreateRequest request,
                                HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success("发布成功，等待审核", articleService.createArticle(userId, request));
    }

    /**
     * 编辑文章
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                                @Valid @RequestBody ArticleCreateRequest request,
                                HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        articleService.updateArticle(userId, id, request);
        return Result.success("更新成功");
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        articleService.deleteArticle(userId, id);
        return Result.success("删除成功");
    }

    /**
     * 获取用户的文章列表（我的发布）
     */
    @GetMapping("/my")
    public Result<PageResult<ArtArticle>> myArticles(ArticleQueryDTO query,
                                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        query.setCurrentUserId(userId);
        query.setUserId(userId);
        // 用户查看自己的文章时不过滤状态
        query.setStatus(null);
        return Result.success(articleService.list(query));
    }

    // ===== 点赞 =====

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        likeService.like(userId, id);
        return Result.success();
    }

    @DeleteMapping("/{id}/like")
    public Result<Void> cancelLike(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        likeService.cancel(userId, id);
        return Result.success();
    }

    // ===== 收藏 =====

    @PostMapping("/{id}/collect")
    public Result<Void> collect(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        collectService.collect(userId, id);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/{id}/collect")
    public Result<Void> cancelCollect(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        collectService.cancel(userId, id);
        return Result.success("取消收藏");
    }

    // ===== 分类 & 标签 =====

    @GetMapping("/categories")
    public Result<List<ArtCategory>> categories() {
        return Result.success(articleService.listCategories());
    }

    @GetMapping("/tags")
    public Result<List<ArtTag>> tags() {
        return Result.success(articleService.listTags());
    }

    // ===== 热门文章 =====

    @GetMapping("/hot")
    public Result<List<ArtArticle>> hot() {
        return Result.success(articleService.getHotArticles(10));
    }
}

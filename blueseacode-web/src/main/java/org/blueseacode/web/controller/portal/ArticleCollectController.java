package org.blueseacode.web.controller.portal;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.ArtArticle;
import org.blueseacode.service.article.ArticleCollectService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.PORTAL_PREFIX + "/article-collects")
@RequiredArgsConstructor
public class ArticleCollectController {

    private final ArticleCollectService articleCollectService;

    /**
     * 收藏文章
     */
    @PostMapping("/articles/{articleId}")
    public Result<Void> collect(@PathVariable Long articleId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        articleCollectService.collect(userId, articleId);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/articles/{articleId}")
    public Result<Void> cancel(@PathVariable Long articleId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        articleCollectService.cancel(userId, articleId);
        return Result.success("取消收藏");
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/articles/{articleId}/check")
    public Result<Boolean> check(@PathVariable Long articleId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(articleCollectService.isCollected(userId, articleId));
    }

    /**
     * 获取我已收藏的文章列表
     */
    @GetMapping("/articles")
    public Result<PageResult<ArtArticle>> myCollectedArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(articleCollectService.listByUser(userId, page, size));
    }
}

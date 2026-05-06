package org.blueseacode.web.controller.portal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.ArtComment;
import org.blueseacode.service.article.ArticleCommentService;
import org.blueseacode.service.article.model.ArticleCommentCreateRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.PORTAL_PREFIX + "/article-comments")
@RequiredArgsConstructor
public class ArticleCommentController {

    private final ArticleCommentService commentService;

    /**
     * 获取文章评论列表
     */
    @GetMapping
    public Result<PageResult<ArtComment>> list(
            @RequestParam Long articleId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.list(articleId, page, size));
    }

    /**
     * 发表评论
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ArticleCommentCreateRequest request,
                                HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        commentService.create(userId, request);
        return Result.success("评论成功");
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute(AppConstant.CURRENT_USER_ID);
        commentService.delete(userId, id);
        return Result.success("删除成功");
    }
}

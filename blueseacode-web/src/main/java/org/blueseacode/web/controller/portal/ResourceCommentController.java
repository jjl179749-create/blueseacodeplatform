package org.blueseacode.web.controller.portal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.ResComment;
import org.blueseacode.service.resource.CommentService;
import org.blueseacode.service.resource.model.CommentCreateRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.PORTAL_PREFIX + "/comments")
@RequiredArgsConstructor
public class ResourceCommentController {

    private final CommentService commentService;

    /**
     * 获取资源评论列表
     */
    @GetMapping
    public Result<PageResult<ResComment>> list(
            @RequestParam Long resourceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.list(resourceId, page, size));
    }

    /**
     * 发表评论
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CommentCreateRequest request,
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

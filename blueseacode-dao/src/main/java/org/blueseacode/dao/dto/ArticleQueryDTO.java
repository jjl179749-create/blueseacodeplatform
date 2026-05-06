package org.blueseacode.dao.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.blueseacode.common.response.PageParam;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleQueryDTO extends PageParam {

    /** 搜索关键词 */
    private String keyword;

    /** 分类筛选 */
    private Long categoryId;

    /** 标签ID列表筛选 */
    private List<Long> tagIds;

    /** 排序: latest(最新)/hottest(最热) */
    private String sortBy;

    /** 状态筛选(管理端使用，用户端默认只查已通过) */
    private Integer status;

    /** 当前用户ID（用于判断是否点赞/收藏） */
    private Long currentUserId;

    /** 发布者用户ID（用于查询指定用户的文章） */
    private Long userId;
}

package org.blueseacode.dao.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.blueseacode.common.response.PageParam;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceQueryDTO extends PageParam {

    /** 搜索关键词 */
    private String keyword;

    /** 分类筛选 */
    private Long categoryId;

    /** 标签ID列表筛选 */
    private List<Long> tagIds;

    /** 排序: latest(最新)/popular(最热)/download(最多下载) */
    private String sortBy;

    /** 最小积分 */
    private Integer minPoints;

    /** 最大积分 */
    private Integer maxPoints;

    /** 状态筛选(管理端使用，用户端默认只查已通过) */
    private Integer status;

    /** 当前用户ID（用于判断是否收藏） */
    private Long currentUserId;

    /** 发布者用户ID（用于查询指定用户的资源） */
    private Long userId;
}

package org.blueseacode.service.article;

import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.dto.ArticleQueryDTO;
import org.blueseacode.dao.entity.ArtArticle;
import org.blueseacode.dao.entity.ArtCategory;
import org.blueseacode.dao.entity.ArtTag;
import org.blueseacode.service.article.model.ArticleCreateRequest;

import java.util.List;

public interface ArticleService {

    /** 创建文章（草稿或提交审核） */
    Long createArticle(Long userId, ArticleCreateRequest request);

    /** 编辑文章 */
    void updateArticle(Long userId, Long articleId, ArticleCreateRequest request);

    /** 删除文章 */
    void deleteArticle(Long userId, Long articleId);

    /** 分页查询 */
    PageResult<ArtArticle> list(ArticleQueryDTO query);

    /** 文章详情 */
    ArtArticle getDetail(Long articleId, Long currentUserId);

    /** 更新状态（审核） */
    void updateStatus(Long articleId, Integer status, String rejectReason);

    /** 获取热门文章 */
    List<ArtArticle> getHotArticles(int limit);

    /** 获取所有分类 */
    List<ArtCategory> listCategories();

    /** 获取所有标签 */
    List<ArtTag> listTags();
}

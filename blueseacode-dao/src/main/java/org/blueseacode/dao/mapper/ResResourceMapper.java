package org.blueseacode.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.blueseacode.dao.dto.ResourceQueryDTO;
import org.blueseacode.dao.entity.ResResource;

public interface ResResourceMapper extends BaseMapper<ResResource> {

    /** 分页查询资源列表（支持搜索+分类+排序） */
    Page<ResResource> selectResourcePage(IPage<?> page,
                                         @Param("query") ResourceQueryDTO query);

    /** 更新下载量+1 */
    @Update("UPDATE res_resource SET download_count = download_count + 1 WHERE id = #{id}")
    void incrementDownloadCount(@Param("id") Long id);

    /** 更新浏览量+1 */
    @Update("UPDATE res_resource SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    /** 更新评论数+1 */
    @Update("UPDATE res_resource SET comment_count = comment_count + 1 WHERE id = #{id}")
    void incrementCommentCount(@Param("id") Long id);

    /** 更新评论数-1 */
    @Update("UPDATE res_resource SET comment_count = GREATEST(comment_count - 1, 0) WHERE id = #{id}")
    void decrementCommentCount(@Param("id") Long id);
}

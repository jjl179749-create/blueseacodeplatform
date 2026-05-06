package org.blueseacode.dao.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.blueseacode.common.response.PageResult;

/**
 * MyBatis-Plus 分页工具类
 * 负责将 MyBatis-Plus Page 转换为统一分页响应 PageResult
 */
public class PageUtil {

    /**
     * 将 MyBatis-Plus Page 转换为 PageResult
     *
     * @param page MyBatis-Plus 分页对象
     * @param <T>  数据类型
     * @return 统一分页响应
     */
    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize());
    }
}

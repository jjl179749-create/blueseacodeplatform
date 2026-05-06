package org.blueseacode.dao.dto;

import lombok.Data;

/**
 * 趋势数据
 */
@Data
public class TrendVO {
    private String date;   // yyyy-MM-dd
    private long count;
}

package org.blueseacode.dao.dto;

import lombok.Data;

/**
 * 仪表盘统计数据
 */
@Data
public class DashboardVO {
    private long totalUsers;
    private long todayNewUsers;
    private long totalResources;
    private long totalArticles;
    private long totalDemands;
    private long pendingAudit;
    private long todayResourceDownloads;
    private long totalViews;
}

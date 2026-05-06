package org.blueseacode.service.points;

/**
 * 积分服务接口
 */
public interface PointsService {

    /**
     * 增加积分
     *
     * @param userId 用户ID
     * @param points 积分数量
     * @param reason 变动原因
     */
    void add(Long userId, int points, String reason);

    /**
     * 扣除积分
     *
     * @param userId 用户ID
     * @param points 积分数量
     * @param reason 变动原因
     */
    void deduct(Long userId, int points, String reason);

    /**
     * 查询用户积分
     *
     * @param userId 用户ID
     * @return 当前积分
     */
    int getPoints(Long userId);
}

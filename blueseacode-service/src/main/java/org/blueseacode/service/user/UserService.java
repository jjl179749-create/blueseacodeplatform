package org.blueseacode.service.user;

import org.blueseacode.service.user.model.PasswordUpdateRequest;
import org.blueseacode.service.user.model.UserProfileUpdateRequest;
import org.blueseacode.service.user.model.UserProfileVO;

public interface UserService {

    /** 获取当前用户个人信息 */
    UserProfileVO getProfile(Long userId);

    /** 获取他人主页信息 */
    UserProfileVO getUserProfile(Long userId);

    /** 更新个人信息 */
    void updateProfile(Long userId, UserProfileUpdateRequest request);

    /** 修改密码 */
    void updatePassword(Long userId, PasswordUpdateRequest request);

    /** 上传头像 */
    String updateAvatar(Long userId, String avatarUrl);
}

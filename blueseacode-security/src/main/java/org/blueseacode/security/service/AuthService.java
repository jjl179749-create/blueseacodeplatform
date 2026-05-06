package org.blueseacode.security.service;

import org.blueseacode.security.model.LoginRequest;
import org.blueseacode.security.model.RegisterRequest;
import org.blueseacode.security.model.TokenResponse;

public interface AuthService {

    /** 用户注册 */
    void register(RegisterRequest request);

    /** 用户登录 */
    TokenResponse login(LoginRequest request, String ip);

    /** 刷新Token */
    TokenResponse refreshToken(String refreshToken);

    /** 登出（将Token加入黑名单） */
    void logout(String accessToken, String refreshToken);
}

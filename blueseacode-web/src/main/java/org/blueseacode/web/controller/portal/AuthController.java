package org.blueseacode.web.controller.portal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.Result;
import org.blueseacode.security.model.LoginRequest;
import org.blueseacode.security.model.RegisterRequest;
import org.blueseacode.security.model.TokenResponse;
import org.blueseacode.security.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.PORTAL_PREFIX + "/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.<Void>success("注册成功");
    }

    @PostMapping("/login")
    public Result<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest servletRequest) {
        String ip = servletRequest.getRemoteAddr();
        TokenResponse token = authService.login(request, ip);
        return Result.success("登录成功", token);
    }

    @PostMapping("/refresh")
    public Result<TokenResponse> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        TokenResponse token = authService.refreshToken(refreshToken);
        return Result.success(token);
    }

    @PostMapping("/logout")
    public Result<Void> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {
        String accessToken = authHeader.replace(AppConstant.TOKEN_PREFIX, "");
        authService.logout(accessToken, refreshToken);
        return Result.<Void>success("已登出");
    }
}

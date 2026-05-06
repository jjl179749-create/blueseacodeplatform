package org.blueseacode.security.config;

import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    // ===== 白名单：无需认证即可访问 =====
    private static final String[] WHITE_LIST = {
            // Swagger
            "/swagger-ui.html", "/swagger-ui/**", "/api-docs/**",
            // 认证接口
            AppConstant.PORTAL_PREFIX + "/auth/**",
            // Actuator
            "/actuator/**",
            // WebSocket 端点（认证在 STOMP CONNECT 帧中处理）
            "/ws/**",
            // 静态资源
            "/error",
            // 测试
            "/api/test/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. 禁用 CSRF（前后端分离）
            .csrf(AbstractHttpConfigurer::disable)

            // 2. 无状态会话（使用JWT）
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. 请求权限配置
            .authorizeHttpRequests(auth -> auth
                // 白名单放行
                .requestMatchers(WHITE_LIST).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 管理端接口需要认证（由 @PreAuthorize 做细粒度角色控制）
                .requestMatchers(AppConstant.ADMIN_PREFIX + "/**").authenticated()
                // 用户端接口需要登录
                .requestMatchers(AppConstant.PORTAL_PREFIX + "/**").authenticated()
                // 其余全部放行
                .anyRequest().permitAll()
            )

            // 4. JWT过滤器（放在 UsernamePasswordAuthenticationFilter 之前）
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}

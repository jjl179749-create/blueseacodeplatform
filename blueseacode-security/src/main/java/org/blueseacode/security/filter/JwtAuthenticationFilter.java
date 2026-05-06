package org.blueseacode.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.security.jwt.JwtTokenProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            // 1. 检查黑名单
            String blacklistKey = AppConstant.CACHE_TOKEN_BLACKLIST + token;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                log.warn("Token已被加入黑名单");
                filterChain.doFilter(request, response);
                return;
            }

            // 2. 校验Token
            if (jwtTokenProvider.validateToken(token) && !jwtTokenProvider.isRefreshToken(token)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                List<String> roles = jwtTokenProvider.getRolesFromToken(token);

                // 3. 构建认证信息
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(request);

                // 4. 设置到 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 5. 放入Request属性（方便Controller获取当前用户）
                request.setAttribute(AppConstant.CURRENT_USER_ID, userId);
                request.setAttribute(AppConstant.CURRENT_USER_ROLES, roles);
            }
        }

        filterChain.doFilter(request, response);
    }

    /** 从请求头中解析 Bearer Token */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AppConstant.AUTH_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AppConstant.TOKEN_PREFIX)) {
            return bearerToken.substring(AppConstant.TOKEN_PREFIX.length());
        }
        return null;
    }
}

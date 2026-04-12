package com.example.demo.security.jwt;

import com.example.demo.auth.service.AuthService;
import com.example.demo.security.user.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j /// tự tạo biến log dùng để ghi log.info, log.warn...
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    /// quyết định req nào không cần chạy JWT filter, req nào phải chạy
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getServletPath();

        /// filter sẽ không chạy tới accs url này
        return uri.startsWith("/api/jpa/auth/login")
                || uri.startsWith("/api/jpa/auth/register")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui")
                || uri.equals("/swagger-ui.html");
    }

    /// mỗi req đi qua filter sẽ chạy vào
    /// lấy token
    /// kiểm tra -> hợp lệ thì cho vào security context
    /// cho req đi tiếp
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // lấy jwt từ req
            String jwt = resolveToken(request);

            //nêú req ko có token -> đi tiếp không xác thực ở đây
            if (!StringUtils.hasText(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            /// kiểm tra token có bị blacklist không
            /// nếu nằm trong black lít thì coi như ko dùng được nữa -> trả về 401
            if (tokenBlacklistService.isBlacklisted(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("""
                    {
                      "code": "AU_401",
                      "message": "Token đã bị logout",
                      "success": false
                    }
                    """);
                return;
            }


            /// kiểm tra token hợp lệ hay không
            if (!jwtTokenProvider.validateToken(jwt)) {
                log.warn("Invalid JWT token for uri={}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            /// lấy userId từ token
            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);

            /// set điều kiện phải lấy được userId từ token và context chưa có ai đăng nhập
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                /// gọi service để lấy thông tin user theo userId
                var userDetails = customUserDetailsService.loadUserById(userId);

                /// tạo object đại diện cho trạng thái user đã đăng nhập
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, /// thông tin user
                                null, /// credentials ko cần password nữa vì đã xác thực qua token
                                userDetails.getAuthorities() /// danh sách quyền user
                        );

                authentication.setDetails(
                        /// gắn thêm thông tin chi tiết từu req vào authentication
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                /// báo cho spring security biết req đã xacs thực user là ai có quyền gì
                SecurityContextHolder.getContext().setAuthentication(authentication);

                /// ghi log
                log.info("Authenticated userId={}, authorities={}",
                        userId,
                        userDetails.getAuthorities());
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            log.error("JWT filter error at uri={}: {}", request.getRequestURI(), ex.getMessage(), ex);
        }
        //cho req đi tiếp sau khi xử lý xong
        filterChain.doFilter(request, response);
    }


    /// tìm token trong req
    private String resolveToken(HttpServletRequest request) {
        /// đọc token từ header
        String bearerToken = request.getHeader("Authorization");
        /// kiểm tra header có token không có chuẩn không sau đó cắt bỏ để lấy token chuẩn
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        //nếu request không gửi cookie nào thì trả null
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> AuthService.ACCESS_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
package com.example.demo.auth.service;

import com.example.demo.auth.dto.req.LoginRequest;
import com.example.demo.auth.dto.res.LoginResponse;
import com.example.demo.auth.dto.res.MeResponse;
import com.example.demo.security.jwt.JwtTokenProvider;
import com.example.demo.security.jwt.TokenBlacklistService;
import com.example.demo.security.user.CustomUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String ACCESS_COOKIE = "access_token";
    public static final String REFRESH_COOKIE = "refresh_token";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    public LoginResponse login(LoginRequest request, HttpServletResponse response){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        addTokenCookie(response, ACCESS_COOKIE, accessToken, jwtTokenProvider.getAccessExpirationInSeconds());
        addTokenCookie(response, REFRESH_COOKIE, refreshToken, jwtTokenProvider.getRefreshExpirationInSeconds());

        List<String> authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .distinct()
                .toList();

        List<String> roles = authorities.stream()
                .filter(a -> a.startsWith("ROLE_"))
                .toList();

        return new LoginResponse(
                accessToken,
                "Cookie",
                jwtTokenProvider.getAccessExpirationInSeconds(),
                //jwtTokenProvider.getRefreshExpirationInSeconds(),
                new LoginResponse.UserInfo(
                        userDetails.getId(),
                        userDetails.getEmail(),
                        userDetails.getName(),
                        roles,
                        authorities
                )
        );
    }

    public MeResponse me(CustomUserDetails userDetails){
        List<String> authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .distinct()
                .toList();

        List<String> roles = authorities.stream()
                .filter(a -> a.startsWith("ROLE_"))
                .toList();

        return new MeResponse(
                userDetails.getId(),
                userDetails.getEmail(),
                userDetails.getName(),
                roles,
                authorities
        );
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {

       String accessToken = resolveAccessToken(request);
       String refreshToken = getCookieValue(request, REFRESH_COOKIE);

       if(StringUtils.hasText(accessToken)){
           tokenBlacklistService.blacklist(accessToken);
       }

       if(StringUtils.hasText(refreshToken)){
           tokenBlacklistService.blacklist(refreshToken);
       }

       clearCookie(response, ACCESS_COOKIE);
       clearCookie(response, REFRESH_COOKIE);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return getCookieValue(request, ACCESS_COOKIE);
    }

   private void addTokenCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds){
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAgeSeconds)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
   }

   private void clearCookie(HttpServletResponse response, String name){
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
   }

   private String getCookieValue(HttpServletRequest request, String cookieName){
       Cookie[] cookies = request.getCookies();

       if(cookies == null){
           return null;
       }

       return Arrays.stream(cookies)
               .filter(cookie -> cookieName.equals(cookie.getName()))
               .map(Cookie::getValue)
               .findFirst()
               .orElse(null);
   }
}



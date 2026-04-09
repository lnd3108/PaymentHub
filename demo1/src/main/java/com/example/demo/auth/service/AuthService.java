package com.example.demo.auth.service;

import com.example.demo.auth.dto.req.LoginRequest;
import com.example.demo.auth.dto.res.LoginResponse;
import com.example.demo.auth.dto.res.MeResponse;
import com.example.demo.security.jwt.JwtTokenProvider;
import com.example.demo.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateToken(authentication);

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
                "Bearer",
                jwtTokenProvider.getExpirationInSeconds(),
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
}

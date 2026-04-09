package com.example.demo.auth.controller;

import com.example.demo.auth.dto.req.LoginRequest;
import com.example.demo.auth.dto.res.LoginResponse;
import com.example.demo.auth.dto.res.MeResponse;
import com.example.demo.auth.service.AuthService;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jpa/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req){
        return ApiResponse.success(authService.login(req));
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(@AuthenticationPrincipal CustomUserDetails userDetails){
        return ApiResponse.success(authService.me(userDetails));
    }
}

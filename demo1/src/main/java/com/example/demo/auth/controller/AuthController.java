package com.example.demo.auth.controller;

import com.example.demo.auth.dto.req.LoginRequest;
import com.example.demo.auth.dto.req.RegisterRequest;
import com.example.demo.auth.dto.res.LoginResponse;
import com.example.demo.auth.dto.res.MeResponse;
import com.example.demo.auth.dto.res.RegisterResponse;
import com.example.demo.auth.service.AuthService;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.security.user.CustomUserDetails;
import com.example.demo.user.service.AcountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jpa/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AcountService acountService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request){
        return acountService.register(request);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req){
        return ApiResponse.success(authService.login(req));
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request) {
        authService.logout(request);
        return ApiResponse.success("Logout success", "Đăng xuất thành công");
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(@AuthenticationPrincipal CustomUserDetails userDetails){
        return ApiResponse.success(authService.me(userDetails));
    }
}

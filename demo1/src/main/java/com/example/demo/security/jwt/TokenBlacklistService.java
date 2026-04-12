package com.example.demo.security.jwt;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/// class service xủ lý blacklist token
@Service
public class TokenBlacklistService {

    /// private chỉ dùng trong class này
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    /// thêm 1 token vào blacklist
    public void blacklist(String token) {
        blacklistedTokens.add(token);
    }

    /// hàm kiểm tra token có nằm trong blacklist không
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
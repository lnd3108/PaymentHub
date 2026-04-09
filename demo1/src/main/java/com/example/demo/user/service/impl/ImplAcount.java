package com.example.demo.user.service.impl;

import com.example.demo.user.entity.Acount;
import com.example.demo.user.entity.Role;
import com.example.demo.user.repository.AcountRepository;
import com.example.demo.user.service.AcountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImplAcount implements AcountService {

    private final AcountRepository acountRepository;

    @Override
    public Acount saveAcount(Acount acount) {
        return null;
    }

    @Override
    public Role saveRole(Role role) {
        return null;
    }
}

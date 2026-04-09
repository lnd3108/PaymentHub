package com.example.demo.user.service;

import com.example.demo.user.entity.Acount;
import com.example.demo.user.entity.Role;

public interface AcountService {
    Acount saveAcount(Acount acount);
    Role saveRole(Role role);

}

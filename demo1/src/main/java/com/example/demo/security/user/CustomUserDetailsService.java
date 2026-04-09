package com.example.demo.security.user;

import com.example.demo.user.entity.Acount;
import com.example.demo.user.repository.AcountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AcountRepository acountRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException{
        Acount acount = acountRepository.findByEmailWithRolesAndPermissions(email)
                .orElseThrow(() ->
                    new UsernameNotFoundException("Email hoặc mật khẩu không đúng")
                );
        return new CustomUserDetails(acount);
    }

    public CustomUserDetails loadUserById(Long id){
        Acount acount = acountRepository.findByIdWithRolesAndPermissions(id)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));
        return new CustomUserDetails(acount);
    }

}

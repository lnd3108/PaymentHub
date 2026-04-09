package com.example.demo.security.user;

import com.example.demo.user.entity.Acount;
import com.example.demo.user.entity.AcountRole;
import com.example.demo.user.entity.RolePermission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Acount acount){
        this.id = acount.getId();
        this.email = acount.getEmail();
        this.password = acount.getPassword();
        this.name = acount.getName();
        this.authorities = buildAuthorities(acount);
    }

    private Collection<? extends GrantedAuthority> buildAuthorities(Acount acount){
        Set<GrantedAuthority> result = new LinkedHashSet<>();

        if(acount.getAcountRoles() == null){
            return result;
        }

        for(AcountRole acountRole : acount.getAcountRoles()){
            if(acountRole.getRole() == null){
                continue;
            }

            String roleName = acountRole.getRole().getName();
            if(roleName != null && !roleName.isBlank()){
                result.add(new SimpleGrantedAuthority(roleName));
            }

            if(acountRole.getRole().getRolePermissions() != null){
                for (RolePermission rp : acountRole.getRole().getRolePermissions()){
                    if(rp.getPermission().getName() != null
                        && rp.getPermission().getName().isBlank()
                        && !rp.getPermission().getName().isBlank()){
                        result.add(new SimpleGrantedAuthority(rp.getPermission().getName()));
                    }
                }
            }
        }
        return result;
    }


    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

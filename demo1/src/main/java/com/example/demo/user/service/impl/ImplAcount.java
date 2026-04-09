package com.example.demo.user.service.impl;

import com.example.demo.auth.dto.req.RegisterRequest;
import com.example.demo.auth.dto.res.RegisterResponse;
import com.example.demo.user.entity.Acount;
import com.example.demo.user.entity.AcountRole;
import com.example.demo.user.entity.AcountRoleId;
import com.example.demo.user.entity.Role;
import com.example.demo.user.repository.AcountRepository;
import com.example.demo.user.repository.AcountRoleRepository;
import com.example.demo.user.repository.RoleRepository;
import com.example.demo.user.service.AcountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ImplAcount implements AcountService {

    private final AcountRepository acountRepository;
    private final RoleRepository roleRepository;
    private final AcountRoleRepository acountRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public Acount saveAcount(Acount acount) {
        if (acountRepository.existsByEmail(acount.getEmail())) {
            throw new RuntimeException("Email đã tồn tại: " + acount.getEmail());
        }

        acount.setPassword(passwordEncoder.encode(acount.getPassword()));
        return acountRepository.save(acount);
    }

    @Override
    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public Role saveRoleByName(String roleName, String description) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    role.setDescription(description);
                    return roleRepository.save(role);
                });
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (acountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        String roleName = (request.getRoleName() == null || request.getRoleName().isBlank())
                ? "ROLE_USER"
                : request.getRoleName().trim().toUpperCase();

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role: " + roleName));

        Acount acount = new Acount();
        acount.setEmail(request.getEmail().trim());
        acount.setName(request.getName().trim());
        acount.setPassword(passwordEncoder.encode(request.getPassword()));

        Acount savedAcount = acountRepository.save(acount);

        AcountRole acountRole = new AcountRole();
        acountRole.setId(new AcountRoleId(savedAcount.getId(), role.getId()));
        acountRole.setAcount(savedAcount);
        acountRole.setRole(role);

        acountRoleRepository.save(acountRole);

        Acount result = acountRepository.findByIdWithRolesAndPermissions(savedAcount.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy account sau khi tạo"));

        Set<String> roles = result.getAcountRoles().stream()
                .map(ar -> ar.getRole().getName())
                .collect(Collectors.toSet());

        return RegisterResponse.builder()
                .id(result.getId())
                .email(result.getEmail())
                .name(result.getName())
                .roles(roles)
                .build();
    }

    @Override
    public void addRoleToAcount(String email, String roleName) {
        Acount acount = acountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy account: " + email));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role: " + roleName));

        AcountRoleId id = new AcountRoleId(acount.getId(), role.getId());

        boolean alreadyAssigned = acount.getAcountRoles().stream()
                .anyMatch(ar -> ar.getId().equals(id));

        if (alreadyAssigned) {
            return;
        }

        AcountRole acountRole = new AcountRole();
        acountRole.setId(id);
        acountRole.setAcount(acount);
        acountRole.setRole(role);

        acountRoleRepository.save(acountRole);
    }

    @Override
    public void createDefaultAccount(String email, String name, String rawPassword, String roleName) {
        if (acountRepository.existsByEmail(email)) {
            return;
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role chưa tồn tại: " + roleName));

        Acount acount = new Acount();
        acount.setEmail(email);
        acount.setName(name);
        acount.setPassword(passwordEncoder.encode(rawPassword));

        Acount savedAcount = acountRepository.save(acount);

        AcountRole acountRole = new AcountRole();
        acountRole.setId(new AcountRoleId(savedAcount.getId(), role.getId()));
        acountRole.setAcount(savedAcount);
        acountRole.setRole(role);

        acountRoleRepository.save(acountRole);
    }
}
package com.book.swap.seeder;

import com.book.swap.models.entities.DbPermission;
import com.book.swap.models.entities.DbRole;
import com.book.swap.models.entities.DbUsers;
import com.book.swap.repository.PermissionRepository;
import com.book.swap.repository.RoleRepository;
import com.book.swap.repository.UserRepository;
import com.book.swap.utils.books.Constants;
import com.book.swap.utils.books.Logger;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class Seeder {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @PostConstruct
    public void init() {
    }

//    @PostConstruct
    public void seedRoles() {
        List<String> roles = Arrays.asList("USER", "ADMIN", "MODERATOR");

        for (String roleName : roles) {
            if (!roleRepository.existsByName(roleName)) {
                DbRole role = new DbRole();
                role.setName(roleName);
                roleRepository.save(role);
                Logger.info("✅ Role added: " + roleName);
            } else {
                Logger.warn("ℹ️ Role already exists: " + roleName);
            }
        }
    }

//    @PostConstruct
    public void seedPermissions() {
        List<String> permissions = Arrays.asList("CREATE_USER", "CREATE_ADMIN", "UPDATE_USER", "UPDATE_ADMIN", "DELETE_USER", "DELETE_ROLE");
        for (String permissionName : permissions) {
            if (permissionRepository.existsByName(permissionName)) {
                DbPermission permission = new DbPermission();
                permission.setName(permissionName);
                permission.setDescription(permissionName);
                permissionRepository.save(permission);
                Logger.info("✅ Permission added: " + permissionName);
            } else {
                Logger.warn("ℹ️ Permission already exists: " + permissionName);
            }
        }
    }

    @PostConstruct
    public void seedAdmin() {
        try {
            DbUsers dbUsers = DbUsers
                    .builder()
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("admin"))
                    .phoneNumber("9876543210")
                    .fullName("Admin")
                    .username("admin123")
                    .roles(Set.of(Constants.ROLE.ADMIN))
                    .build();
            if (!userRepository.existsByEmail(dbUsers.getEmail()) || !userRepository.existsByUsername(dbUsers.getUsername())) {
                DbUsers admin = userRepository.save(dbUsers);
                Logger.info("Admin created successfully: " + dbUsers.getUsername()+", ID: "+admin.getId());
            }
        } catch (Exception e) {
            Logger.error("Error while saving admin roles: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}

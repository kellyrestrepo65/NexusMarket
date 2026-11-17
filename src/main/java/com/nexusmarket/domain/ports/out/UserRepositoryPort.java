package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(EntityId id);

    List<User> findAll();

    boolean existsByEmail(String email);

    boolean existsByIdentityDocument(String identityDocument);

    boolean existsByRole(Role role);
}

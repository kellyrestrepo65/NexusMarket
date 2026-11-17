package com.nexusmarket.application.adapters.out.persistence.mysql.repositories;

import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByIdentityDocument(String identityDocument);

    Optional<UserEntity> findFirstByIdentityDocument(String identityDocument);

    boolean existsByRole(Role role);
}

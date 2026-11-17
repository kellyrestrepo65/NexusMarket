package com.nexusmarket.application.adapters.out.persistence.mysql.repositories;

import com.nexusmarket.application.adapters.out.persistence.mysql.entities.ShoppingCartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShoppingCartJpaRepository extends JpaRepository<ShoppingCartEntity, Integer> {

    Optional<ShoppingCartEntity> findByBuyerId(Integer buyerId);
}

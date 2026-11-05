package com.nexusmarket.application.adapters.out.persistence.mysql.repositories;

import com.nexusmarket.application.adapters.out.persistence.mysql.entities.ReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReturnJpaRepository extends JpaRepository<ReturnEntity, Integer> {

    Optional<ReturnEntity> findByOrderId(Integer orderId);
}

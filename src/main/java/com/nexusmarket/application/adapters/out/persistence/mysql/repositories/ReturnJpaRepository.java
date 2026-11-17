package com.nexusmarket.application.adapters.out.persistence.mysql.repositories;

import com.nexusmarket.application.adapters.out.persistence.mysql.entities.ReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReturnJpaRepository extends JpaRepository<ReturnEntity, Integer> {

    List<ReturnEntity> findByOrderId(Integer orderId);
}

package com.nexusmarket.application.adapters.out.persistence.mysql.repositories;

import com.nexusmarket.application.adapters.out.persistence.mysql.entities.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseJpaRepository extends JpaRepository<WarehouseEntity, Integer> {

    List<WarehouseEntity> findByOwnerIdOrderByIdAsc(Integer ownerId);
}

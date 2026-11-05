package com.nexusmarket.application.adapters.out.persistence.mysql.repositories;

import com.nexusmarket.application.adapters.out.persistence.mysql.entities.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, Integer> {

    Optional<InvoiceEntity> findByOrderId(Integer orderId);
}

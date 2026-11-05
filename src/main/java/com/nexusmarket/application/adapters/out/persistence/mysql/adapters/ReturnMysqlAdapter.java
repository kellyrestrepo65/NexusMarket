package com.nexusmarket.application.adapters.out.persistence.mysql.adapters;

import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.ReturnEntity;
import com.nexusmarket.application.adapters.out.persistence.mysql.repositories.ReturnJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ReturnMysqlAdapter implements ReturnRepositoryPort {

    private final ReturnJpaRepository returnJpaRepository;

    public ReturnMysqlAdapter(ReturnJpaRepository returnJpaRepository) {
        this.returnJpaRepository = returnJpaRepository;
    }

    @Override
    public Return save(Return orderReturn) {
        returnJpaRepository.save(ReturnEntity.from(orderReturn));
        return orderReturn;
    }

    @Override
    public Optional<Return> findById(EntityId id) {
        return returnJpaRepository.findById(id.value()).map(ReturnEntity::toDomain);
    }

    @Override
    public Optional<Return> findByOrderId(EntityId orderId) {
        return returnJpaRepository.findByOrderId(orderId.value()).map(ReturnEntity::toDomain);
    }

    @Override
    public List<Return> findAll() {
        return returnJpaRepository.findAll().stream().map(ReturnEntity::toDomain).toList();
    }
}

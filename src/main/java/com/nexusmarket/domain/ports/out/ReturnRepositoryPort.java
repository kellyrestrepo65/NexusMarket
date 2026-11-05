package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.valueobjects.EntityId;

import java.util.List;
import java.util.Optional;

public interface ReturnRepositoryPort {

    Return save(Return orderReturn);

    Optional<Return> findById(EntityId id);

    Optional<Return> findByOrderId(EntityId orderId);

    List<Return> findAll();
}

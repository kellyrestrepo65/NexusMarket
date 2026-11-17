package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.order.Invoice;
import com.nexusmarket.domain.valueobjects.EntityId;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepositoryPort {

    Invoice save(Invoice invoice);

    Optional<Invoice> findByOrderId(EntityId orderId);

    List<Invoice> findAll();
}

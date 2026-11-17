package com.nexusmarket.application.adapters.out.persistence.mysql.adapters;

import com.nexusmarket.domain.models.order.Invoice;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.InvoiceEntity;
import com.nexusmarket.application.adapters.out.persistence.mysql.repositories.InvoiceJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class InvoiceMysqlAdapter implements InvoiceRepositoryPort {

    private final InvoiceJpaRepository invoiceJpaRepository;

    public InvoiceMysqlAdapter(InvoiceJpaRepository invoiceJpaRepository) {
        this.invoiceJpaRepository = invoiceJpaRepository;
    }

    @Override
    public Invoice save(Invoice invoice) {
        invoiceJpaRepository.save(InvoiceEntity.from(invoice));
        return invoice;
    }

    @Override
    public Optional<Invoice> findByOrderId(EntityId orderId) {
        return invoiceJpaRepository.findByOrderId(orderId.value()).map(InvoiceEntity::toDomain);
    }

    @Override
    public List<Invoice> findAll() {
        return invoiceJpaRepository.findAll().stream().map(InvoiceEntity::toDomain).toList();
    }
}

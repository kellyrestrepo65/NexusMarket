package com.nexusmarket.application.adapters.out.persistence.mysql.entities;

import com.nexusmarket.domain.models.order.Invoice;
import com.nexusmarket.domain.valueobjects.EntityId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
public class InvoiceEntity {

    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer orderId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private LocalDateTime issueDate;

    protected InvoiceEntity() {
    }

    public static InvoiceEntity from(Invoice invoice) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.id = invoice.getId().value();
        entity.orderId = invoice.getOrderId().value();
        entity.total = invoice.getTotal();
        entity.issueDate = invoice.getIssueDate();
        return entity;
    }

    public Invoice toDomain() {
        return new Invoice(EntityId.of(id), EntityId.of(orderId), total, issueDate);
    }
}

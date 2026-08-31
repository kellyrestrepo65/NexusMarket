package com.nexusmarket.domain.model.order;

import com.nexusmarket.domain.exception.InvalidArgumentException;
import com.nexusmarket.domain.model.valueobject.EntityId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * La factura de una orden. Cada orden genera exactamente una factura
 * cuando se confirma el pago, con el total ya calculado y cerrado
 * (no se vuelve a recalcular despues).
 */
public class Invoice {

    private final EntityId id;
    private final EntityId orderId;
    private final BigDecimal total;
    private final LocalDateTime issueDate;

    public Invoice(EntityId id, EntityId orderId, BigDecimal total, LocalDateTime issueDate) {
        if (id == null) {
            throw new InvalidArgumentException("The invoice id is required");
        }
        if (orderId == null) {
            throw new InvalidArgumentException("The invoice must be associated with an order");
        }
        if (total == null || total.signum() < 0) {
            throw new InvalidArgumentException("The invoice total must be greater than or equal to zero");
        }
        if (issueDate == null) {
            throw new InvalidArgumentException("The issue date is required");
        }
        this.id = id;
        this.orderId = orderId;
        this.total = total;
        this.issueDate = issueDate;
    }

    public EntityId getId() {
        return id;
    }

    public EntityId getOrderId() {
        return orderId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invoice)) return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(id, invoice.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

package com.nexusmarket.domain.models.order;

import com.nexusmarket.domain.exceptions.InvalidArgumentException;
import com.nexusmarket.domain.exceptions.InvalidStateTransitionException;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.RefundStatus;

import java.math.BigDecimal;
import java.util.Objects;

/** El reembolso que se genera cuando una devolucion se aprueba (ver Return.approve()). */
public class Refund {

    private final EntityId id;
    private final EntityId returnId;
    private final BigDecimal amount;
    private RefundStatus status;

    public Refund(EntityId id, EntityId returnId, BigDecimal amount) {
        if (id == null) {
            throw new InvalidArgumentException("The refund id is required");
        }
        if (returnId == null) {
            throw new InvalidArgumentException("The refund must originate from a return");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidArgumentException("The refund amount must be greater than zero");
        }
        this.id = id;
        this.returnId = returnId;
        this.amount = amount;
        this.status = RefundStatus.PENDING;
    }

    public void process() {
        if (this.status != RefundStatus.PENDING) {
            throw new InvalidStateTransitionException("Only a pending refund can be processed");
        }
        this.status = RefundStatus.PROCESSED;
    }

    public void reject() {
        if (this.status != RefundStatus.PENDING) {
            throw new InvalidStateTransitionException("Only a pending refund can be rejected");
        }
        this.status = RefundStatus.REJECTED;
    }

    /** Reconstruye un reembolso ya guardado con su estado. */
    public static Refund restore(EntityId id, EntityId returnId, BigDecimal amount, RefundStatus status) {
        if (status == null) {
            throw new InvalidArgumentException("The refund status is required");
        }
        Refund refund = new Refund(id, returnId, amount);
        refund.status = status;
        return refund;
    }

    public EntityId getId() {
        return id;
    }

    public EntityId getReturnId() {
        return returnId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public RefundStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Refund)) return false;
        Refund that = (Refund) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

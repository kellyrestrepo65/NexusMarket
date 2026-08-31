package com.nexusmarket.domain.model.order;

import com.nexusmarket.domain.exception.InvalidArgumentException;
import com.nexusmarket.domain.exception.InvalidStateTransitionException;
import com.nexusmarket.domain.model.valueobject.EntityId;
import com.nexusmarket.domain.model.valueobject.ReturnStatus;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Una devolucion solicitada por un comprador sobre una orden ya entregada.
 * Cuando se aprueba, se crea el Refund correspondiente aqui mismo, para que
 * nunca exista un reembolso sin una devolucion aprobada detras.
 */
public class Return {

    private final EntityId id;
    private final EntityId orderId;
    private final String reason;
    private ReturnStatus status;
    private Refund refund;

    public Return(EntityId id, EntityId orderId, String reason) {
        if (id == null) {
            throw new InvalidArgumentException("The return id is required");
        }
        if (orderId == null) {
            throw new InvalidArgumentException("The return must be associated with an order");
        }
        if (reason == null || reason.isBlank()) {
            throw new InvalidArgumentException("The return reason cannot be empty");
        }
        this.id = id;
        this.orderId = orderId;
        this.reason = reason;
        this.status = ReturnStatus.REQUESTED;
    }

    public Refund approve(EntityId refundId, BigDecimal refundAmount) {
        if (this.status != ReturnStatus.REQUESTED) {
            throw new InvalidStateTransitionException("Only a requested return can be approved");
        }
        this.status = ReturnStatus.APPROVED;
        this.refund = new Refund(refundId, this.id, refundAmount);
        return this.refund;
    }

    public void reject() {
        if (this.status != ReturnStatus.REQUESTED) {
            throw new InvalidStateTransitionException("Only a requested return can be rejected");
        }
        this.status = ReturnStatus.REJECTED;
    }

    public EntityId getId() {
        return id;
    }

    public EntityId getOrderId() {
        return orderId;
    }

    public String getReason() {
        return reason;
    }

    public ReturnStatus getStatus() {
        return status;
    }

    public Refund getRefund() {
        return refund;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Return)) return false;
        Return that = (Return) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

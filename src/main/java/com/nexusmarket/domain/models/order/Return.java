package com.nexusmarket.domain.models.order;

import com.nexusmarket.domain.exceptions.InvalidArgumentException;
import com.nexusmarket.domain.exceptions.InvalidStateTransitionException;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.ReturnStatus;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Una devolucion solicitada por un comprador sobre UN producto de una orden ya
 * entregada. Guarda el vendedor dueno del producto para poder atribuirle la
 * devolucion y el reembolso sin tocar los demas productos de la orden.
 * Cuando se aprueba, se crea el Refund correspondiente aqui mismo, para que
 * nunca exista un reembolso sin una devolucion aprobada detras.
 */
public class Return {

    private final EntityId id;
    private final EntityId orderId;
    private final EntityId productId;
    private final EntityId sellerId;
    private final int quantity;
    private final String reason;
    private ReturnStatus status;
    private Refund refund;

    public Return(EntityId id, EntityId orderId, String reason) {
        this(id, orderId, null, null, 0, reason);
    }

    public Return(EntityId id, EntityId orderId, EntityId productId, EntityId sellerId, int quantity,
                  String reason) {
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
        if (productId != null && quantity <= 0) {
            throw new InvalidArgumentException("The returned quantity must be greater than zero");
        }
        this.orderId = orderId;
        this.productId = productId;
        this.sellerId = sellerId;
        this.quantity = quantity;
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

    /**
     * Reconstruye una devolucion ya guardada. Solo una devolucion aprobada
     * puede tener reembolso, y una aprobada siempre lo tiene.
     */
    public static Return restore(EntityId id, EntityId orderId, String reason, ReturnStatus status,
                                 Refund refund) {
        return restore(id, orderId, null, null, 0, reason, status, refund);
    }

    public static Return restore(EntityId id, EntityId orderId, EntityId productId, EntityId sellerId,
                                 int quantity, String reason, ReturnStatus status, Refund refund) {
        if (status == null) {
            throw new InvalidArgumentException("The return status is required");
        }
        if ((status == ReturnStatus.APPROVED) != (refund != null)) {
            throw new InvalidArgumentException("Only an approved return has a refund");
        }
        Return stored = new Return(id, orderId, productId, sellerId, quantity, reason);
        stored.status = status;
        stored.refund = refund;
        return stored;
    }

    public EntityId getId() {
        return id;
    }

    public EntityId getOrderId() {
        return orderId;
    }

    /** Producto devuelto; null solo en devoluciones antiguas de orden completa. */
    public EntityId getProductId() {
        return productId;
    }

    public EntityId getSellerId() {
        return sellerId;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isForProduct(EntityId candidate) {
        return productId == null || productId.equals(candidate);
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

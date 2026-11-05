package com.nexusmarket.application.adapters.out.persistence.mysql.entities;

import com.nexusmarket.domain.models.order.Refund;
import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.RefundStatus;
import com.nexusmarket.domain.enums.ReturnStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Una devolucion junto con su reembolso, que solo existe cuando la devolucion se aprueba. */
@Entity
@Table(name = "returns")
public class ReturnEntity {

    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer orderId;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnStatus status;

    private Integer refundId;

    @Column(precision = 15, scale = 2)
    private BigDecimal refundAmount;

    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus;

    protected ReturnEntity() {
    }

    public static ReturnEntity from(Return orderReturn) {
        ReturnEntity entity = new ReturnEntity();
        entity.id = orderReturn.getId().value();
        entity.orderId = orderReturn.getOrderId().value();
        entity.reason = orderReturn.getReason();
        entity.status = orderReturn.getStatus();
        Refund refund = orderReturn.getRefund();
        if (refund != null) {
            entity.refundId = refund.getId().value();
            entity.refundAmount = refund.getAmount();
            entity.refundStatus = refund.getStatus();
        }
        return entity;
    }

    public Return toDomain() {
        EntityId returnId = EntityId.of(id);
        Refund refund = refundId == null ? null
                : Refund.restore(EntityId.of(refundId), returnId, refundAmount, refundStatus);
        return Return.restore(returnId, EntityId.of(orderId), reason, status, refund);
    }
}

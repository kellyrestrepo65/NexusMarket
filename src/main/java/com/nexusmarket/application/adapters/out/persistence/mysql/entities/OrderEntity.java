package com.nexusmarket.application.adapters.out.persistence.mysql.entities;

import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.order.OrderItem;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.OrderStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private Integer id;

    @Column(nullable = false)
    private Integer buyerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<Item> items = new ArrayList<>();

    @Embeddable
    public static class Item {
        private Integer productId;
        private int quantity;
        @Column(precision = 15, scale = 2)
        private BigDecimal unitPrice;

        protected Item() {
        }

        Item(Integer productId, int quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }

    protected OrderEntity() {
    }

    public static OrderEntity from(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.id = order.getId().value();
        entity.buyerId = order.getBuyerId().value();
        entity.status = order.getStatus();
        for (OrderItem item : order.getItems()) {
            entity.items.add(new Item(item.getProductId().value(), item.getQuantity(), item.getUnitPrice()));
        }
        return entity;
    }

    /**
     * Reconstruye la orden con sus propias transiciones (primero los items,
     * luego cada estado en orden) para que una orden guardada nunca se salte la
     * maquina de estados.
     */
    public Order toDomain() {
        Order order = new Order(EntityId.of(id), EntityId.of(buyerId));
        for (Item item : items) {
            order.addItem(new OrderItem(EntityId.of(item.productId), item.quantity, item.unitPrice));
        }
        for (OrderStatus step : OrderStatus.values()) {
            if (step.ordinal() > status.ordinal()) {
                break;
            }
            if (order.getStatus().canAdvanceTo(step)) {
                order.advanceTo(step);
            }
        }
        return order;
    }

    public Integer getId() {
        return id;
    }
}

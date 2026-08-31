package com.nexusmarket.domain.model.order;

import com.nexusmarket.domain.exception.InvalidArgumentException;
import com.nexusmarket.domain.exception.OrderFinalizedException;
import com.nexusmarket.domain.exception.InvalidStateTransitionException;
import com.nexusmarket.domain.model.valueobject.EntityId;
import com.nexusmarket.domain.model.valueobject.OrderStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * La orden que se genera cuando el comprador confirma su carrito. Agrupa
 * los OrderItem y guarda el estado actual (CART, PENDING_PAYMENT, PAID,
 * DISPATCHED, DELIVERED). advanceTo() es el unico metodo que cambia el
 * estado, y usa OrderStatus.canAdvanceTo() para no permitir saltos raros,
 * por ejemplo pasar de PAID a DELIVERED sin pasar por DISPATCHED.
 * Una orden entregada (DELIVERED) ya no se puede modificar.
 */
public class Order {

    private final EntityId id;
    private final EntityId buyerId;
    private final List<OrderItem> items;
    private OrderStatus status;

    public Order(EntityId id, EntityId buyerId) {
        if (id == null) {
            throw new InvalidArgumentException("The order id is required");
        }
        if (buyerId == null) {
            throw new InvalidArgumentException("The order must belong to a buyer");
        }
        this.id = id;
        this.buyerId = buyerId;
        this.items = new ArrayList<>();
        this.status = OrderStatus.CART;
    }

    public void addItem(OrderItem item) {
        validateNotFinalized();
        if (item == null) {
            throw new InvalidArgumentException("The item cannot be null");
        }
        this.items.add(item);
    }

    public void removeItem(EntityId productId) {
        validateNotFinalized();
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i).getProductId().equals(productId)) {
                this.items.remove(i);
                return;
            }
        }
    }

    /**
     * Unico metodo que cambia el estado de la orden. La decision de si el
     * cambio es valido la hace OrderStatus.canAdvanceTo(), no esta clase.
     */
    public void advanceTo(OrderStatus nextStatus) {
        if (nextStatus == null) {
            throw new InvalidArgumentException("The next status cannot be null");
        }
        if (!this.status.canAdvanceTo(nextStatus)) {
            throw new InvalidStateTransitionException(
                    "Invalid transition: cannot go from " + this.status + " to " + nextStatus);
        }
        this.status = nextStatus;
    }

    public boolean requiresShipment() {
        return !this.items.isEmpty();
    }

    public BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : this.items) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    public boolean isFinalized() {
        return this.status.isFinal();
    }

    private void validateNotFinalized() {
        if (this.status.isFinal()) {
            throw new OrderFinalizedException("A finalized order (DELIVERED) cannot be modified");
        }
    }

    public EntityId getId() {
        return id;
    }

    public EntityId getBuyerId() {
        return buyerId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

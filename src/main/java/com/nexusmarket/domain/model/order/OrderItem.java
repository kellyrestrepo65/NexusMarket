package com.nexusmarket.domain.model.order;

import com.nexusmarket.domain.exception.InvalidArgumentException;
import com.nexusmarket.domain.model.valueobject.EntityId;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Un producto dentro de una orden ya confirmada. A diferencia de
 * CartItem, aqui el precio unitario queda congelado al momento de la
 * compra, para que si el precio del producto cambia despues no afecte
 * ordenes que ya se hicieron.
 */
public class OrderItem {

    private final EntityId productId;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderItem(EntityId productId, int quantity, BigDecimal unitPrice) {
        if (productId == null) {
            throw new InvalidArgumentException("The item's product is required");
        }
        if (quantity <= 0) {
            throw new InvalidArgumentException("The quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new InvalidArgumentException("The unit price must be greater than or equal to zero");
        }
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public EntityId getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem that = (OrderItem) o;
        return Objects.equals(productId, that.productId)
                && Objects.equals(unitPrice, that.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, unitPrice);
    }
}

package com.nexusmarket.domain.model.cart;

import com.nexusmarket.domain.exception.InvalidArgumentException;
import com.nexusmarket.domain.model.valueobject.EntityId;

import java.util.Objects;

/**
 * Un producto dentro del carrito, con su cantidad. Siempre se crea,
 * modifica y elimina a traves de ShoppingCart, nunca solo, por eso el
 * constructor es package-private.
 */
public class CartItem {

    private final EntityId productId;
    private int quantity;

    CartItem(EntityId productId, int quantity) {
        if (productId == null) {
            throw new InvalidArgumentException("The item's product is required");
        }
        if (quantity <= 0) {
            throw new InvalidArgumentException("The quantity must be greater than zero");
        }
        this.productId = productId;
        this.quantity = quantity;
    }

    void increment(int units) {
        this.quantity += units;
    }

    void changeQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new InvalidArgumentException("The quantity must be greater than zero");
        }
        this.quantity = newQuantity;
    }

    public EntityId getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartItem)) return false;
        CartItem that = (CartItem) o;
        return Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}

package com.nexusmarket.domain.model.cart;

import com.nexusmarket.domain.exception.InvalidArgumentException;
import com.nexusmarket.domain.model.valueobject.EntityId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * El carrito de compras de un comprador, antes de convertirse en una orden.
 * Todos los cambios a los items pasan por aqui, nunca se modifica un
 * CartItem directamente desde afuera.
 */
public class ShoppingCart {

    private final EntityId id;
    private final EntityId buyerId;
    private final List<CartItem> items;

    public ShoppingCart(EntityId id, EntityId buyerId) {
        if (id == null) {
            throw new InvalidArgumentException("The cart id is required");
        }
        if (buyerId == null) {
            throw new InvalidArgumentException("The cart must belong to a buyer");
        }
        this.id = id;
        this.buyerId = buyerId;
        this.items = new ArrayList<>();
    }

    public void addProduct(EntityId productId, int quantity) {
        CartItem existing = findItem(productId);
        if (existing != null) {
            existing.increment(quantity);
        } else {
            this.items.add(new CartItem(productId, quantity));
        }
    }

    public void changeQuantity(EntityId productId, int newQuantity) {
        CartItem item = findItem(productId);
        if (item == null) {
            throw new InvalidArgumentException("The product is not in the cart");
        }
        item.changeQuantity(newQuantity);
    }

    public void removeProduct(EntityId productId) {
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i).getProductId().equals(productId)) {
                this.items.remove(i);
                return;
            }
        }
    }

    public void clear() {
        this.items.clear();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    private CartItem findItem(EntityId productId) {
        for (CartItem item : this.items) {
            if (item.getProductId().equals(productId)) {
                return item;
            }
        }
        return null;
    }

    public EntityId getId() {
        return id;
    }

    public EntityId getBuyerId() {
        return buyerId;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShoppingCart)) return false;
        ShoppingCart that = (ShoppingCart) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

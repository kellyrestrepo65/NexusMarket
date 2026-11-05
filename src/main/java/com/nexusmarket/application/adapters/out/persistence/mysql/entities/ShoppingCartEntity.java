package com.nexusmarket.application.adapters.out.persistence.mysql.entities;

import com.nexusmarket.domain.models.cart.CartItem;
import com.nexusmarket.domain.models.cart.ShoppingCart;
import com.nexusmarket.domain.valueobjects.EntityId;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shopping_carts")
public class ShoppingCartEntity {

    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer buyerId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
    private List<Item> items = new ArrayList<>();

    @Embeddable
    public static class Item {
        private Integer productId;
        private int quantity;

        protected Item() {
        }

        Item(Integer productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }

    protected ShoppingCartEntity() {
    }

    public static ShoppingCartEntity from(ShoppingCart cart) {
        ShoppingCartEntity entity = new ShoppingCartEntity();
        entity.id = cart.getId().value();
        entity.buyerId = cart.getBuyerId().value();
        for (CartItem item : cart.getItems()) {
            entity.items.add(new Item(item.getProductId().value(), item.getQuantity()));
        }
        return entity;
    }

    public ShoppingCart toDomain() {
        ShoppingCart cart = new ShoppingCart(EntityId.of(id), EntityId.of(buyerId));
        for (Item item : items) {
            cart.addProduct(EntityId.of(item.productId), item.quantity);
        }
        return cart;
    }

    public Integer getId() {
        return id;
    }
}

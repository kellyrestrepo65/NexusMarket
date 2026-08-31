package com.nexusmarket.domain.model.catalog;

import com.nexusmarket.domain.exception.InvalidArgumentException;
import com.nexusmarket.domain.exception.InvalidStateTransitionException;
import com.nexusmarket.domain.model.valueobject.EntityId;
import com.nexusmarket.domain.model.valueobject.ProductStatus;
import com.nexusmarket.domain.model.valueobject.ProductType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Un producto publicado por un vendedor. Se usa un solo Product con el
 * campo type (PHYSICAL o DIGITAL) en vez de crear dos clases distintas,
 * porque el nombre, precio, variantes y estado son iguales en los dos
 * casos; lo unico que cambia es si necesita envio o no.
 */
public class Product {

    private final EntityId id;
    private final EntityId sellerId;
    private String name;
    private BigDecimal price;
    private final ProductType type;
    private ProductStatus status;
    private final List<Variant> variants;

    public Product(EntityId id, EntityId sellerId, String name, BigDecimal price, ProductType type) {
        if (id == null) {
            throw new InvalidArgumentException("The product id is required");
        }
        if (sellerId == null) {
            throw new InvalidArgumentException("A product must always belong to a seller");
        }
        if (type == null) {
            throw new InvalidArgumentException("The product type is required (PHYSICAL or DIGITAL)");
        }
        this.id = id;
        this.sellerId = sellerId;
        this.type = type;
        this.variants = new ArrayList<>();
        this.status = ProductStatus.PUBLISHED;
        changeName(name);
        changePrice(price);
    }

    public void changeName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new InvalidArgumentException("The product name cannot be empty");
        }
        this.name = newName;
    }

    public void changePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.signum() < 0) {
            throw new InvalidArgumentException("The product price must be greater than or equal to zero");
        }
        this.price = newPrice;
    }

    public void addVariant(Variant variant) {
        if (variant == null) {
            throw new InvalidArgumentException("The variant cannot be null");
        }
        this.variants.add(variant);
    }

    public void suspend() {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new InvalidStateTransitionException("A discontinued product cannot be suspended");
        }
        this.status = ProductStatus.SUSPENDED;
    }

    public void publish() {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new InvalidStateTransitionException("A discontinued product cannot be published again");
        }
        this.status = ProductStatus.PUBLISHED;
    }

    public void discontinue() {
        this.status = ProductStatus.DISCONTINUED;
    }

    public boolean isAvailableForPurchase() {
        return this.status == ProductStatus.PUBLISHED;
    }

    /** true si el producto necesita reservar inventario y enviarse fisicamente. */
    public boolean requiresPhysicalShipping() {
        return this.type == ProductType.PHYSICAL;
    }

    public EntityId getId() {
        return id;
    }

    public EntityId getSellerId() {
        return sellerId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public ProductType getType() {
        return type;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public List<Variant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

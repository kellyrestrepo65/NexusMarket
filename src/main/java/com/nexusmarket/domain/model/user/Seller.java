package com.nexusmarket.domain.model.user;

import com.nexusmarket.domain.exception.InvalidArgumentException;
import com.nexusmarket.domain.model.catalog.Product;
import com.nexusmarket.domain.model.inventory.Warehouse;
import com.nexusmarket.domain.model.valueobject.EntityId;
import com.nexusmarket.domain.model.valueobject.Role;
import com.nexusmarket.domain.model.valueobject.WarehouseType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Un vendedor. No se registra solo, lo da de alta un Administrator, por
 * eso no hay un metodo "register" aqui, solo el constructor, que ya pide
 * una bodega inicial. Un vendedor puede tener varias bodegas y varios
 * productos publicados.
 */
public class Seller extends User {

    private final List<Warehouse> warehouses;
    private final List<Product> products;

    public Seller(EntityId id, String fullName, String email, Warehouse initialWarehouse) {
        super(id, fullName, email, Role.SELLER);
        if (initialWarehouse == null) {
            throw new InvalidArgumentException("A seller requires an initial warehouse when onboarded");
        }
        if (initialWarehouse.getType() != WarehouseType.SELLER) {
            throw new InvalidArgumentException("A seller's initial warehouse must be of type SELLER");
        }
        this.warehouses = new ArrayList<>();
        this.products = new ArrayList<>();
        this.warehouses.add(initialWarehouse);
    }

    /** Bodegas adicionales del vendedor; la primera ya quedo fija en el constructor. */
    public void addWarehouse(Warehouse warehouse) {
        if (warehouse == null) {
            throw new InvalidArgumentException("The warehouse cannot be null");
        }
        if (warehouse.getType() != WarehouseType.SELLER) {
            throw new InvalidArgumentException("Only warehouses of type SELLER can be added");
        }
        this.warehouses.add(warehouse);
    }

    public void publishProduct(Product product) {
        if (product == null) {
            throw new InvalidArgumentException("The product cannot be null");
        }
        if (!product.getSellerId().equals(getId())) {
            throw new InvalidArgumentException("The product does not belong to this seller");
        }
        this.products.add(product);
    }

    @Override
    public boolean canOperateOn(User other) {
        return super.canOperateOn(other);
    }

    public List<Warehouse> getWarehouses() {
        return Collections.unmodifiableList(warehouses);
    }

    public List<Product> getProducts() {
        return Collections.unmodifiableList(products);
    }
}

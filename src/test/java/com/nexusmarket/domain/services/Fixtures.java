package com.nexusmarket.domain.services;

import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.models.user.Buyer;
import com.nexusmarket.domain.models.user.LogisticsOperator;
import com.nexusmarket.domain.valueobjects.Address;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.ProductType;

import java.math.BigDecimal;

/** Objetos reales del dominio compartidos por las pruebas de los servicios. */
public final class Fixtures {

    public static final EntityId BUYER_ID = EntityId.of(200001);
    public static final EntityId SELLER_ID = EntityId.of(200002);
    public static final EntityId LOGISTICS_ID = EntityId.of(200003);
    public static final EntityId PRODUCT_ID = EntityId.of(200010);
    public static final EntityId WAREHOUSE_ID = EntityId.of(200020);

    private Fixtures() {
    }

    public static Buyer buyer() {
        return new Buyer(BUYER_ID, "123", "Ana Perez", "ana@mail.com",
                new Address("Calle 1", "Medellin", "Antioquia", "CO", "050001"));
    }

    public static LogisticsOperator logistics() {
        return new LogisticsOperator(LOGISTICS_ID, "456", "Luis Log", "luis@mail.com");
    }

    public static Product physicalProduct() {
        return new Product(PRODUCT_ID, SELLER_ID, "Camiseta", new BigDecimal("50000"), ProductType.PHYSICAL);
    }

    public static Product digitalProduct() {
        return new Product(PRODUCT_ID, SELLER_ID, "E-book", new BigDecimal("20000"), ProductType.DIGITAL);
    }

    public static Inventory inventory(int id, int stock) {
        Inventory inventory = new Inventory(EntityId.of(300000 + id), PRODUCT_ID, EntityId.of(400000 + id));
        if (stock > 0) {
            inventory.receiveStock(stock);
        }
        return inventory;
    }
}

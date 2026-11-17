package com.nexusmarket.domain.services.cart;

import com.nexusmarket.domain.exceptions.InsufficientInventoryException;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import org.springframework.stereotype.Component;

/**
 * Bloquea en el carrito cantidades de un producto fisico mayores a las unidades
 * libres en inventario (disponibles menos reservadas). Los digitales no tienen
 * limite de stock.
 */
@Component
public class CartStockValidator {

    private final InventoryRepositoryPort inventoryRepositoryPort;

    public CartStockValidator(InventoryRepositoryPort inventoryRepositoryPort) {
        this.inventoryRepositoryPort = inventoryRepositoryPort;
    }

    public void validate(Product product, int requestedQuantity) {
        if (!product.requiresPhysicalShipping()) {
            return;
        }
        int free = inventoryRepositoryPort.findByProductId(product.getId()).stream()
                .mapToInt(Inventory::getAvailableFreeQuantity).sum();
        if (requestedQuantity > free) {
            throw new InsufficientInventoryException("Insufficient inventory for product " + product.getId()
                    + ": requested " + requestedQuantity + ", available " + free);
        }
    }
}

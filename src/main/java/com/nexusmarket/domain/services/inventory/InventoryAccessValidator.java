package com.nexusmarket.domain.services.inventory;

import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.inventory.Warehouse;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import org.springframework.stereotype.Component;

/**
 * Matriz de responsabilidades: "Administracion Inventario" le corresponde al
 * vendedor y al Operador Logistico. Un vendedor solo trabaja sobre sus propios
 * productos en sus propias bodegas; el comprador nunca administra inventarios
 * (Dominio 2).
 */
@Component
public class InventoryAccessValidator {

    private final AuthorizationService authorizationService;
    private final ProductRepositoryPort productRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;

    public InventoryAccessValidator(AuthorizationService authorizationService,
                                    ProductRepositoryPort productRepositoryPort,
                                    WarehouseRepositoryPort warehouseRepositoryPort) {
        this.authorizationService = authorizationService;
        this.productRepositoryPort = productRepositoryPort;
        this.warehouseRepositoryPort = warehouseRepositoryPort;
    }

    public Product validate(EntityId actorId, EntityId productId, EntityId warehouseId) {
        User actor = authorizationService.requireRole(actorId, Role.SELLER, Role.LOGISTICS_OPERATOR);
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
        Warehouse warehouse = warehouseRepositoryPort.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse", warehouseId));
        if (actor.getRole() == Role.SELLER) {
            if (!product.getSellerId().equals(actor.getId())) {
                throw new ForbiddenOperationException("The product " + productId + " belongs to another seller");
            }
            if (!actor.getId().equals(warehouse.getOwnerId())) {
                throw new ForbiddenOperationException("The warehouse " + warehouseId + " does not belong to the seller");
            }
        }
        return product;
    }
}

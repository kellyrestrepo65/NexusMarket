package com.nexusmarket.domain.services.inventory;

import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.ConsultInventoryUseCase;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Inventario distribuido de un producto entre bodegas. El comprador nunca
 * consulta inventarios (Dominio 2).
 */
@Service
public class ConsultInventoryService implements ConsultInventoryUseCase {

    private final AuthorizationService authorizationService;
    private final ProductRepositoryPort productRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    public ConsultInventoryService(AuthorizationService authorizationService,
                                   ProductRepositoryPort productRepositoryPort,
                                   InventoryRepositoryPort inventoryRepositoryPort) {
        this.authorizationService = authorizationService;
        this.productRepositoryPort = productRepositoryPort;
        this.inventoryRepositoryPort = inventoryRepositoryPort;
    }

    @Override
    public List<Inventory> execute(EntityId actorId, EntityId productId) {
        User actor = authorizationService.requireRole(actorId,
                Role.SELLER, Role.LOGISTICS_OPERATOR, Role.SUPERVISOR);
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
        if (actor.getRole() == Role.SELLER && !product.getSellerId().equals(actor.getId())) {
            throw new ForbiddenOperationException("The product " + productId + " belongs to another seller");
        }
        return inventoryRepositoryPort.findByProductId(productId);
    }
}

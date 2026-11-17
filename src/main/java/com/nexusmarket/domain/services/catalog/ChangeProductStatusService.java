package com.nexusmarket.domain.services.catalog;

import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.ProductStatus;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.ChangeProductStatusUseCase;
import org.springframework.stereotype.Service;

/**
 * Estado del Dominio 5: Publicado, Suspendido o Descontinuado. Un vendedor solo
 * cambia sus propios productos.
 */
@Service
public class ChangeProductStatusService implements ChangeProductStatusUseCase {

    private final AuthorizationService authorizationService;
    private final ProductRepositoryPort productRepositoryPort;

    public ChangeProductStatusService(AuthorizationService authorizationService,
                                      ProductRepositoryPort productRepositoryPort) {
        this.authorizationService = authorizationService;
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public Product execute(EntityId actorId, EntityId productId, ProductStatus newStatus) {
        User seller = authorizationService.requireRole(actorId, Role.SELLER);
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
        if (!product.getSellerId().equals(seller.getId())) {
            throw new ForbiddenOperationException("The product " + productId + " belongs to another seller");
        }
        switch (newStatus) {
            case PUBLISHED -> product.publish();
            case SUSPENDED -> product.suspend();
            case DISCONTINUED -> product.discontinue();
        }
        return productRepositoryPort.save(product);
    }
}

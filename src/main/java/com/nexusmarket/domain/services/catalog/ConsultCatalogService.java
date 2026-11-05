package com.nexusmarket.domain.services.catalog;

import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.ProductStatus;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.ConsultCatalogUseCase;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Flujo 6.1 "Publicacion": los productos publicados se ven en el catalogo
 * publico. Un vendedor tambien consulta todos sus productos, sin importar su
 * estado.
 */
@Service
public class ConsultCatalogService implements ConsultCatalogUseCase {

    private final AuthorizationService authorizationService;
    private final ProductRepositoryPort productRepositoryPort;

    public ConsultCatalogService(AuthorizationService authorizationService,
                                 ProductRepositoryPort productRepositoryPort) {
        this.authorizationService = authorizationService;
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public List<Product> executePublished(EntityId actorId) {
        authorizationService.requireActiveUser(actorId);
        return productRepositoryPort.findByStatus(ProductStatus.PUBLISHED);
    }

    @Override
    public List<Product> executeOwn(EntityId actorId) {
        User seller = authorizationService.requireRole(actorId, Role.SELLER);
        return productRepositoryPort.findBySellerId(seller.getId());
    }
}

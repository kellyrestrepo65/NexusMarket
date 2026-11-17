package com.nexusmarket.domain.services.catalog;

import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.catalog.Variant;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.ProductType;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.RegisterProductUseCase;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Flujo 6.1 "Catalogo": el vendedor registra sus productos y define sus
 * caracteristicas (tipo y variantes, Dominio 5).
 */
@Service
public class RegisterProductService implements RegisterProductUseCase {

    private final AuthorizationService authorizationService;
    private final ProductRepositoryPort productRepositoryPort;

    public RegisterProductService(AuthorizationService authorizationService,
                                  ProductRepositoryPort productRepositoryPort) {
        this.authorizationService = authorizationService;
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public Product execute(EntityId actorId, String name, BigDecimal price, ProductType type,
                           Map<String, String> variants) {
        User seller = authorizationService.requireRole(actorId, Role.SELLER);
        Product product = new Product(EntityId.next(), seller.getId(), name, price, type);
        if (variants != null) {
            variants.forEach((attribute, value) ->
                    product.addVariant(new Variant(EntityId.next(), attribute, value)));
        }
        return productRepositoryPort.save(product);
    }
}

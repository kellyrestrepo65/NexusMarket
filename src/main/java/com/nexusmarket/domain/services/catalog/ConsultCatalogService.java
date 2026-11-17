package com.nexusmarket.domain.services.catalog;

import com.nexusmarket.domain.models.catalog.CatalogEntry;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.ProductStatus;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.ConsultCatalogUseCase;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Flujo 6.1 "Publicacion": los productos publicados se ven en el catalogo
 * publico. Un vendedor tambien consulta todos sus productos, sin importar su
 * estado.
 */
@Service
public class ConsultCatalogService implements ConsultCatalogUseCase {

    private final AuthorizationService authorizationService;
    private final ProductRepositoryPort productRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    public ConsultCatalogService(AuthorizationService authorizationService,
                                 ProductRepositoryPort productRepositoryPort,
                                 UserRepositoryPort userRepositoryPort,
                                 InventoryRepositoryPort inventoryRepositoryPort) {
        this.authorizationService = authorizationService;
        this.productRepositoryPort = productRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.inventoryRepositoryPort = inventoryRepositoryPort;
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

    @Override
    public List<CatalogEntry> executeShowcase(EntityId actorId) {
        List<Product> products = executePublished(actorId);
        // stock libre por producto sumando todas las bodegas
        Map<EntityId, Integer> stock = inventoryRepositoryPort.findAll().stream()
                .collect(Collectors.groupingBy(Inventory::getProductId,
                        Collectors.summingInt(Inventory::getAvailableFreeQuantity)));
        // nombre de cada vendedor, consultado una sola vez
        Map<EntityId, String> sellers = new HashMap<>();
        return products.stream().map(p -> new CatalogEntry(p,
                sellers.computeIfAbsent(p.getSellerId(), id -> userRepositoryPort.findById(id)
                        .map(User::getFullName).orElse("Vendedor")),
                stock.getOrDefault(p.getId(), 0))).toList();
    }
}

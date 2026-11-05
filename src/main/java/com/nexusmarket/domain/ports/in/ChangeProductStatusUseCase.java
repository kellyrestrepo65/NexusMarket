package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.enums.ProductStatus;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * Estado del Dominio 5: Publicado, Suspendido o Descontinuado. Un vendedor solo
 * cambia sus propios productos.
 */
public interface ChangeProductStatusUseCase {

    Product execute(EntityId actorId, EntityId productId, ProductStatus newStatus);
}

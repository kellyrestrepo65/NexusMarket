package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.valueobjects.EntityId;
import java.util.List;

/**
 * Flujo 6.1 "Publicacion": los productos publicados se ven en el catalogo
 * publico. Un vendedor tambien consulta todos sus productos, sin importar su
 * estado.
 */
public interface ConsultCatalogUseCase {

    List<Product> executePublished(EntityId actorId);

    List<Product> executeOwn(EntityId actorId);
}

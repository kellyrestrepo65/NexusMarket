package com.nexusmarket.domain.models.catalog;

/**
 * Producto publicado tal como se muestra en la vitrina: con el nombre del
 * vendedor y las unidades libres sumadas de todas las bodegas.
 */
public record CatalogEntry(Product product, String sellerName, int freeStock) {
}

# Servicios de Catalogo

Paquete: `domain/services/catalog`. Objetivos: OBJ-04 (Dominio 5).

| Servicio (puerto de entrada) | Firma | Reglas |
|---|---|---|
| RegisterProductService (RegisterProductUseCase) | `execute(actorId, name, price, ProductType, variants)` | Vendedor; el producto inicia PUBLISHED |
| ChangeProductStatusService (ChangeProductStatusUseCase) | `execute(actorId, productId, ProductStatus)` | solo el vendedor dueño |
| ConsultCatalogService (ConsultCatalogUseCase) | `executePublished(actorId)`, `executeOwn(actorId)` | catalogo publico: solo publicados; el vendedor ve todos los suyos |

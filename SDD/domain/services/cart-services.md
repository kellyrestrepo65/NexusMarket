# Servicios de Carrito

Paquete: `domain/services/cart`. Objetivos: OBJ-06 (Dominio 2, RG-03).

El carrito siempre es el del comprador identificado; no hay id de comprador en la ruta.

| Servicio (puerto de entrada) | Firma | Reglas |
|---|---|---|
| AddProductToCartService (AddProductToCartUseCase) | `execute(actorId, productId, quantity)` | Comprador ENABLED; producto PUBLISHED; crea el carrito la primera vez |
| ChangeCartItemQuantityService (ChangeCartItemQuantityUseCase) | `execute(actorId, productId, newQuantity)` | cantidad > 0 |
| RemoveProductFromCartService (RemoveProductFromCartUseCase) | `execute(actorId, productId)` | |
| ClearCartService (ClearCartUseCase) | `execute(actorId)` | |
| ConsultCartService (ConsultCartUseCase) | `execute(actorId)` | si nunca agrego nada recibe un carrito vacio |

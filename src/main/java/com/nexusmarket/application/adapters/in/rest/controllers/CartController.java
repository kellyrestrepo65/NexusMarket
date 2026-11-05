package com.nexusmarket.application.adapters.in.rest.controllers;

import com.nexusmarket.infrastructure.security.CurrentUser;

import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.in.AddProductToCartUseCase;
import com.nexusmarket.domain.ports.in.ChangeCartItemQuantityUseCase;
import com.nexusmarket.domain.ports.in.ClearCartUseCase;
import com.nexusmarket.domain.ports.in.ConsultCartUseCase;
import com.nexusmarket.domain.ports.in.RemoveProductFromCartUseCase;
import com.nexusmarket.application.adapters.in.rest.requests.Requests.AddToCartRequest;
import com.nexusmarket.application.adapters.in.rest.requests.Requests.QuantityRequest;
import com.nexusmarket.application.adapters.in.rest.responses.Responses.CartResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** El carrito del comprador identificado (RG-03): no hay id de comprador en la ruta. */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final AddProductToCartUseCase addProductToCartUseCase;
    private final ChangeCartItemQuantityUseCase changeCartItemQuantityUseCase;
    private final RemoveProductFromCartUseCase removeProductFromCartUseCase;
    private final ClearCartUseCase clearCartUseCase;
    private final ConsultCartUseCase consultCartUseCase;

    public CartController(AddProductToCartUseCase addProductToCartUseCase,
                          ChangeCartItemQuantityUseCase changeCartItemQuantityUseCase,
                          RemoveProductFromCartUseCase removeProductFromCartUseCase,
                          ClearCartUseCase clearCartUseCase, ConsultCartUseCase consultCartUseCase) {
        this.addProductToCartUseCase = addProductToCartUseCase;
        this.changeCartItemQuantityUseCase = changeCartItemQuantityUseCase;
        this.removeProductFromCartUseCase = removeProductFromCartUseCase;
        this.clearCartUseCase = clearCartUseCase;
        this.consultCartUseCase = consultCartUseCase;
    }

    @GetMapping
    public CartResponse consult(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor) {
        return CartResponse.from(consultCartUseCase.execute(CurrentUser.of(actor)));
    }

    @PostMapping("/items")
    public CartResponse add(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                            @Valid @RequestBody AddToCartRequest request) {
        return CartResponse.from(addProductToCartUseCase.execute(CurrentUser.of(actor),
                EntityId.of(request.productId()), request.quantity()));
    }

    @PutMapping("/items/{productId}")
    public CartResponse changeQuantity(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                       @PathVariable String productId,
                                       @Valid @RequestBody QuantityRequest request) {
        return CartResponse.from(changeCartItemQuantityUseCase.execute(CurrentUser.of(actor),
                EntityId.of(productId), request.quantity()));
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse remove(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                               @PathVariable String productId) {
        return CartResponse.from(removeProductFromCartUseCase.execute(CurrentUser.of(actor), EntityId.of(productId)));
    }

    @DeleteMapping
    public CartResponse clear(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor) {
        return CartResponse.from(clearCartUseCase.execute(CurrentUser.of(actor)));
    }
}

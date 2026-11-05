package com.nexusmarket.application.adapters.in.rest.controllers;

import com.nexusmarket.infrastructure.security.CurrentUser;

import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.in.CheckoutUseCase;
import com.nexusmarket.domain.ports.in.ConsultOrdersUseCase;
import com.nexusmarket.domain.ports.in.DeliverOrderUseCase;
import com.nexusmarket.domain.ports.in.DispatchOrderUseCase;
import com.nexusmarket.domain.ports.in.PayOrderUseCase;
import com.nexusmarket.application.adapters.in.rest.responses.Responses.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CheckoutUseCase checkoutUseCase;
    private final PayOrderUseCase payOrderUseCase;
    private final DispatchOrderUseCase dispatchOrderUseCase;
    private final DeliverOrderUseCase deliverOrderUseCase;
    private final ConsultOrdersUseCase consultOrdersUseCase;

    public OrderController(CheckoutUseCase checkoutUseCase, PayOrderUseCase payOrderUseCase,
                           DispatchOrderUseCase dispatchOrderUseCase, DeliverOrderUseCase deliverOrderUseCase,
                           ConsultOrdersUseCase consultOrdersUseCase) {
        this.checkoutUseCase = checkoutUseCase;
        this.payOrderUseCase = payOrderUseCase;
        this.dispatchOrderUseCase = dispatchOrderUseCase;
        this.deliverOrderUseCase = deliverOrderUseCase;
        this.consultOrdersUseCase = consultOrdersUseCase;
    }

    /** Confirma el carrito del comprador: crea la orden y reserva el inventario. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor) {
        return OrderResponse.from(checkoutUseCase.execute(CurrentUser.of(actor)));
    }

    @PostMapping("/{orderId}/payment")
    public OrderResponse pay(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                             @PathVariable String orderId) {
        return OrderResponse.from(payOrderUseCase.execute(CurrentUser.of(actor), EntityId.of(orderId)));
    }

    @PostMapping("/{orderId}/dispatch")
    public OrderResponse dispatch(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                  @PathVariable String orderId) {
        return OrderResponse.from(dispatchOrderUseCase.execute(CurrentUser.of(actor), EntityId.of(orderId)));
    }

    @PostMapping("/{orderId}/delivery")
    public OrderResponse deliver(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                 @PathVariable String orderId) {
        return OrderResponse.from(deliverOrderUseCase.execute(CurrentUser.of(actor), EntityId.of(orderId)));
    }

    @GetMapping
    public List<OrderResponse> consult(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor) {
        return consultOrdersUseCase.execute(CurrentUser.of(actor)).stream().map(OrderResponse::from).toList();
    }

    @GetMapping("/{orderId}")
    public OrderResponse consultOne(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                    @PathVariable String orderId) {
        return OrderResponse.from(consultOrdersUseCase.executeOne(CurrentUser.of(actor), EntityId.of(orderId)));
    }
}

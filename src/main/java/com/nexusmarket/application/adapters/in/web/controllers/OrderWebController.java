package com.nexusmarket.application.adapters.in.web.controllers;

import com.nexusmarket.application.adapters.in.rest.responses.Responses.InvoiceResponse;
import com.nexusmarket.application.adapters.in.rest.responses.Responses.OrderResponse;
import com.nexusmarket.application.adapters.in.rest.responses.Responses.ShipmentResponse;
import com.nexusmarket.application.adapters.in.web.support.WebSession;
import com.nexusmarket.domain.ports.in.ConsultInvoicesUseCase;
import com.nexusmarket.domain.ports.in.ConsultOrdersUseCase;
import com.nexusmarket.domain.ports.in.ConsultShipmentsUseCase;
import com.nexusmarket.domain.ports.in.DeliverOrderUseCase;
import com.nexusmarket.domain.ports.in.DispatchOrderUseCase;
import com.nexusmarket.domain.ports.in.PayOrderUseCase;
import com.nexusmarket.domain.valueobjects.EntityId;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Ordenes (pago, despacho y entrega), facturas y envios (flujo 6.1). */
@Controller
public class OrderWebController {

    private final ConsultOrdersUseCase consultOrdersUseCase;
    private final PayOrderUseCase payOrderUseCase;
    private final DispatchOrderUseCase dispatchOrderUseCase;
    private final DeliverOrderUseCase deliverOrderUseCase;
    private final ConsultInvoicesUseCase consultInvoicesUseCase;
    private final ConsultShipmentsUseCase consultShipmentsUseCase;

    public OrderWebController(ConsultOrdersUseCase consultOrdersUseCase, PayOrderUseCase payOrderUseCase,
                              DispatchOrderUseCase dispatchOrderUseCase, DeliverOrderUseCase deliverOrderUseCase,
                              ConsultInvoicesUseCase consultInvoicesUseCase,
                              ConsultShipmentsUseCase consultShipmentsUseCase) {
        this.consultOrdersUseCase = consultOrdersUseCase;
        this.payOrderUseCase = payOrderUseCase;
        this.dispatchOrderUseCase = dispatchOrderUseCase;
        this.deliverOrderUseCase = deliverOrderUseCase;
        this.consultInvoicesUseCase = consultInvoicesUseCase;
        this.consultShipmentsUseCase = consultShipmentsUseCase;
    }

    @GetMapping("/orders")
    public String orders(HttpSession session, Model model) {
        model.addAttribute("orders", consultOrdersUseCase.execute(WebSession.actor(session)).stream()
                .map(OrderResponse::from).toList());
        return "orders";
    }

    @PostMapping("/orders/{id}/payment")
    public String pay(@PathVariable String id, HttpSession session, RedirectAttributes redirect) {
        payOrderUseCase.execute(WebSession.actor(session), EntityId.of(id));
        redirect.addFlashAttribute("success", "Orden pagada y factura emitida");
        return "redirect:/orders";
    }

    @PostMapping("/orders/{id}/dispatch")
    public String dispatch(@PathVariable String id, HttpSession session, RedirectAttributes redirect) {
        dispatchOrderUseCase.execute(WebSession.actor(session), EntityId.of(id));
        redirect.addFlashAttribute("success", "Orden despachada");
        return "redirect:/orders";
    }

    @PostMapping("/orders/{id}/delivery")
    public String deliver(@PathVariable String id, HttpSession session, RedirectAttributes redirect) {
        deliverOrderUseCase.execute(WebSession.actor(session), EntityId.of(id));
        redirect.addFlashAttribute("success", "Entrega confirmada");
        return "redirect:/orders";
    }

    @GetMapping("/invoices")
    public String invoices(HttpSession session, Model model) {
        model.addAttribute("invoices", consultInvoicesUseCase.execute(WebSession.actor(session)).stream()
                .map(InvoiceResponse::from).toList());
        return "invoices";
    }

    @GetMapping("/shipments")
    public String shipments(HttpSession session, Model model) {
        model.addAttribute("shipments", consultShipmentsUseCase.execute(WebSession.actor(session)).stream()
                .map(ShipmentResponse::from).toList());
        return "shipments";
    }
}

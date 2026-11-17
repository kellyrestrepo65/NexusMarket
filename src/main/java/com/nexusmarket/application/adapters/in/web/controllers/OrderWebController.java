package com.nexusmarket.application.adapters.in.web.controllers;

import com.nexusmarket.application.adapters.in.rest.responses.Responses.InvoiceResponse;
import com.nexusmarket.application.adapters.in.rest.responses.Responses.ShipmentResponse;
import com.nexusmarket.application.adapters.in.web.support.WebSession;
import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.order.Invoice;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.user.Buyer;
import com.nexusmarket.domain.models.user.Seller;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.ports.in.ConsultInvoicesUseCase;
import com.nexusmarket.domain.ports.in.ConsultOrdersUseCase;
import com.nexusmarket.domain.ports.in.ConsultShipmentsUseCase;
import com.nexusmarket.domain.ports.in.DeliverOrderUseCase;
import com.nexusmarket.domain.ports.in.DispatchOrderUseCase;
import com.nexusmarket.domain.ports.in.PayOrderUseCase;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.valueobjects.EntityId;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Ordenes (pago, despacho y entrega), facturas y envios (flujo 6.1). */
@Controller
public class OrderWebController {

    private final ConsultOrdersUseCase consultOrdersUseCase;
    private final PayOrderUseCase payOrderUseCase;
    private final DispatchOrderUseCase dispatchOrderUseCase;
    private final DeliverOrderUseCase deliverOrderUseCase;
    private final ConsultInvoicesUseCase consultInvoicesUseCase;
    private final ConsultShipmentsUseCase consultShipmentsUseCase;
    private final ProductRepositoryPort productRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;

    public OrderWebController(ConsultOrdersUseCase consultOrdersUseCase, PayOrderUseCase payOrderUseCase,
                              DispatchOrderUseCase dispatchOrderUseCase, DeliverOrderUseCase deliverOrderUseCase,
                              ConsultInvoicesUseCase consultInvoicesUseCase,
                              ConsultShipmentsUseCase consultShipmentsUseCase,
                              ProductRepositoryPort productRepositoryPort, UserRepositoryPort userRepositoryPort,
                              OrderRepositoryPort orderRepositoryPort) {
        this.consultOrdersUseCase = consultOrdersUseCase;
        this.payOrderUseCase = payOrderUseCase;
        this.dispatchOrderUseCase = dispatchOrderUseCase;
        this.deliverOrderUseCase = deliverOrderUseCase;
        this.consultInvoicesUseCase = consultInvoicesUseCase;
        this.consultShipmentsUseCase = consultShipmentsUseCase;
        this.productRepositoryPort = productRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
    }

    // Vistas para las plantillas: lineas con nombre del producto y orden con su factura (si ya existe)
    public record Line(String productId, String name, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
    }

    public record OrderView(String id, String buyerId, String buyerName, String status, BigDecimal total,
                            int units, List<Line> lines, String invoiceId) {
    }

    public record InvoiceView(String id, String orderId, BigDecimal total, LocalDateTime issueDate,
                              String buyerName) {
    }

    public record BuyerData(String name, String document, String email, String address) {
    }

    @GetMapping("/orders")
    public String orders(HttpSession session, Model model) {
        EntityId actorId = WebSession.actor(session);
        Map<String, String> invoiceByOrder = invoiceByOrder(actorId);
        Optional<Set<EntityId>> visible = sellerProducts(actorId);
        model.addAttribute("orders", consultOrdersUseCase.execute(actorId).stream()
                .map(order -> toView(order, invoiceByOrder.get(order.getId().toString()), visible))
                .toList());
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String order(@PathVariable String id, HttpSession session, Model model) {
        EntityId actorId = WebSession.actor(session);
        Order order = findOrder(actorId, id);
        model.addAttribute("order", toView(order, invoiceByOrder(actorId).get(order.getId().toString()),
                sellerProducts(actorId)));
        return "order-detail";
    }

    @PostMapping("/orders/{id}/payment")
    public String pay(@PathVariable String id, HttpSession session, RedirectAttributes redirect) {
        EntityId actorId = WebSession.actor(session);
        payOrderUseCase.execute(actorId, EntityId.of(id));
        redirect.addFlashAttribute("success", "Pago aprobado. Tu factura ya fue emitida.");
        // se lleva al comprador directo a la factura recien emitida
        String invoiceId = invoiceByOrder(actorId).get(id);
        return invoiceId == null ? "redirect:/orders/" + id : "redirect:/invoices/" + invoiceId;
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
        EntityId actorId = WebSession.actor(session);
        // la factura ya viene autorizada por consultInvoices; la orden se lee solo para el nombre del cliente
        model.addAttribute("invoices", consultInvoicesUseCase.execute(actorId).stream()
                .map(f -> {
                    String buyer = orderRepositoryPort.findById(f.getOrderId())
                            .map(o -> userName(o.getBuyerId())).orElse("-");
                    InvoiceResponse r = InvoiceResponse.from(f);
                    return new InvoiceView(r.id(), r.orderId(), r.total(), r.issueDate(), buyer);
                })
                .toList());
        return "invoices";
    }

    @GetMapping("/invoices/{id}")
    public String invoice(@PathVariable String id, HttpSession session, Model model) {
        EntityId actorId = WebSession.actor(session);
        // solo se buscan facturas visibles para el actor, asi se respeta la misma autorizacion del listado
        Invoice invoice = consultInvoicesUseCase.execute(actorId).stream()
                .filter(f -> f.getId().toString().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Invoice", id));
        Order order = orderRepositoryPort.findById(invoice.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order", invoice.getOrderId().toString()));
        model.addAttribute("invoice", InvoiceResponse.from(invoice));
        model.addAttribute("order", toView(order, id, Optional.empty()));
        model.addAttribute("buyer", buyerData(order.getBuyerId()));
        return "invoice-detail";
    }

    @GetMapping("/shipments")
    public String shipments(HttpSession session, Model model) {
        model.addAttribute("shipments", consultShipmentsUseCase.execute(WebSession.actor(session)).stream()
                .map(ShipmentResponse::from).toList());
        return "shipments";
    }

    private Order findOrder(EntityId actorId, String id) {
        return consultOrdersUseCase.execute(actorId).stream()
                .filter(o -> o.getId().toString().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Order", id));
    }

    private Map<String, String> invoiceByOrder(EntityId actorId) {
        try {
            return consultInvoicesUseCase.execute(actorId).stream()
                    .collect(Collectors.toMap(f -> f.getOrderId().toString(), f -> f.getId().toString(), (a, b) -> a));
        } catch (ForbiddenOperationException e) {
            // vendedor y operador logistico ven ordenes pero no facturas (RG-03)
            return Map.of();
        }
    }

    // Si el actor es vendedor devuelve sus productos: solo vera esas lineas de la orden (seccion 5)
    private Optional<Set<EntityId>> sellerProducts(EntityId actorId) {
        return userRepositoryPort.findById(actorId)
                .filter(user -> user instanceof Seller)
                .map(user -> productRepositoryPort.findBySellerId(user.getId()).stream()
                        .map(Product::getId).collect(Collectors.toSet()));
    }

    private OrderView toView(Order order, String invoiceId, Optional<Set<EntityId>> visible) {
        List<Line> lines = order.getItems().stream()
                .filter(i -> visible.map(ids -> ids.contains(i.getProductId())).orElse(true))
                .map(i -> new Line(i.getProductId().toString(),
                        productRepositoryPort.findById(i.getProductId()).map(Product::getName)
                                .orElse("Producto " + i.getProductId()),
                        i.getQuantity(), i.getUnitPrice(), i.getSubtotal()))
                .toList();
        int units = lines.stream().mapToInt(Line::quantity).sum();
        return new OrderView(order.getId().toString(), order.getBuyerId().toString(), userName(order.getBuyerId()),
                order.getStatus().name(),
                visible.isPresent() ? lines.stream().map(Line::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                        : order.calculateTotal(),
                units, lines, invoiceId);
    }

    private String userName(EntityId userId) {
        return userRepositoryPort.findById(userId).map(User::getFullName).orElse(userId.toString());
    }

    private BuyerData buyerData(EntityId buyerId) {
        return userRepositoryPort.findById(buyerId).map(user -> {
            String address = user instanceof Buyer buyer && buyer.getMainAddress() != null
                    ? buyer.getMainAddress().getAddressLine1() + ", " + buyer.getMainAddress().getCity() + ", "
                    + buyer.getMainAddress().getStateOrProvince() + ", " + buyer.getMainAddress().getCountry()
                    : "-";
            return new BuyerData(user.getFullName(), user.getIdentityDocument(), user.getEmail(), address);
        }).orElse(new BuyerData(buyerId.toString(), "-", "-", "-"));
    }
}

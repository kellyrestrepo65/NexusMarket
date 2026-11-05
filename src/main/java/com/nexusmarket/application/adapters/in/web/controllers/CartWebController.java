package com.nexusmarket.application.adapters.in.web.controllers;

import com.nexusmarket.application.adapters.in.rest.responses.Responses.ProductResponse;
import com.nexusmarket.application.adapters.in.web.support.WebSession;
import com.nexusmarket.domain.models.cart.ShoppingCart;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.ports.in.AddProductToCartUseCase;
import com.nexusmarket.domain.ports.in.ChangeCartItemQuantityUseCase;
import com.nexusmarket.domain.ports.in.CheckoutUseCase;
import com.nexusmarket.domain.ports.in.ClearCartUseCase;
import com.nexusmarket.domain.ports.in.ConsultCartUseCase;
import com.nexusmarket.domain.ports.in.ConsultCatalogUseCase;
import com.nexusmarket.domain.ports.in.RemoveProductFromCartUseCase;
import com.nexusmarket.domain.valueobjects.EntityId;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Catalogo publico, carrito y checkout del comprador identificado (RG-03). */
@Controller
public class CartWebController {

    private final ConsultCatalogUseCase consultCatalogUseCase;
    private final ConsultCartUseCase consultCartUseCase;
    private final AddProductToCartUseCase addProductToCartUseCase;
    private final ChangeCartItemQuantityUseCase changeCartItemQuantityUseCase;
    private final RemoveProductFromCartUseCase removeProductFromCartUseCase;
    private final ClearCartUseCase clearCartUseCase;
    private final CheckoutUseCase checkoutUseCase;

    public CartWebController(ConsultCatalogUseCase consultCatalogUseCase, ConsultCartUseCase consultCartUseCase,
                             AddProductToCartUseCase addProductToCartUseCase,
                             ChangeCartItemQuantityUseCase changeCartItemQuantityUseCase,
                             RemoveProductFromCartUseCase removeProductFromCartUseCase,
                             ClearCartUseCase clearCartUseCase, CheckoutUseCase checkoutUseCase) {
        this.consultCatalogUseCase = consultCatalogUseCase;
        this.consultCartUseCase = consultCartUseCase;
        this.addProductToCartUseCase = addProductToCartUseCase;
        this.changeCartItemQuantityUseCase = changeCartItemQuantityUseCase;
        this.removeProductFromCartUseCase = removeProductFromCartUseCase;
        this.clearCartUseCase = clearCartUseCase;
        this.checkoutUseCase = checkoutUseCase;
    }

    @GetMapping("/catalog")
    public String catalog(HttpSession session, Model model) {
        model.addAttribute("products", consultCatalogUseCase.executePublished(WebSession.actor(session))
                .stream().map(ProductResponse::from).toList());
        return "catalog";
    }

    // Fila del carrito con el nombre y el precio actual del catalogo (el precio se fija en el checkout)
    public record CartLine(String productId, String name, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
    }

    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        EntityId actorId = WebSession.actor(session);
        ShoppingCart cart = consultCartUseCase.execute(actorId);
        Map<EntityId, Product> catalog = consultCatalogUseCase.executePublished(actorId).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<CartLine> lines = cart.getItems().stream().map(item -> {
            Product product = catalog.get(item.getProductId());
            BigDecimal price = product == null ? null : product.getPrice();
            return new CartLine(item.getProductId().toString(),
                    product == null ? "(no disponible)" : product.getName(), item.getQuantity(), price,
                    price == null ? null : price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }).toList();
        model.addAttribute("lines", lines);
        model.addAttribute("total", lines.stream().map(CartLine::subtotal).filter(s -> s != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return "cart";
    }

    @PostMapping("/cart/items")
    public String add(@RequestParam String productId, @RequestParam int quantity, HttpSession session,
                      RedirectAttributes redirect) {
        addProductToCartUseCase.execute(WebSession.actor(session), EntityId.of(productId), quantity);
        redirect.addFlashAttribute("success", "Producto agregado al carrito");
        return "redirect:/catalog";
    }

    @PostMapping("/cart/items/{productId}/quantity")
    public String changeQuantity(@PathVariable String productId, @RequestParam int quantity, HttpSession session) {
        changeCartItemQuantityUseCase.execute(WebSession.actor(session), EntityId.of(productId), quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/items/{productId}/removal")
    public String remove(@PathVariable String productId, HttpSession session) {
        removeProductFromCartUseCase.execute(WebSession.actor(session), EntityId.of(productId));
        return "redirect:/cart";
    }

    @PostMapping("/cart/clearing")
    public String clear(HttpSession session) {
        clearCartUseCase.execute(WebSession.actor(session));
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(HttpSession session, RedirectAttributes redirect) {
        Order order = checkoutUseCase.execute(WebSession.actor(session));
        redirect.addFlashAttribute("success", "Orden " + order.getId() + " creada; queda pendiente de pago.");
        return "redirect:/orders";
    }
}

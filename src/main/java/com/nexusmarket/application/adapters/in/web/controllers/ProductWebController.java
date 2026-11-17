package com.nexusmarket.application.adapters.in.web.controllers;

import com.nexusmarket.application.adapters.in.rest.responses.Responses.ProductResponse;
import com.nexusmarket.application.adapters.in.web.support.WebSession;
import com.nexusmarket.domain.enums.ProductStatus;
import com.nexusmarket.domain.enums.ProductType;
import com.nexusmarket.domain.ports.in.ChangeProductStatusUseCase;
import com.nexusmarket.domain.ports.in.ConsultCatalogUseCase;
import com.nexusmarket.domain.ports.in.RegisterProductUseCase;
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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/** Productos del vendedor identificado: registro con variantes y cambio de estado. */
@Controller
public class ProductWebController {

    private final ConsultCatalogUseCase consultCatalogUseCase;
    private final RegisterProductUseCase registerProductUseCase;
    private final ChangeProductStatusUseCase changeProductStatusUseCase;

    public ProductWebController(ConsultCatalogUseCase consultCatalogUseCase,
                                RegisterProductUseCase registerProductUseCase,
                                ChangeProductStatusUseCase changeProductStatusUseCase) {
        this.consultCatalogUseCase = consultCatalogUseCase;
        this.registerProductUseCase = registerProductUseCase;
        this.changeProductStatusUseCase = changeProductStatusUseCase;
    }

    @GetMapping("/products")
    public String products(HttpSession session, Model model) {
        model.addAttribute("products", consultCatalogUseCase.executeOwn(WebSession.actor(session)).stream()
                .map(ProductResponse::from).toList());
        model.addAttribute("statuses", ProductStatus.values());
        return "products";
    }

    @PostMapping("/products")
    public String register(@RequestParam String name, @RequestParam BigDecimal price, @RequestParam ProductType type,
                           @RequestParam(defaultValue = "") String variants, HttpSession session,
                           RedirectAttributes redirect) {
        registerProductUseCase.execute(WebSession.actor(session), name, price, type, parseVariants(variants));
        redirect.addFlashAttribute("success", "Producto registrado");
        return "redirect:/products";
    }

    @PostMapping("/products/{id}/status")
    public String changeStatus(@PathVariable String id, @RequestParam ProductStatus status, HttpSession session,
                               RedirectAttributes redirect) {
        changeProductStatusUseCase.execute(WebSession.actor(session), EntityId.of(id), status);
        redirect.addFlashAttribute("success", "Estado del producto actualizado");
        return "redirect:/products";
    }

    // "color=rojo, talla=M" -> {color: rojo, talla: M}
    private Map<String, String> parseVariants(String text) {
        Map<String, String> variants = new LinkedHashMap<>();
        Arrays.stream(text.split(","))
                .map(pair -> pair.split("=", 2))
                .filter(pair -> pair.length == 2 && !pair[0].isBlank() && !pair[1].isBlank())
                .forEach(pair -> variants.put(pair[0].trim(), pair[1].trim()));
        return variants;
    }
}

package com.nexusmarket.application.adapters.in.web.controllers;

import com.nexusmarket.application.adapters.in.rest.responses.Responses.WarehouseResponse;
import com.nexusmarket.application.adapters.in.web.support.WebSession;
import com.nexusmarket.domain.enums.WarehouseType;
import com.nexusmarket.domain.ports.in.ConsultWarehousesUseCase;
import com.nexusmarket.domain.ports.in.CreateWarehouseUseCase;
import com.nexusmarket.domain.valueobjects.EntityId;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Bodegas del marketplace y de los vendedores. */
@Controller
public class WarehouseWebController {

    private final ConsultWarehousesUseCase consultWarehousesUseCase;
    private final CreateWarehouseUseCase createWarehouseUseCase;

    public WarehouseWebController(ConsultWarehousesUseCase consultWarehousesUseCase,
                                  CreateWarehouseUseCase createWarehouseUseCase) {
        this.consultWarehousesUseCase = consultWarehousesUseCase;
        this.createWarehouseUseCase = createWarehouseUseCase;
    }

    @GetMapping("/warehouses")
    public String warehouses(HttpSession session, Model model) {
        model.addAttribute("warehouses", consultWarehousesUseCase.execute(WebSession.actor(session)).stream()
                .map(WarehouseResponse::from).toList());
        model.addAttribute("types", WarehouseType.values());
        return "warehouses";
    }

    @PostMapping("/warehouses")
    public String create(@RequestParam String name, @RequestParam WarehouseType type,
                         @RequestParam(required = false) String sellerId, HttpSession session,
                         RedirectAttributes redirect) {
        EntityId seller = sellerId == null || sellerId.isBlank() ? null : EntityId.of(sellerId.trim());
        createWarehouseUseCase.execute(WebSession.actor(session), name, type, seller);
        redirect.addFlashAttribute("success", "Bodega creada");
        return "redirect:/warehouses";
    }
}

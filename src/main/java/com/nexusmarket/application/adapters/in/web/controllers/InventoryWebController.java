package com.nexusmarket.application.adapters.in.web.controllers;

import com.nexusmarket.application.adapters.in.rest.responses.Responses.InventoryResponse;
import com.nexusmarket.application.adapters.in.rest.responses.Responses.ProductResponse;
import com.nexusmarket.application.adapters.in.rest.responses.Responses.WarehouseResponse;
import com.nexusmarket.application.adapters.in.web.support.WebSession;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.ports.in.AdjustInventoryUseCase;
import com.nexusmarket.domain.ports.in.ConsultCatalogUseCase;
import com.nexusmarket.domain.ports.in.ConsultInventoryUseCase;
import com.nexusmarket.domain.ports.in.ConsultUserUseCase;
import com.nexusmarket.domain.ports.in.ConsultWarehousesUseCase;
import com.nexusmarket.domain.ports.in.MarkInventoryDamagedUseCase;
import com.nexusmarket.domain.ports.in.ReceiveStockUseCase;
import com.nexusmarket.domain.valueobjects.EntityId;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Consulta de existencias por producto, recepcion, ajustes y unidades danadas. */
@Controller
public class InventoryWebController {

    private final ConsultUserUseCase consultUserUseCase;
    private final ConsultCatalogUseCase consultCatalogUseCase;
    private final ConsultWarehousesUseCase consultWarehousesUseCase;
    private final ConsultInventoryUseCase consultInventoryUseCase;
    private final ReceiveStockUseCase receiveStockUseCase;
    private final AdjustInventoryUseCase adjustInventoryUseCase;
    private final MarkInventoryDamagedUseCase markInventoryDamagedUseCase;

    public InventoryWebController(ConsultUserUseCase consultUserUseCase, ConsultCatalogUseCase consultCatalogUseCase,
                                  ConsultWarehousesUseCase consultWarehousesUseCase,
                                  ConsultInventoryUseCase consultInventoryUseCase,
                                  ReceiveStockUseCase receiveStockUseCase,
                                  AdjustInventoryUseCase adjustInventoryUseCase,
                                  MarkInventoryDamagedUseCase markInventoryDamagedUseCase) {
        this.consultUserUseCase = consultUserUseCase;
        this.consultCatalogUseCase = consultCatalogUseCase;
        this.consultWarehousesUseCase = consultWarehousesUseCase;
        this.consultInventoryUseCase = consultInventoryUseCase;
        this.receiveStockUseCase = receiveStockUseCase;
        this.adjustInventoryUseCase = adjustInventoryUseCase;
        this.markInventoryDamagedUseCase = markInventoryDamagedUseCase;
    }

    @GetMapping("/inventory")
    public String inventory(@RequestParam(required = false) String productId, HttpSession session, Model model) {
        EntityId actorId = WebSession.actor(session);
        User user = consultUserUseCase.execute(actorId, actorId);
        // el vendedor solo opera sus productos; los demas roles ven el catalogo publicado
        List<Product> products = user.getRole() == Role.SELLER
                ? consultCatalogUseCase.executeOwn(actorId)
                : consultCatalogUseCase.executePublished(actorId);
        boolean canOperate = user.getRole() == Role.SELLER || user.getRole() == Role.LOGISTICS_OPERATOR;
        model.addAttribute("products", products.stream().map(ProductResponse::from).toList());
        model.addAttribute("canOperate", canOperate);
        model.addAttribute("warehouses", canOperate
                ? consultWarehousesUseCase.execute(actorId).stream().map(WarehouseResponse::from).toList()
                : List.of());
        model.addAttribute("productId", productId);
        model.addAttribute("inventories", productId == null || productId.isBlank() ? List.of()
                : consultInventoryUseCase.execute(actorId, EntityId.of(productId)).stream()
                .map(InventoryResponse::from).toList());
        return "inventory";
    }

    @PostMapping("/inventory/receipts")
    public String receive(@RequestParam String productId, @RequestParam String warehouseId,
                          @RequestParam int quantity, HttpSession session, RedirectAttributes redirect) {
        receiveStockUseCase.execute(WebSession.actor(session), EntityId.of(productId), EntityId.of(warehouseId),
                quantity);
        redirect.addFlashAttribute("success", "Recepcion registrada");
        return "redirect:/inventory?productId=" + productId;
    }

    @PostMapping("/inventory/{id}/adjustments")
    public String adjust(@PathVariable String id, @RequestParam int delta, @RequestParam String productId,
                         HttpSession session, RedirectAttributes redirect) {
        adjustInventoryUseCase.execute(WebSession.actor(session), EntityId.of(id), delta);
        redirect.addFlashAttribute("success", "Inventario ajustado");
        return "redirect:/inventory?productId=" + productId;
    }

    @PostMapping("/inventory/{id}/damaged")
    public String damaged(@PathVariable String id, @RequestParam int quantity, @RequestParam String productId,
                          HttpSession session, RedirectAttributes redirect) {
        markInventoryDamagedUseCase.execute(WebSession.actor(session), EntityId.of(id), quantity);
        redirect.addFlashAttribute("success", "Unidades marcadas como danadas");
        return "redirect:/inventory?productId=" + productId;
    }
}

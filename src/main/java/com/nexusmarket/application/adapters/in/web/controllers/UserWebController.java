package com.nexusmarket.application.adapters.in.web.controllers;

import com.nexusmarket.application.adapters.in.rest.responses.Responses.UserResponse;
import com.nexusmarket.application.adapters.in.web.support.WebSession;
import com.nexusmarket.domain.models.user.Seller;
import com.nexusmarket.domain.ports.in.ChangeUserStatusUseCase;
import com.nexusmarket.domain.ports.in.ConsultUserUseCase;
import com.nexusmarket.domain.ports.in.RegisterSellerUseCase;
import com.nexusmarket.domain.valueobjects.EntityId;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Administracion de usuarios y alta de vendedores. */
@Controller
public class UserWebController {

    private final ConsultUserUseCase consultUserUseCase;
    private final ChangeUserStatusUseCase changeUserStatusUseCase;
    private final RegisterSellerUseCase registerSellerUseCase;

    public UserWebController(ConsultUserUseCase consultUserUseCase, ChangeUserStatusUseCase changeUserStatusUseCase,
                             RegisterSellerUseCase registerSellerUseCase) {
        this.consultUserUseCase = consultUserUseCase;
        this.changeUserStatusUseCase = changeUserStatusUseCase;
        this.registerSellerUseCase = registerSellerUseCase;
    }

    @GetMapping("/users")
    public String users(HttpSession session, Model model) {
        model.addAttribute("users", consultUserUseCase.executeAll(WebSession.actor(session)).stream()
                .map(UserResponse::from).toList());
        return "users";
    }

    @PostMapping("/users/{id}/status")
    public String changeStatus(@PathVariable String id, @RequestParam boolean active, HttpSession session,
                               RedirectAttributes redirect) {
        changeUserStatusUseCase.execute(WebSession.actor(session), EntityId.of(id), active);
        redirect.addFlashAttribute("success", active ? "Usuario activado" : "Usuario inactivado");
        return "redirect:/users";
    }

    @GetMapping("/sellers/new")
    public String sellerForm(HttpSession session) {
        WebSession.actor(session);
        return "sellers";
    }

    @PostMapping("/sellers")
    public String registerSeller(@RequestParam String identityDocument, @RequestParam String fullName,
                                 @RequestParam String email, @RequestParam String firstWarehouseName,
                                 HttpSession session, RedirectAttributes redirect) {
        Seller seller = registerSellerUseCase.execute(WebSession.actor(session), identityDocument, fullName, email,
                firstWarehouseName);
        redirect.addFlashAttribute("success", "Vendedor registrado con id " + seller.getId());
        return "redirect:/users";
    }
}

package com.nexusmarket.application.adapters.in.web.controllers;

import com.nexusmarket.application.adapters.in.web.support.WebMenu;
import com.nexusmarket.application.adapters.in.web.support.WebSession;
import com.nexusmarket.domain.models.user.Buyer;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.ports.in.ConsultUserUseCase;
import com.nexusmarket.domain.ports.in.RegisterBuyerUseCase;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import com.nexusmarket.domain.valueobjects.Address;
import com.nexusmarket.domain.valueobjects.EntityId;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Ingreso, salida, autorregistro del comprador y perfil propio. */
@Controller
public class SessionWebController {

    private final ConsultUserUseCase consultUserUseCase;
    private final RegisterBuyerUseCase registerBuyerUseCase;

    private final UserRepositoryPort userRepositoryPort;
    private final boolean demoData;

    public SessionWebController(ConsultUserUseCase consultUserUseCase, RegisterBuyerUseCase registerBuyerUseCase,
                                UserRepositoryPort userRepositoryPort,
                                @Value("${nexusmarket.demo-data:false}") boolean demoData) {
        this.consultUserUseCase = consultUserUseCase;
        this.registerBuyerUseCase = registerBuyerUseCase;
        this.userRepositoryPort = userRepositoryPort;
        this.demoData = demoData;
    }

    // Cuenta de prueba que se muestra en el ingreso para no tener que buscar ids en el log
    public record DemoAccount(String id, String name, String role) {
    }

    @GetMapping("/")
    public String home(HttpSession session, Model model, RedirectAttributes redirect) {
        // reenvia el mensaje flash que llego a "/" para que no se pierda en la segunda redireccion
        for (String key : new String[]{"success", "error"}) {
            if (model.getAttribute(key) != null) {
                redirect.addFlashAttribute(key, model.getAttribute(key));
            }
        }
        if (!WebSession.isLoggedIn(session)) {
            return "redirect:/login";
        }
        EntityId actorId = WebSession.actor(session);
        User user = consultUserUseCase.execute(actorId, actorId);
        return "redirect:" + WebMenu.homeOf(user.getRole().name());
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        if (demoData) {
            model.addAttribute("demoAccounts", userRepositoryPort.findAll().stream()
                    .map(u -> new DemoAccount(u.getId().toString(), u.getFullName(),
                            WebMenu.LABELS.getOrDefault(u.getRole().name(), u.getRole().name())))
                    .toList());
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String userId, HttpSession session) {
        EntityId actorId = EntityId.of(userId.trim());
        // el dominio valida que exista y este activo (RG-01)
        consultUserUseCase.execute(actorId, actorId);
        WebSession.login(session, actorId);
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        WebSession.logout(session);
        return "redirect:/login";
    }

    @PostMapping("/register")
    public String register(@RequestParam String identityDocument, @RequestParam String fullName,
                           @RequestParam String email, @RequestParam String addressLine1,
                           @RequestParam String city, @RequestParam(required = false) String stateOrProvince,
                           @RequestParam String country, @RequestParam(required = false) String postalCode,
                           HttpSession session, RedirectAttributes redirect) {
        Buyer buyer = registerBuyerUseCase.execute(identityDocument, fullName, email,
                new Address(addressLine1, city, stateOrProvince, country, postalCode));
        WebSession.login(session, buyer.getId());
        redirect.addFlashAttribute("success", "Registro exitoso. Tu id de usuario es " + buyer.getId());
        return "redirect:" + WebMenu.homeOf("BUYER");
    }

    @GetMapping("/profile")
    public String profile(HttpSession session) {
        WebSession.actor(session);
        return "profile";
    }
}

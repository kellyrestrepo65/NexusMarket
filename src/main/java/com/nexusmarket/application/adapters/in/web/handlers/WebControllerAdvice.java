package com.nexusmarket.application.adapters.in.web.handlers;

import com.nexusmarket.application.adapters.in.rest.responses.Responses.UserResponse;
import com.nexusmarket.application.adapters.in.web.support.WebMenu;
import com.nexusmarket.application.adapters.in.web.support.WebSession;
import com.nexusmarket.domain.exceptions.DomainException;
import com.nexusmarket.domain.exceptions.UnauthenticatedException;
import com.nexusmarket.domain.ports.in.ConsultUserUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Comun a todas las vistas: usuario en sesion, menu de su rol y etiquetas.
 * Tambien traduce las excepciones del dominio: en una accion (POST) vuelve a
 * la pagina anterior con el mensaje; al abrir una pagina (GET) muestra la
 * vista de error.
 */
@ControllerAdvice(basePackages = "com.nexusmarket.application.adapters.in.web")
public class WebControllerAdvice {

    private final ConsultUserUseCase consultUserUseCase;

    public WebControllerAdvice(ConsultUserUseCase consultUserUseCase) {
        this.consultUserUseCase = consultUserUseCase;
    }

    @ModelAttribute
    public void common(HttpSession session, HttpServletRequest request, Model model) {
        UserResponse currentUser = null;
        if (WebSession.isLoggedIn(session)) {
            try {
                var actorId = WebSession.actor(session);
                currentUser = UserResponse.from(consultUserUseCase.execute(actorId, actorId));
            } catch (DomainException e) {
                // el usuario fue desactivado mientras tenia la sesion abierta
                WebSession.logout(session);
            }
        }
        List<WebMenu.Item> menu = currentUser == null ? List.of() : WebMenu.forRole(currentUser.role());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("menu", menu);
        model.addAttribute("labels", WebMenu.LABELS);
        model.addAttribute("currentPath", request.getRequestURI());
    }

    @ExceptionHandler(UnauthenticatedException.class)
    public String unauthenticated(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "Ingresa con tu id de usuario para continuar.");
        return "redirect:/login";
    }

    @ExceptionHandler({DomainException.class, OptimisticLockingFailureException.class,
            DataIntegrityViolationException.class, IllegalArgumentException.class})
    public ModelAndView domainError(Exception e, HttpServletRequest request, RedirectAttributes redirect) {
        String message = e instanceof DomainException ? e.getMessage()
                : e instanceof IllegalArgumentException ? "Datos invalidos: " + e.getMessage()
                : "Otro usuario modifico los datos al mismo tiempo; intenta de nuevo.";
        if ("POST".equals(request.getMethod())) {
            redirect.addFlashAttribute("error", message);
            String referer = request.getHeader("Referer");
            return new ModelAndView("redirect:" + (referer == null ? "/" : referer));
        }
        ModelAndView view = new ModelAndView("error-page");
        view.addObject("message", message);
        return view;
    }
}

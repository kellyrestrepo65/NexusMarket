package com.nexusmarket.application.adapters.in.web.controllers;

import com.nexusmarket.application.adapters.in.rest.responses.Responses.ReturnResponse;
import com.nexusmarket.application.adapters.in.web.support.WebSession;
import com.nexusmarket.domain.ports.in.ConsultReturnsUseCase;
import com.nexusmarket.domain.ports.in.ProcessRefundUseCase;
import com.nexusmarket.domain.ports.in.RequestReturnUseCase;
import com.nexusmarket.domain.ports.in.ReviewReturnUseCase;
import com.nexusmarket.domain.valueobjects.EntityId;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Solicitud y revision de devoluciones y gestion de reembolsos. */
@Controller
public class ReturnWebController {

    private final ConsultReturnsUseCase consultReturnsUseCase;
    private final RequestReturnUseCase requestReturnUseCase;
    private final ReviewReturnUseCase reviewReturnUseCase;
    private final ProcessRefundUseCase processRefundUseCase;

    public ReturnWebController(ConsultReturnsUseCase consultReturnsUseCase, RequestReturnUseCase requestReturnUseCase,
                               ReviewReturnUseCase reviewReturnUseCase, ProcessRefundUseCase processRefundUseCase) {
        this.consultReturnsUseCase = consultReturnsUseCase;
        this.requestReturnUseCase = requestReturnUseCase;
        this.reviewReturnUseCase = reviewReturnUseCase;
        this.processRefundUseCase = processRefundUseCase;
    }

    @GetMapping("/returns")
    public String returns(HttpSession session, Model model) {
        model.addAttribute("returns", consultReturnsUseCase.execute(WebSession.actor(session)).stream()
                .map(ReturnResponse::from).toList());
        return "returns";
    }

    @PostMapping("/returns")
    public String request(@RequestParam String orderId, @RequestParam String reason, HttpSession session,
                          RedirectAttributes redirect) {
        requestReturnUseCase.execute(WebSession.actor(session), EntityId.of(orderId), reason);
        redirect.addFlashAttribute("success", "Devolucion solicitada");
        return "redirect:/returns";
    }

    @PostMapping("/returns/{id}/approval")
    public String approve(@PathVariable String id, HttpSession session, RedirectAttributes redirect) {
        reviewReturnUseCase.approve(WebSession.actor(session), EntityId.of(id));
        redirect.addFlashAttribute("success", "Devolucion aprobada; reembolso pendiente");
        return "redirect:/returns";
    }

    @PostMapping("/returns/{id}/rejection")
    public String reject(@PathVariable String id, HttpSession session, RedirectAttributes redirect) {
        reviewReturnUseCase.reject(WebSession.actor(session), EntityId.of(id));
        redirect.addFlashAttribute("success", "Devolucion rechazada");
        return "redirect:/returns";
    }

    @PostMapping("/returns/{id}/refund/processing")
    public String processRefund(@PathVariable String id, HttpSession session, RedirectAttributes redirect) {
        processRefundUseCase.process(WebSession.actor(session), EntityId.of(id));
        redirect.addFlashAttribute("success", "Reembolso procesado");
        return "redirect:/returns";
    }

    @PostMapping("/returns/{id}/refund/rejection")
    public String rejectRefund(@PathVariable String id, HttpSession session, RedirectAttributes redirect) {
        processRefundUseCase.reject(WebSession.actor(session), EntityId.of(id));
        redirect.addFlashAttribute("success", "Reembolso rechazado");
        return "redirect:/returns";
    }
}

package com.nexusmarket.application.adapters.in.web.controllers;

import com.nexusmarket.application.adapters.in.web.support.WebSession;
import com.nexusmarket.domain.ports.in.AdministrativeReportUseCase;
import com.nexusmarket.domain.ports.in.SellerReportUseCase;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** OBJ-12: reporte administrativo consolidado y reportes de vendedores. */
@Controller
public class ReportWebController {

    private final AdministrativeReportUseCase administrativeReportUseCase;
    private final SellerReportUseCase sellerReportUseCase;

    public ReportWebController(AdministrativeReportUseCase administrativeReportUseCase,
                               SellerReportUseCase sellerReportUseCase) {
        this.administrativeReportUseCase = administrativeReportUseCase;
        this.sellerReportUseCase = sellerReportUseCase;
    }

    @GetMapping("/report")
    public String report(HttpSession session, Model model) {
        model.addAttribute("report", administrativeReportUseCase.execute(WebSession.actor(session)));
        return "report";
    }

    /** Administrador/Supervisor ven todos los vendedores; el Vendedor solo el suyo. */
    @GetMapping("/seller-reports")
    public String sellerReports(HttpSession session, Model model) {
        model.addAttribute("sellerReports", sellerReportUseCase.visibleTo(WebSession.actor(session)));
        return "seller-reports";
    }
}

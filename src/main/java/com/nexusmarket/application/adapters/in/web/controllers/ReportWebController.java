package com.nexusmarket.application.adapters.in.web.controllers;

import com.nexusmarket.application.adapters.in.web.support.WebSession;
import com.nexusmarket.domain.ports.in.AdministrativeReportUseCase;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** OBJ-12: reporte administrativo consolidado. */
@Controller
public class ReportWebController {

    private final AdministrativeReportUseCase administrativeReportUseCase;

    public ReportWebController(AdministrativeReportUseCase administrativeReportUseCase) {
        this.administrativeReportUseCase = administrativeReportUseCase;
    }

    @GetMapping("/report")
    public String report(HttpSession session, Model model) {
        model.addAttribute("report", administrativeReportUseCase.execute(WebSession.actor(session)));
        return "report";
    }
}

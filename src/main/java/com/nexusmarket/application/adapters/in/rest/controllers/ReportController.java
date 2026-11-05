package com.nexusmarket.application.adapters.in.rest.controllers;

import com.nexusmarket.infrastructure.security.CurrentUser;

import com.nexusmarket.domain.models.report.AdministrativeReport;
import com.nexusmarket.domain.ports.in.AdministrativeReportUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** OBJ-12: consolidated administrative report. */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final AdministrativeReportUseCase administrativeReportUseCase;

    public ReportController(AdministrativeReportUseCase administrativeReportUseCase) {
        this.administrativeReportUseCase = administrativeReportUseCase;
    }

    @GetMapping("/administrative")
    public AdministrativeReport administrative(
            @RequestHeader(value = CurrentUser.HEADER, required = false) String actor) {
        return administrativeReportUseCase.execute(CurrentUser.of(actor));
    }
}

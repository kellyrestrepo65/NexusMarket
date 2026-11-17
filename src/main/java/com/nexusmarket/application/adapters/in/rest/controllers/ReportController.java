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
    private final com.nexusmarket.domain.ports.in.SellerReportUseCase sellerReportUseCase;

    public ReportController(AdministrativeReportUseCase administrativeReportUseCase,
                            com.nexusmarket.domain.ports.in.SellerReportUseCase sellerReportUseCase) {
        this.administrativeReportUseCase = administrativeReportUseCase;
        this.sellerReportUseCase = sellerReportUseCase;
    }

    @GetMapping("/sellers")
    public java.util.List<com.nexusmarket.domain.models.report.SellerReport> sellers(
            @RequestHeader(value = CurrentUser.HEADER, required = false) String actor) {
        return sellerReportUseCase.all(CurrentUser.of(actor));
    }

    @GetMapping("/sellers/me")
    public com.nexusmarket.domain.models.report.SellerReport mine(
            @RequestHeader(value = CurrentUser.HEADER, required = false) String actor) {
        return sellerReportUseCase.mine(CurrentUser.of(actor));
    }

    @GetMapping("/sellers/{sellerId}")
    public com.nexusmarket.domain.models.report.SellerReport seller(
            @RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
            @org.springframework.web.bind.annotation.PathVariable String sellerId) {
        return sellerReportUseCase.of(CurrentUser.of(actor), CurrentUser.of(sellerId));
    }

    @GetMapping("/administrative")
    public AdministrativeReport administrative(
            @RequestHeader(value = CurrentUser.HEADER, required = false) String actor) {
        return administrativeReportUseCase.execute(CurrentUser.of(actor));
    }
}

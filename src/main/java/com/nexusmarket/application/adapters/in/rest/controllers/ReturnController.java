package com.nexusmarket.application.adapters.in.rest.controllers;

import com.nexusmarket.infrastructure.security.CurrentUser;

import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.in.ConsultReturnsUseCase;
import com.nexusmarket.domain.ports.in.ProcessRefundUseCase;
import com.nexusmarket.domain.ports.in.RequestReturnUseCase;
import com.nexusmarket.domain.ports.in.ReviewReturnUseCase;
import com.nexusmarket.application.adapters.in.rest.requests.Requests.RequestReturnRequest;
import com.nexusmarket.application.adapters.in.rest.responses.Responses.ReturnResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** OBJ-11: devoluciones solicitadas por el comprador y sus reembolsos. */
@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final RequestReturnUseCase requestReturnUseCase;
    private final ReviewReturnUseCase reviewReturnUseCase;
    private final ProcessRefundUseCase processRefundUseCase;
    private final ConsultReturnsUseCase consultReturnsUseCase;

    public ReturnController(RequestReturnUseCase requestReturnUseCase, ReviewReturnUseCase reviewReturnUseCase,
                            ProcessRefundUseCase processRefundUseCase, ConsultReturnsUseCase consultReturnsUseCase) {
        this.requestReturnUseCase = requestReturnUseCase;
        this.reviewReturnUseCase = reviewReturnUseCase;
        this.processRefundUseCase = processRefundUseCase;
        this.consultReturnsUseCase = consultReturnsUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnResponse request(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                  @Valid @RequestBody RequestReturnRequest request) {
        return ReturnResponse.from(requestReturnUseCase.execute(CurrentUser.of(actor),
                EntityId.of(request.orderId()), request.reason()));
    }

    @PostMapping("/{returnId}/approval")
    public ReturnResponse approve(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                  @PathVariable String returnId) {
        return ReturnResponse.from(reviewReturnUseCase.approve(CurrentUser.of(actor), EntityId.of(returnId)));
    }

    @PostMapping("/{returnId}/rejection")
    public ReturnResponse reject(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                 @PathVariable String returnId) {
        return ReturnResponse.from(reviewReturnUseCase.reject(CurrentUser.of(actor), EntityId.of(returnId)));
    }

    @PostMapping("/{returnId}/refund/processing")
    public ReturnResponse processRefund(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                        @PathVariable String returnId) {
        return ReturnResponse.from(processRefundUseCase.process(CurrentUser.of(actor), EntityId.of(returnId)));
    }

    @PostMapping("/{returnId}/refund/rejection")
    public ReturnResponse rejectRefund(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                       @PathVariable String returnId) {
        return ReturnResponse.from(processRefundUseCase.reject(CurrentUser.of(actor), EntityId.of(returnId)));
    }

    @GetMapping
    public List<ReturnResponse> consult(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor) {
        return consultReturnsUseCase.execute(CurrentUser.of(actor)).stream().map(ReturnResponse::from).toList();
    }

    @GetMapping("/{returnId}")
    public ReturnResponse consultOne(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                     @PathVariable String returnId) {
        return ReturnResponse.from(consultReturnsUseCase.executeOne(CurrentUser.of(actor), EntityId.of(returnId)));
    }
}

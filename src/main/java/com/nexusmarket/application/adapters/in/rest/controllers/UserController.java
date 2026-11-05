package com.nexusmarket.application.adapters.in.rest.controllers;

import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.infrastructure.security.CurrentUser;

import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.in.ChangeUserStatusUseCase;
import com.nexusmarket.domain.ports.in.ConsultUserUseCase;
import com.nexusmarket.domain.ports.in.RegisterBuyerUseCase;
import com.nexusmarket.domain.ports.in.RegisterSellerUseCase;
import com.nexusmarket.application.adapters.in.rest.requests.Requests.ChangeUserStatusRequest;
import com.nexusmarket.application.adapters.in.rest.requests.Requests.RegisterBuyerRequest;
import com.nexusmarket.application.adapters.in.rest.requests.Requests.RegisterSellerRequest;
import com.nexusmarket.application.adapters.in.rest.responses.Responses.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RegisterBuyerUseCase registerBuyerUseCase;
    private final RegisterSellerUseCase registerSellerUseCase;
    private final ChangeUserStatusUseCase changeUserStatusUseCase;
    private final ConsultUserUseCase consultUserUseCase;

    public UserController(RegisterBuyerUseCase registerBuyerUseCase, RegisterSellerUseCase registerSellerUseCase,
                          ChangeUserStatusUseCase changeUserStatusUseCase, ConsultUserUseCase consultUserUseCase) {
        this.registerBuyerUseCase = registerBuyerUseCase;
        this.registerSellerUseCase = registerSellerUseCase;
        this.changeUserStatusUseCase = changeUserStatusUseCase;
        this.consultUserUseCase = consultUserUseCase;
    }

    /** Autorregistro publico de compradores: no necesita X-User-Id. */
    @PostMapping("/buyers")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerBuyer(@Valid @RequestBody RegisterBuyerRequest request) {
        return UserResponse.from(registerBuyerUseCase.execute(request.identityDocument(), request.fullName(),
                request.email(), request.mainAddress().toDomain()));
    }

    @PostMapping("/sellers")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerSeller(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                       @Valid @RequestBody RegisterSellerRequest request) {
        return UserResponse.from(registerSellerUseCase.execute(CurrentUser.of(actor), request.identityDocument(),
                request.fullName(), request.email(), request.firstWarehouseName()));
    }

    @PatchMapping("/{userId}/status")
    public UserResponse changeStatus(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                     @PathVariable String userId,
                                     @Valid @RequestBody ChangeUserStatusRequest request) {
        return UserResponse.from(changeUserStatusUseCase.execute(CurrentUser.of(actor), EntityId.of(userId),
                request.active()));
    }

    @GetMapping("/{userId}")
    public UserResponse consult(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                @PathVariable String userId) {
        return UserResponse.from(consultUserUseCase.execute(CurrentUser.of(actor), EntityId.of(userId)));
    }

    @GetMapping
    public List<UserResponse> consultAll(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor) {
        return consultUserUseCase.executeAll(CurrentUser.of(actor)).stream().map(UserResponse::from).toList();
    }
}

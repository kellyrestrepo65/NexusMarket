package com.nexusmarket.domain.services.security;

import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.exceptions.UnauthenticatedException;
import com.nexusmarket.domain.models.user.Buyer;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.nexusmarket.domain.services.Fixtures.BUYER_ID;
import static com.nexusmarket.domain.services.Fixtures.buyer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/** RG-01 (usuario identificado y activo) y RG-03 (permisos por rol). */
@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Test
    void rejectsMissingActor() {
        assertThrows(UnauthenticatedException.class, () -> authorizationService.requireActiveUser(null));
    }

    @Test
    void rejectsUnknownActor() {
        when(userRepositoryPort.findById(BUYER_ID)).thenReturn(Optional.empty());

        assertThrows(UnauthenticatedException.class, () -> authorizationService.requireActiveUser(BUYER_ID));
    }

    @Test
    void rejectsBlockedActor() {
        Buyer buyer = buyer();
        buyer.block();
        when(userRepositoryPort.findById(BUYER_ID)).thenReturn(Optional.of(buyer));

        assertThrows(ForbiddenOperationException.class, () -> authorizationService.requireActiveUser(BUYER_ID));
    }

    @Test
    void rejectsRoleNotAllowed() {
        when(userRepositoryPort.findById(BUYER_ID)).thenReturn(Optional.of(buyer()));

        assertThrows(ForbiddenOperationException.class,
                () -> authorizationService.requireRole(BUYER_ID, Role.ADMINISTRATOR));
    }

    @Test
    void returnsActorWithAllowedRole() {
        when(userRepositoryPort.findById(BUYER_ID)).thenReturn(Optional.of(buyer()));

        assertEquals(BUYER_ID, authorizationService.requireRole(BUYER_ID, Role.BUYER).getId());
    }
}

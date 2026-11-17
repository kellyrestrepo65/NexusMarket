package com.nexusmarket.domain.services.user;

import com.nexusmarket.domain.exceptions.DuplicateDataException;
import com.nexusmarket.domain.models.user.Buyer;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.Address;
import com.nexusmarket.domain.enums.CommercialStatus;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Seccion 11: el documento de identidad y el correo son unicos. */
@ExtendWith(MockitoExtension.class)
class RegisterBuyerServiceTest {

    private static final Address ADDRESS = new Address("Calle 1", "Medellin", null, "CO", null);

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private RegisterBuyerService service;

    @BeforeEach
    void setUp() {
        service = new RegisterBuyerService(userRepositoryPort, new UniqueUserDataValidator(userRepositoryPort));
    }

    @Test
    void registersEnabledBuyer() {
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Buyer buyer = service.execute("123", "Ana Perez", "ana@mail.com", ADDRESS);

        assertEquals(Role.BUYER, buyer.getRole());
        assertEquals(CommercialStatus.ENABLED, buyer.getCommercialStatus());
        verify(userRepositoryPort).save(buyer);
    }

    @Test
    void rejectsDuplicatedDocument() {
        when(userRepositoryPort.existsByIdentityDocument("123")).thenReturn(true);

        assertThrows(DuplicateDataException.class,
                () -> service.execute("123", "Ana Perez", "ana@mail.com", ADDRESS));
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsDuplicatedEmail() {
        when(userRepositoryPort.existsByEmail("ana@mail.com")).thenReturn(true);

        assertThrows(DuplicateDataException.class,
                () -> service.execute("123", "Ana Perez", "ana@mail.com", ADDRESS));
        verify(userRepositoryPort, never()).save(any());
    }
}

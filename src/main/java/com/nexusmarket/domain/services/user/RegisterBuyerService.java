package com.nexusmarket.domain.services.user;

import com.nexusmarket.domain.models.user.Buyer;
import com.nexusmarket.domain.valueobjects.Address;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.ports.in.RegisterBuyerUseCase;
import org.springframework.stereotype.Service;

/**
 * Proceso "Registro de compradores". Es la unica operacion que se ejecuta
 * sin un usuario identificado, porque el usuario todavia no existe.
 */
@Service
public class RegisterBuyerService implements RegisterBuyerUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UniqueUserDataValidator uniqueUserDataValidator;

    public RegisterBuyerService(UserRepositoryPort userRepositoryPort,
                                UniqueUserDataValidator uniqueUserDataValidator) {
        this.userRepositoryPort = userRepositoryPort;
        this.uniqueUserDataValidator = uniqueUserDataValidator;
    }

    @Override
    public Buyer execute(String identityDocument, String fullName, String email, Address mainAddress) {
        uniqueUserDataValidator.validate(identityDocument, email);
        Buyer buyer = new Buyer(EntityId.next(), identityDocument, fullName, email, mainAddress);
        return (Buyer) userRepositoryPort.save(buyer);
    }
}

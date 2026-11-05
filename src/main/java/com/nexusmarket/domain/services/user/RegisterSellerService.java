package com.nexusmarket.domain.services.user;

import com.nexusmarket.domain.models.user.Administrator;

import com.nexusmarket.domain.models.inventory.Warehouse;
import com.nexusmarket.domain.models.user.Seller;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.RegisterSellerUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dominio 3 y flujo 6.1 "Incorporacion": los vendedores no se autorregistran; el
 * Administrador registra al vendedor junto con su primera bodega.
 */
@Service
public class RegisterSellerService implements RegisterSellerUseCase {

    private final AuthorizationService authorizationService;
    private final UniqueUserDataValidator uniqueUserDataValidator;
    private final UserRepositoryPort userRepositoryPort;

    public RegisterSellerService(AuthorizationService authorizationService,
                                 UniqueUserDataValidator uniqueUserDataValidator,
                                 UserRepositoryPort userRepositoryPort) {
        this.authorizationService = authorizationService;
        this.uniqueUserDataValidator = uniqueUserDataValidator;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Transactional
    @Override
    public Seller execute(EntityId actorId, String identityDocument, String fullName,
                          String email, String firstWarehouseName) {
        authorizationService.requireRole(actorId, Role.ADMINISTRATOR);
        uniqueUserDataValidator.validate(identityDocument, email);
        EntityId sellerId = EntityId.next();
        Warehouse firstWarehouse = Warehouse.sellerWarehouse(EntityId.next(), firstWarehouseName, sellerId);
        Seller seller = new Seller(sellerId, identityDocument, fullName, email, firstWarehouse);
        return (Seller) userRepositoryPort.save(seller);
    }
}

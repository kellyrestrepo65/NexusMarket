package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.user.Seller;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * Dominio 3 y flujo 6.1 "Incorporacion": los vendedores no se autorregistran; el
 * Administrador registra al vendedor junto con su primera bodega.
 */
public interface RegisterSellerUseCase {

    Seller execute(EntityId actorId, String identityDocument, String fullName, String email, String firstWarehouseName);
}

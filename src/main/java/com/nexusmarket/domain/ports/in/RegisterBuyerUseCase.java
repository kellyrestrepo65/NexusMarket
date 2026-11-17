package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.user.Buyer;
import com.nexusmarket.domain.valueobjects.Address;

/**
 * Proceso "Registro de compradores". Es la unica operacion que se ejecuta
 * sin un usuario identificado, porque el usuario todavia no existe.
 */
public interface RegisterBuyerUseCase {

    Buyer execute(String identityDocument, String fullName, String email, Address mainAddress);
}

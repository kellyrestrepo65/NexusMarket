package com.nexusmarket.domain.services.user;

import com.nexusmarket.domain.exceptions.DuplicateDataException;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import org.springframework.stereotype.Component;

/**
 * Seccion 11: el documento de identidad y el correo son unicos en la plataforma.
 */
@Component
public class UniqueUserDataValidator {

    private final UserRepositoryPort userRepositoryPort;

    public UniqueUserDataValidator(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    public void validate(String identityDocument, String email) {
        if (identityDocument != null && userRepositoryPort.existsByIdentityDocument(identityDocument.trim())) {
            throw new DuplicateDataException("A user with identity document " + identityDocument + " already exists");
        }
        if (email != null && userRepositoryPort.existsByEmail(email)) {
            throw new DuplicateDataException("A user with email " + email + " already exists");
        }
    }
}

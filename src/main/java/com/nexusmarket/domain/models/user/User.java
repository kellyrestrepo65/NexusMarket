package com.nexusmarket.domain.models.user;

import com.nexusmarket.domain.exceptions.InvalidArgumentException;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.UserStatus;
import com.nexusmarket.domain.enums.Role;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Clase base de todos los usuarios del sistema. Es abstracta porque un
 * "User" solo no existe en la practica, siempre es alguna de sus
 * subclases (Buyer, Seller, LogisticsOperator, Administrator, Supervisor).
 * El rol se fija en el constructor de cada subclase y no tiene setter,
 * para que un usuario no pueda cambiar de rol despues de creado.
 */
public abstract class User {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private final EntityId id;
    private final String identityDocument;
    private String fullName;
    private String email;
    private final Role role;
    private UserStatus status;

    protected User(EntityId id, String identityDocument, String fullName, String email, Role role) {
        if (id == null) {
            throw new InvalidArgumentException("The user id is required");
        }
        if (identityDocument == null || identityDocument.isBlank()) {
            throw new InvalidArgumentException("The identity document is required");
        }
        if (role == null) {
            throw new InvalidArgumentException("The user role is required");
        }
        this.id = id;
        this.identityDocument = identityDocument.trim();
        this.role = role;
        this.status = UserStatus.ACTIVE;
        changeFullName(fullName);
        changeEmail(email);
    }

    public final void changeFullName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new InvalidArgumentException("The full name cannot be empty");
        }
        this.fullName = newName;
    }

    public final void changeEmail(String newEmail) {
        if (newEmail == null || !EMAIL_PATTERN.matcher(newEmail).matches()) {
            throw new InvalidArgumentException("The email does not have a valid format");
        }
        this.email = newEmail;
    }

    /**
     * Cada rol solo deberia poder operar dentro de su propio alcance.
     * Las subclases sobreescriben este metodo cuando su alcance es distinto
     * al de por defecto (operar solo sobre si mismo).
     */
    public boolean canOperateOn(User other) {
        return this.id.equals(other.id);
    }

    public void block() {
        if (this.status == UserStatus.BLOCKED) {
            return;
        }
        this.status = UserStatus.BLOCKED;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public EntityId getId() {
        return id;
    }

    public String getIdentityDocument() {
        return identityDocument;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

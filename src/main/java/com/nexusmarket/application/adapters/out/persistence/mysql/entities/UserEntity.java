package com.nexusmarket.application.adapters.out.persistence.mysql.entities;

import com.nexusmarket.domain.models.user.Buyer;
import com.nexusmarket.domain.models.user.User;

import com.nexusmarket.domain.enums.CommercialStatus;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.enums.UserStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Todos los roles viven en una sola tabla; la columna role indica que subclase
 * de User se reconstruye. Las columnas exclusivas del comprador quedan en null
 * para los demas roles.
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private String identityDocument;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    private CommercialStatus commercialStatus;

    @Embedded
    private AddressEmbeddable mainAddress;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "buyer_additional_addresses", joinColumns = @JoinColumn(name = "user_id"))
    private List<AddressEmbeddable> additionalAddresses = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getIdentityDocument() { return identityDocument; }
    public void setIdentityDocument(String identityDocument) { this.identityDocument = identityDocument; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public CommercialStatus getCommercialStatus() { return commercialStatus; }
    public void setCommercialStatus(CommercialStatus commercialStatus) { this.commercialStatus = commercialStatus; }
    public AddressEmbeddable getMainAddress() { return mainAddress; }
    public void setMainAddress(AddressEmbeddable mainAddress) { this.mainAddress = mainAddress; }
    public List<AddressEmbeddable> getAdditionalAddresses() { return additionalAddresses; }
    public void setAdditionalAddresses(List<AddressEmbeddable> additionalAddresses) { this.additionalAddresses = additionalAddresses; }
}

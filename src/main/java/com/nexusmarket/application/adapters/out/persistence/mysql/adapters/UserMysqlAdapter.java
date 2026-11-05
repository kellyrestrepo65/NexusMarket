package com.nexusmarket.application.adapters.out.persistence.mysql.adapters;

import com.nexusmarket.domain.models.inventory.Warehouse;
import com.nexusmarket.domain.models.user.Administrator;
import com.nexusmarket.domain.models.user.Buyer;
import com.nexusmarket.domain.models.user.LogisticsOperator;
import com.nexusmarket.domain.models.user.Seller;
import com.nexusmarket.domain.models.user.Supervisor;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.Address;
import com.nexusmarket.domain.enums.CommercialStatus;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.enums.UserStatus;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.AddressEmbeddable;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.UserEntity;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.WarehouseEntity;
import com.nexusmarket.application.adapters.out.persistence.mysql.repositories.UserJpaRepository;
import com.nexusmarket.application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Los usuarios se reconstruyen con sus constructores y metodos de estado del
 * dominio, asi un usuario guardado pasa siempre las mismas validaciones que uno
 * nuevo. Un vendedor se reconstruye a partir de sus bodegas (la primera es la
 * inicial).
 */
@Component
public class UserMysqlAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;
    private final WarehouseJpaRepository warehouseJpaRepository;

    public UserMysqlAdapter(UserJpaRepository userJpaRepository, WarehouseJpaRepository warehouseJpaRepository) {
        this.userJpaRepository = userJpaRepository;
        this.warehouseJpaRepository = warehouseJpaRepository;
    }

    @Override
    public User save(User user) {
        if (user instanceof Seller seller) {
            for (Warehouse warehouse : seller.getWarehouses()) {
                warehouseJpaRepository.save(WarehouseEntity.from(warehouse));
            }
        }
        userJpaRepository.save(toEntity(user));
        return user;
    }

    @Override
    public Optional<User> findById(EntityId id) {
        return userJpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmailIgnoreCase(email.trim());
    }

    @Override
    public boolean existsByIdentityDocument(String identityDocument) {
        return userJpaRepository.existsByIdentityDocument(identityDocument);
    }

    @Override
    public boolean existsByRole(Role role) {
        return userJpaRepository.existsByRole(role);
    }

    private UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId().value());
        entity.setIdentityDocument(user.getIdentityDocument());
        entity.setFullName(user.getFullName());
        entity.setEmail(user.getEmail());
        entity.setRole(user.getRole());
        entity.setStatus(user.getStatus());
        if (user instanceof Buyer buyer) {
            entity.setCommercialStatus(buyer.getCommercialStatus());
            entity.setMainAddress(AddressEmbeddable.from(buyer.getMainAddress()));
            entity.setAdditionalAddresses(buyer.getAdditionalAddresses().stream()
                    .map(AddressEmbeddable::from).toList());
        }
        return entity;
    }

    private User toDomain(UserEntity entity) {
        EntityId id = EntityId.of(entity.getId());
        String doc = entity.getIdentityDocument();
        String name = entity.getFullName();
        String email = entity.getEmail();
        User user = switch (entity.getRole()) {
            case BUYER -> toBuyer(entity, id);
            case SELLER -> toSeller(id, doc, name, email);
            case ADMINISTRATOR -> new Administrator(id, doc, name, email);
            case LOGISTICS_OPERATOR -> new LogisticsOperator(id, doc, name, email);
            case SUPERVISOR -> new Supervisor(id, doc, name, email);
        };
        if (entity.getStatus() == UserStatus.BLOCKED) {
            user.block();
        }
        return user;
    }

    private Buyer toBuyer(UserEntity entity, EntityId id) {
        Address main = entity.getMainAddress().toDomain();
        Buyer buyer = new Buyer(id, entity.getIdentityDocument(), entity.getFullName(), entity.getEmail(), main);
        for (AddressEmbeddable additional : entity.getAdditionalAddresses()) {
            buyer.addAdditionalAddress(additional.toDomain());
        }
        if (entity.getCommercialStatus() == CommercialStatus.RESTRICTED) {
            buyer.restrict();
        }
        return buyer;
    }

    private Seller toSeller(EntityId id, String doc, String name, String email) {
        List<Warehouse> warehouses = warehouseJpaRepository.findByOwnerIdOrderByIdAsc(id.value()).stream()
                .map(WarehouseEntity::toDomain).toList();
        Seller seller = new Seller(id, doc, name, email, warehouses.get(0));
        warehouses.stream().skip(1).forEach(seller::addWarehouse);
        return seller;
    }
}

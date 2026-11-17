package com.nexusmarket.infrastructure.config;

import com.nexusmarket.domain.models.user.Administrator;
import com.nexusmarket.domain.models.user.LogisticsOperator;
import com.nexusmarket.domain.models.user.Supervisor;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.application.adapters.out.persistence.mongodb.documents.ProductDocument;
import com.nexusmarket.application.adapters.out.persistence.mongodb.repositories.ProductMongoRepository;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.InventoryEntity;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.OrderEntity;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.ShoppingCartEntity;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.UserEntity;
import com.nexusmarket.application.adapters.out.persistence.mysql.entities.WarehouseEntity;
import com.nexusmarket.application.adapters.out.persistence.mysql.repositories.InventoryJpaRepository;
import com.nexusmarket.application.adapters.out.persistence.mysql.repositories.OrderJpaRepository;
import com.nexusmarket.application.adapters.out.persistence.mysql.repositories.ShoppingCartJpaRepository;
import com.nexusmarket.application.adapters.out.persistence.mysql.repositories.UserJpaRepository;
import com.nexusmarket.application.adapters.out.persistence.mysql.repositories.WarehouseJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 1. EntityId.next() es una secuencia en memoria, asi que despues de reiniciar
 * debe continuar despues del id mas alto ya guardado en cualquier base de datos.
 * 2. La especificacion no dice quien registra al personal interno
 * (Administrador, Operador Logistico, Supervisor); se crea uno de cada uno desde
 * la configuracion la primera vez que arranca la plataforma.
 */
@Component
@org.springframework.core.annotation.Order(1)
public class StartupInitializer implements ApplicationRunner {

    private final UserJpaRepository userJpaRepository;
    private final WarehouseJpaRepository warehouseJpaRepository;
    private final InventoryJpaRepository inventoryJpaRepository;
    private final ShoppingCartJpaRepository shoppingCartJpaRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final ProductMongoRepository productMongoRepository;
    private final UserRepositoryPort userRepositoryPort;
    private final String adminDocument;
    private final String adminEmail;
    private final String logisticsDocument;
    private final String logisticsEmail;
    private final String supervisorDocument;
    private final String supervisorEmail;

    public StartupInitializer(UserJpaRepository userJpaRepository,
                              WarehouseJpaRepository warehouseJpaRepository,
                              InventoryJpaRepository inventoryJpaRepository,
                              ShoppingCartJpaRepository shoppingCartJpaRepository,
                              OrderJpaRepository orderJpaRepository,
                              ProductMongoRepository productMongoRepository,
                              UserRepositoryPort userRepositoryPort,
                              @Value("${nexusmarket.seed.admin.document}") String adminDocument,
                              @Value("${nexusmarket.seed.admin.email}") String adminEmail,
                              @Value("${nexusmarket.seed.logistics.document}") String logisticsDocument,
                              @Value("${nexusmarket.seed.logistics.email}") String logisticsEmail,
                              @Value("${nexusmarket.seed.supervisor.document}") String supervisorDocument,
                              @Value("${nexusmarket.seed.supervisor.email}") String supervisorEmail) {
        this.userJpaRepository = userJpaRepository;
        this.warehouseJpaRepository = warehouseJpaRepository;
        this.inventoryJpaRepository = inventoryJpaRepository;
        this.shoppingCartJpaRepository = shoppingCartJpaRepository;
        this.orderJpaRepository = orderJpaRepository;
        this.productMongoRepository = productMongoRepository;
        this.userRepositoryPort = userRepositoryPort;
        this.adminDocument = adminDocument;
        this.adminEmail = adminEmail;
        this.logisticsDocument = logisticsDocument;
        this.logisticsEmail = logisticsEmail;
        this.supervisorDocument = supervisorDocument;
        this.supervisorEmail = supervisorEmail;
    }

    @Override
    public void run(ApplicationArguments args) {
        EntityId.advanceSequenceAfter(highestStoredId());
        seed(Role.ADMINISTRATOR, () -> new Administrator(EntityId.next(), adminDocument, "Administrador", adminEmail));
        seed(Role.LOGISTICS_OPERATOR, () -> new LogisticsOperator(EntityId.next(), logisticsDocument, "Operador Logistico", logisticsEmail));
        seed(Role.SUPERVISOR, () -> new Supervisor(EntityId.next(), supervisorDocument, "Supervisor", supervisorEmail));
    }

    private void seed(Role role, java.util.function.Supplier<User> factory) {
        if (!userRepositoryPort.existsByRole(role)) {
            userRepositoryPort.save(factory.get());
        }
    }

    private int highestStoredId() {
        IntStream variantIds = productMongoRepository.findAll().stream()
                .flatMap(product -> product.getVariants().stream())
                .mapToInt(ProductDocument.VariantDocument::id);
        return Stream.of(
                        ids(userJpaRepository.findAll().stream(), UserEntity::getId),
                        ids(warehouseJpaRepository.findAll().stream(), WarehouseEntity::getId),
                        ids(inventoryJpaRepository.findAll().stream(), InventoryEntity::getId),
                        ids(shoppingCartJpaRepository.findAll().stream(), ShoppingCartEntity::getId),
                        ids(orderJpaRepository.findAll().stream(), OrderEntity::getId),
                        ids(productMongoRepository.findAll().stream(), ProductDocument::getId),
                        variantIds)
                .flatMapToInt(Function.identity())
                .max()
                .orElse(0);
    }

    private static <T> IntStream ids(Stream<T> rows, Function<T, Integer> id) {
        return rows.mapToInt(id::apply);
    }
}

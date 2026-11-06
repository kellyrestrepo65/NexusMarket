package com.nexusmarket.infrastructure.config;

import com.nexusmarket.domain.enums.ProductType;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.user.Seller;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.ports.in.RegisterBuyerUseCase;
import com.nexusmarket.domain.ports.in.RegisterProductUseCase;
import com.nexusmarket.domain.ports.in.RegisterSellerUseCase;
import com.nexusmarket.domain.ports.in.ReceiveStockUseCase;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import com.nexusmarket.domain.valueobjects.Address;
import com.nexusmarket.domain.valueobjects.EntityId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Carga datos de prueba (vendedores, productos, stock y un comprador) usando los casos de uso.
 * Solo corre si no hay vendedores registrados; se desactiva con DEMO_DATA=false.
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "nexusmarket.demo-data", havingValue = "true", matchIfMissing = false)
public class DemoDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final UserRepositoryPort userRepositoryPort;
    private final WarehouseRepositoryPort warehouseRepositoryPort;
    private final RegisterSellerUseCase registerSellerUseCase;
    private final RegisterProductUseCase registerProductUseCase;
    private final ReceiveStockUseCase receiveStockUseCase;
    private final RegisterBuyerUseCase registerBuyerUseCase;

    public DemoDataInitializer(UserRepositoryPort userRepositoryPort, WarehouseRepositoryPort warehouseRepositoryPort,
                               RegisterSellerUseCase registerSellerUseCase, RegisterProductUseCase registerProductUseCase,
                               ReceiveStockUseCase receiveStockUseCase, RegisterBuyerUseCase registerBuyerUseCase) {
        this.userRepositoryPort = userRepositoryPort;
        this.warehouseRepositoryPort = warehouseRepositoryPort;
        this.registerSellerUseCase = registerSellerUseCase;
        this.registerProductUseCase = registerProductUseCase;
        this.receiveStockUseCase = receiveStockUseCase;
        this.registerBuyerUseCase = registerBuyerUseCase;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepositoryPort.existsByRole(Role.SELLER)) {
            return;
        }
        EntityId adminId = userRepositoryPort.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMINISTRATOR).map(User::getId).findFirst().orElse(null);
        if (adminId == null) {
            return;
        }

        Seller tecno = seller(adminId, "2000000001", "TecnoStore", "tecno@demo.com");
        Seller sonido = seller(adminId, "2000000002", "SonidoPro", "sonido@demo.com");
        Seller soft = seller(adminId, "2000000003", "SoftHouse", "soft@demo.com");
        Seller edu = seller(adminId, "2000000004", "EduOnline", "edu@demo.com");
        Seller hogar = seller(adminId, "2000000005", "HogarMax", "hogar@demo.com");

        physical(tecno, "Laptop 14\"", "3200000", Map.of("Color", "Gris", "RAM", "16GB"), 8);
        physical(tecno, "Monitor 27\"", "1150000", Map.of("Resolucion", "QHD"), 2);
        physical(tecno, "Mouse ergonomico", "95000", Map.of("Color", "Negro"), 25);
        physical(tecno, "Teclado mecanico", "280000", Map.of("Switch", "Rojo"), 3);
        physical(sonido, "Audifonos inalambricos", "420000", Map.of("Color", "Blanco"), 12);
        physical(sonido, "Parlante bluetooth", "310000", Map.of("Color", "Azul"), 0);
        physical(hogar, "Silla de oficina", "690000", Map.of("Color", "Negro"), 5);
        physical(hogar, "Lampara de escritorio", "120000", Map.of(), 15);
        digital(soft, "Licencia antivirus", "89000", Map.of("Duracion", "1 ano"));
        digital(soft, "Suite de oficina", "250000", Map.of("Licencia", "Personal"));
        digital(edu, "Curso de Java", "150000", Map.of("Nivel", "Intermedio"));
        digital(edu, "Curso de Spring Boot", "180000", Map.of("Nivel", "Avanzado"));

        EntityId buyerId = registerBuyerUseCase.execute("3000000001", "Laura Gomez", "laura@demo.com",
                new Address("Calle 10 # 20-30", "Medellin", "Antioquia", "Colombia", "050001")).getId();

        log.info("Datos de prueba cargados. Comprador: {} | Vendedores: {}, {}, {}, {}, {}", buyerId,
                tecno.getId(), sonido.getId(), soft.getId(), edu.getId(), hogar.getId());
    }

    private Seller seller(EntityId adminId, String document, String name, String email) {
        return registerSellerUseCase.execute(adminId, document, name, email, "Bodega " + name);
    }

    private void physical(Seller seller, String name, String price, Map<String, String> variants, int stock) {
        Product product = registerProductUseCase.execute(seller.getId(), name, new BigDecimal(price),
                ProductType.PHYSICAL, variants);
        if (stock > 0) {
            EntityId warehouseId = warehouseRepositoryPort.findByOwnerId(seller.getId()).get(0).getId();
            receiveStockUseCase.execute(seller.getId(), product.getId(), warehouseId, stock);
        }
    }

    private void digital(Seller seller, String name, String price, Map<String, String> variants) {
        registerProductUseCase.execute(seller.getId(), name, new BigDecimal(price), ProductType.DIGITAL, variants);
    }
}

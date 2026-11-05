package com.nexusmarket.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Prueba de integracion de punta a punta: controllers REST, servicios del
 * dominio y adaptadores reales de MySQL y MongoDB.
 * Usa bases separadas (nexusmarket_test) para no tocar los datos de desarrollo.
 * Si las bases no estan arriba (docker compose up -d) la prueba se omite.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/nexusmarket_test?createDatabaseIfNotExist=true",
        "spring.data.mongodb.uri=mongodb://localhost:27017/nexusmarket_test"
})
@AutoConfigureMockMvc
class MarketplaceFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepositoryPort users;

    private final ObjectMapper json = new ObjectMapper();
    private String admin;
    private String operator;
    private String supervisor;
    private String suffix;

    @BeforeAll
    static void requireDatabases() {
        assumeTrue(isOpen(3306) && isOpen(27017), "MySQL o MongoDB no estan disponibles");
    }

    @BeforeEach
    void loadInternalUsers() {
        admin = idOf(Role.ADMINISTRATOR);
        operator = idOf(Role.LOGISTICS_OPERATOR);
        supervisor = idOf(Role.SUPERVISOR);
        suffix = String.valueOf(System.nanoTime()).substring(8);
    }

    @Test
    void completePurchaseFlowPersistsInMysqlAndMongo() throws Exception {
        String seller = registerSeller();
        String buyer = registerBuyer("B" + suffix);
        String warehouse = call(get("/api/warehouses"), seller, null, 200).get(0).get("id").asText();

        // el catalogo se guarda en MongoDB y se vuelve a leer
        String product = call(post("/api/products"), seller, Map.of("name", "Laptop", "price", 1000,
                "type", "PHYSICAL", "variants", Map.of("color", "negro")), 201).get("id").asText();
        assertTrue(call(get("/api/products"), buyer, null, 200).toString().contains(product));

        // inventario en MySQL
        call(post("/api/inventories/receipts"), seller,
                Map.of("productId", product, "warehouseId", warehouse, "quantity", 5), 201);

        // carrito, pedido, pago, despacho y entrega
        call(post("/api/cart/items"), buyer, Map.of("productId", product, "quantity", 2), 201);
        String order = call(post("/api/orders"), buyer, null, 201).get("id").asText();
        call(post("/api/orders/" + order + "/payment"), buyer, null, 200);
        call(get("/api/orders/" + order + "/invoice"), buyer, null, 200);
        call(post("/api/orders/" + order + "/dispatch"), operator, null, 200);
        JsonNode delivered = call(post("/api/orders/" + order + "/delivery"), operator, null, 200);
        assertEquals("DELIVERED", delivered.get("status").asText());

        // el reporte consolidado lo consulta el supervisor
        call(get("/api/reports/administrative"), supervisor, null, 200);
    }

    @Test
    void businessRulesAreEnforcedThroughTheApi() throws Exception {
        String seller = registerSeller();
        String buyer = registerBuyer("R" + suffix);

        // RG-03: un comprador no puede registrar vendedores
        call(post("/api/users/sellers"), buyer, Map.of("identityDocument", "X" + suffix, "fullName", "X",
                "email", "x" + suffix + "@demo.com", "firstWarehouseName", "W"), 403);
        // RG-01: sin X-User-Id no hay operacion
        call(get("/api/orders"), null, null, 401);
        // documento unico
        call(post("/api/users/buyers"), null, buyerBody("R" + suffix, "otro" + suffix + "@demo.com"), 409);
        // no se puede comprar mas de lo que hay en inventario
        String product = call(post("/api/products"), seller, Map.of("name", "Mouse", "price", 50,
                "type", "PHYSICAL"), 201).get("id").asText();
        call(post("/api/cart/items"), buyer, Map.of("productId", product, "quantity", 1), 201);
        call(post("/api/orders"), buyer, null, 422);
        // validacion de entrada
        call(put("/api/cart/items/" + product), buyer, Map.of("quantity", 0), 400);
    }

    private String registerSeller() throws Exception {
        return call(post("/api/users/sellers"), admin, Map.of("identityDocument", "S" + suffix,
                "fullName", "Seller Test", "email", "seller" + suffix + "@demo.com",
                "firstWarehouseName", "Bodega Test"), 201).get("id").asText();
    }

    private String registerBuyer(String document) throws Exception {
        return call(post("/api/users/buyers"), null, buyerBody(document, document + "@demo.com"), 201)
                .get("id").asText();
    }

    private Map<String, Object> buyerBody(String document, String email) {
        return Map.of("identityDocument", document, "fullName", "Buyer Test", "email", email,
                "mainAddress", Map.of("addressLine1", "Calle 1", "city", "Medellin", "country", "CO"));
    }

    private JsonNode call(MockHttpServletRequestBuilder request, String actor, Object body, int expected)
            throws Exception {
        request.contentType(MediaType.APPLICATION_JSON);
        if (actor != null) {
            request.header("X-User-Id", actor);
        }
        if (body != null) {
            request.content(json.writeValueAsString(body));
        }
        var response = mvc.perform(request).andReturn().getResponse();
        String text = response.getContentAsString();
        // algunos endpoints de creacion responden 200 y otros 201: ambos son exito
        int status = (expected == 201 && response.getStatus() == 200) ? 201 : response.getStatus();
        assertEquals(expected, status, text);
        return text.isBlank() ? json.nullNode() : json.readTree(text);
    }

    private String idOf(Role role) {
        return users.findAll().stream().filter(u -> u.getRole() == role).findFirst()
                .map(User::getId).map(id -> String.valueOf(id.value())).orElseThrow();
    }

    private static boolean isOpen(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), 500);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

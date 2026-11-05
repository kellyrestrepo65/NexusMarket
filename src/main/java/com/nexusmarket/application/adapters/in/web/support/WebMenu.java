package com.nexusmarket.application.adapters.in.web.support;

import java.util.List;
import java.util.Map;

/**
 * Menu de cada rol (matriz de responsabilidades) y nombres en español de los
 * enums que se muestran en las vistas. Solo es presentacion: los permisos los
 * sigue validando el dominio en cada caso de uso.
 */
public final class WebMenu {

    public record Item(String path, String title) {
    }

    private static final Item CATALOG = new Item("/catalog", "Catalogo");
    private static final Item CART = new Item("/cart", "Carrito");
    private static final Item ORDERS = new Item("/orders", "Ordenes");
    private static final Item RETURNS = new Item("/returns", "Devoluciones");
    private static final Item INVOICES = new Item("/invoices", "Facturas");
    private static final Item SHIPMENTS = new Item("/shipments", "Envios");
    private static final Item PRODUCTS = new Item("/products", "Mis productos");
    private static final Item INVENTORY = new Item("/inventory", "Inventario");
    private static final Item WAREHOUSES = new Item("/warehouses", "Bodegas");
    private static final Item USERS = new Item("/users", "Usuarios");
    private static final Item SELLERS = new Item("/sellers/new", "Registrar vendedor");
    private static final Item REPORT = new Item("/report", "Reporte");
    private static final Item PROFILE = new Item("/profile", "Mi perfil");

    private static final Map<String, List<Item>> BY_ROLE = Map.of(
            "BUYER", List.of(CATALOG, CART, ORDERS, RETURNS, INVOICES, SHIPMENTS, PROFILE),
            "SELLER", List.of(PRODUCTS, INVENTORY, WAREHOUSES, ORDERS, PROFILE),
            "LOGISTICS_OPERATOR", List.of(ORDERS, SHIPMENTS, INVENTORY, WAREHOUSES, PROFILE),
            "ADMINISTRATOR", List.of(USERS, SELLERS, WAREHOUSES, RETURNS, INVOICES, SHIPMENTS, REPORT, PROFILE),
            "SUPERVISOR", List.of(REPORT, USERS, WAREHOUSES, INVENTORY, ORDERS, INVOICES, SHIPMENTS, RETURNS, PROFILE));

    public static final Map<String, String> LABELS = Map.ofEntries(
            Map.entry("BUYER", "Comprador"), Map.entry("SELLER", "Vendedor"),
            Map.entry("LOGISTICS_OPERATOR", "Operador Logistico"), Map.entry("ADMINISTRATOR", "Administrador"),
            Map.entry("SUPERVISOR", "Supervisor"),
            Map.entry("ACTIVE", "Activo"), Map.entry("BLOCKED", "Bloqueado"), Map.entry("INACTIVE", "Inactivo"),
            Map.entry("ENABLED", "Habilitado"), Map.entry("RESTRICTED", "Restringido"),
            Map.entry("PUBLISHED", "Publicado"), Map.entry("SUSPENDED", "Suspendido"),
            Map.entry("DISCONTINUED", "Descontinuado"),
            Map.entry("PHYSICAL", "Fisico"), Map.entry("DIGITAL", "Digital"), Map.entry("MARKETPLACE", "Marketplace"),
            Map.entry("CART", "Carrito"), Map.entry("PENDING_PAYMENT", "Pendiente de pago"), Map.entry("PAID", "Pagada"),
            Map.entry("DISPATCHED", "Despachada"), Map.entry("DELIVERED", "Entregada"),
            Map.entry("IN_PREPARATION", "En preparacion"), Map.entry("IN_TRANSIT", "En transito"),
            Map.entry("REQUESTED", "Solicitada"), Map.entry("APPROVED", "Aprobada"), Map.entry("REJECTED", "Rechazada"),
            Map.entry("PENDING", "Pendiente"), Map.entry("PROCESSED", "Procesado"));

    private WebMenu() {
    }

    public static List<Item> forRole(String role) {
        return BY_ROLE.getOrDefault(role, List.of(PROFILE));
    }

    // Pagina de inicio de cada rol: la primera opcion de su menu
    public static String homeOf(String role) {
        return forRole(role).get(0).path();
    }
}

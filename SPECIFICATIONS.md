# Especificación de NexusMarket

> Documento generado a partir del modelo de dominio simplificado
> (`src/main/java/com/nexusmarket/domain` y `src/main/java/com/nexusmarket/valueObjects`).
> Refleja el modelo tal como quedó tras la simplificación: entidades anémicas
> (POJOs Lombok con anotaciones JPA) salvo dos invariantes de comportamiento que
> viven en `InventoryItem` y `Order`.

## 1. Descripción general

NexusMarket es un **marketplace de e-commerce multi-vendedor**: distintos
vendedores (`SellerProfile`) publican productos (`Product`) y los compradores
(`BuyerProfile`) concretan compras (`Order` / `OrderItem`). Cada producto lista
sus variantes vendibles como simples etiquetas de texto (`Product.variants`),
sin entidad propia.

El sistema modela stock **multi-almacén**: un producto puede tener inventario
(`InventoryItem`) en distintos almacenes (`Warehouse`), propios de la plataforma
(`MARKETPLACE`) o de un vendedor (`SELLER`). El stock nunca puede quedar
negativo. Toda compra puede generar una factura (`BillingInvoice`) asociada uno a
uno con la orden, y existe un registro simple de devoluciones (`ReturnRequest`).
La identidad de todos los actores (comprador, vendedor, operador logístico,
administrador, supervisor) se modela con una única entidad `User` con un rol
(`UserRole`); `BuyerProfile` y `SellerProfile` son perfiles asociados uno a uno.

## 2. Stack tecnológico

Extraído de `pom.xml`:

- **Java 17** (`<java.version>17</java.version>`)
- **Spring Boot 4.1.0** (`spring-boot-starter-parent`)
- **Spring Data JPA** (`spring-boot-starter-data-jpa`) — persistencia relacional de las entidades de dominio (anotaciones `jakarta.persistence.*`)
- **Spring Data MongoDB** (`spring-boot-starter-data-mongodb`) — dependencia presente en el POM; ninguna clase del dominio actual la usa
- **Spring Security** (`spring-boot-starter-security`)
- **Spring Web** (`spring-boot-starter-web`)
- **MySQL Connector/J** (`com.mysql:mysql-connector-j`, scope `runtime`)
- **Lombok** (`org.projectlombok:lombok`, `optional`) — `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@ToString`
- **spring-boot-starter-test** (scope `test`) — presente en el POM; sin pruebas automatizadas en esta entrega (ver sección 6)

`application.properties` sólo define `spring.application.name`: no hay datasource
configurado, por lo que las anotaciones de esquema (`unique`, `nullable`,
`@CollectionTable`) son pistas DDL y no se materializa ningún esquema en runtime.

## 3. Modelo de dominio

El dominio contiene **exactamente 10 entidades**. Todo `id` es un `String`
generado (`GenerationType.IDENTITY`) y es el único identificador. Los importes
son `BigDecimal`.

```
User ──1:1── BuyerProfile ──1:N── Order ──1:N── OrderItem ──N:1── Product
 │                                  │                              │
 │                                  ├──1:1── BillingInvoice        └──N:1── SellerProfile
 │                                  └──1:N── ReturnRequest
 └──1:1── SellerProfile ──1:N── Warehouse ──1:N── InventoryItem ──N:1── Product
```

### User

Aggregate root de identidad. POJO anémico (`@Getter/@Setter` Lombok).

- `id: String`.
- `fullName: String` — columna `nullable = false`.
- `documentId: String` — columna `nullable = false`, `unique = true`.
- `email: String` — columna `nullable = false`, `unique = true`.
- `role: UserRole` — `@Enumerated(STRING)`, `nullable = false`.
- `status: UserStatus` — `@Enumerated(STRING)`, `nullable = false`; default `ACTIVE` vía `@Builder.Default`.

No tiene `password` ni constructores de dominio validados. Es el destino de
`BuyerProfile.user` y `SellerProfile.user`.

### BuyerProfile

Perfil de comprador, asociado uno a uno a un `User`.

- `id: String`.
- `mainAddress: String` — columna `nullable = false`.
- `additionalAddresses: List<String>` — `@ElementCollection`, tabla `buyer_additional_addresses` (columna `address`, longitud 500); default lista vacía.
- `commercialStatus: CommercialStatus` — `@Enumerated(STRING)`, `nullable = false`; default `ACTIVE` vía `@Builder.Default`.
- `user: User` — `@OneToOne(optional = false)`, columna única `user_id` (`nullable = false`).

### SellerProfile

Perfil de vendedor, asociado uno a uno a un `User`. POJO anémico.

- `id: String`.
- `businessName: String`.
- `taxIdentification: String`.
- `user: User` — `@OneToOne`, columna única `user_id` (`nullable = false`).
- `warehouses: List<Warehouse>` — `@OneToMany` (mappedBy `sellerProfile`), sin cascada.
- `products: List<Product>` — `@OneToMany` (mappedBy `sellerProfile`), sin cascada.

### Product

Aggregate root de catálogo. Un producto pertenece a un único vendedor.

- `id: String`.
- `name: String` — columna `nullable = false`.
- `description: String` — columna de hasta 2000 caracteres.
- `price: BigDecimal` — `precision = 19, scale = 2`, `nullable = false`.
- `type: ProductType` — `@Enumerated(STRING)`, `nullable = false`.
- `status: ProductStatus` — `@Enumerated(STRING)`, `nullable = false`; default `PUBLISHED` vía `@Builder.Default`.
- `sellerProfile: SellerProfile` — **`@ManyToOne(optional = false)`**, `seller_profile_id nullable = false`.
- `variants: List<String>` — `@ElementCollection`, tabla `product_variants` (columna `variant`); default lista vacía.

No hay precio derivado ni método de recálculo.

### Warehouse

Almacén físico donde se guarda inventario. POJO anémico.

- `id: String`.
- `name: String`, `location: String`.
- `type: WarehouseType` — `@Enumerated(STRING)`, `nullable = false`.
- `sellerProfile: SellerProfile` — `@ManyToOne`, `nullable = true` **a propósito**: un almacén `MARKETPLACE` pertenece a la plataforma y no a un vendedor.

### InventoryItem

Registro de stock de un producto en un almacén. Mantiene la invariante de
inventario no negativo.

- `id: String`.
- `quantity: int` — columna `nullable = false`; **sin setter** (`@Setter(AccessLevel.NONE)`).
- `product: Product` — **`@ManyToOne(optional = false)`**, `product_id nullable = false`.
- `warehouse: Warehouse` — **`@ManyToOne(optional = false)`**, `warehouse_id nullable = false`.
- Restricción única compuesta a nivel de tabla: `(product_id, warehouse_id)`.

Método de dominio:
- `adjust(int delta)`: suma `delta` a `quantity` (positivo repone, negativo consume). Si el resultado sería `< 0` lanza `IllegalArgumentException` y `quantity` queda intacto. Es la **única** vía de mutación de `quantity`.

### Order

Aggregate root de la compra. Una orden `DELIVERED_FINALIZED` es inmutable.

- `id: String`.
- `status: OrderStatus` — `@Enumerated(STRING)`, `nullable = false`; default `CART` vía `@Builder.Default`.
- `totalAmount: BigDecimal` — `precision = 19, scale = 2`; campo plano almacenado, el dominio nunca lo calcula; default `BigDecimal.ZERO`.
- `buyerProfile: BuyerProfile` — **`@ManyToOne(optional = false)`**, `buyer_profile_id nullable = false`.
- `items: List<OrderItem>` — `@OneToMany` (mappedBy `order`, `cascade = ALL`, `orphanRemoval = true`); no expone getter Lombok.

Métodos de dominio (la clase **no** tiene `@Setter` a nivel de clase):
- `getItems()`: devuelve `Collections.unmodifiableList(items)`.
- `addItem(OrderItem)`: `assertNotFinalized()`; rechaza `null` con `IllegalArgumentException`; agrega el ítem.
- `updateStatus(OrderStatus)`: `assertNotFinalized()`; asigna el nuevo estado. Transicionar *hacia* `DELIVERED_FINALIZED` está permitido; toda mutación posterior se rechaza.
- `setTotalAmount(BigDecimal)`: `assertNotFinalized()`; asigna.
- `setBuyerProfile(BuyerProfile)`: `assertNotFinalized()`; asigna.
- `assertNotFinalized()` (privado): si `status == DELIVERED_FINALIZED` lanza `IllegalStateException`.

### OrderItem

Detalle congelado de una compra. **Inmutable**: sólo `@Getter`, sin setters.

- `id: String`.
- `quantity: int` — columna `nullable = false`.
- `unitPrice: BigDecimal` — `precision = 19, scale = 2`, `nullable = false`; snapshot del precio al momento de la compra, nunca se recalcula.
- `order: Order` — `@ManyToOne(optional = false)`, `order_id nullable = false`.
- `product: Product` — `@ManyToOne(optional = false)`, `product_id nullable = false`.

No hay `subtotal` ni ningún campo derivado.

### BillingInvoice

Factura de una orden. POJO anémico.

- `id: String`.
- `amount: BigDecimal` — `precision = 19, scale = 2`.
- `issuedAt: LocalDateTime`.
- `order: Order` — `@OneToOne(optional = false)`, columna única `order_id` (`nullable = false`).

No hay `invoiceNumber` ni `tax`.

### ReturnRequest

Registro simple de devolución sobre una orden. POJO anémico, sin métodos.

- `id: String`.
- `reason: String` — columna de hasta 1000 caracteres.
- `order: Order` — `@ManyToOne(optional = false)`, `order_id nullable = false`.

No hay `status` ni referencia a administrador.

## 4. Enumerados

El paquete `valueObjects` contiene **exactamente 7 enumerados**.

### UserRole
- `BUYER`, `SELLER`, `LOGISTICS_OPERATOR`, `ADMINISTRATOR`, `SUPERVISOR`.

### UserStatus
- `ACTIVE` (estado inicial por defecto), `BLOCKED`, `INACTIVE`.

### CommercialStatus
Estado comercial de un `BuyerProfile`.
- `ACTIVE` (default), `RESTRICTED`, `SUSPENDED`.

### WarehouseType
- `MARKETPLACE` — almacén propio de la plataforma.
- `SELLER` — almacén propio de un vendedor.

### ProductType
- `PHYSICAL`, `DIGITAL`.

### ProductStatus
- `PUBLISHED` (default), `SUSPENDED`, `DISCONTINUED`.

### OrderStatus
- `CART` (default), `PENDING_PAYMENT`, `PAID`, `DISPATCHED`, `DELIVERED_FINALIZED`.

Se eliminaron `InventoryStatus` y `ReturnStatus`: el estado de inventario dejó de
modelarse (sólo importa `quantity`) y las devoluciones ya no tienen ciclo de vida.

## 5. Invariantes y reglas de negocio

El modelo es deliberadamente anémico. Sólo se conservan **tres** invariantes,
todas en el límite del modelo:

1. **El stock nunca es negativo.** `InventoryItem.quantity` no tiene setter; la
   única mutación es `adjust(int delta)`, que lanza `IllegalArgumentException`
   (dejando `quantity` sin cambios) cuando el resultado sería `< 0`.
2. **Un pedido finalizado es inmutable.** Con `Order.status == DELIVERED_FINALIZED`,
   `addItem`, `updateStatus`, `setTotalAmount` y `setBuyerProfile` lanzan
   `IllegalStateException`. `getItems()` devuelve una lista no modificable, de modo
   que tampoco se puede mutar la colección por fuera de `addItem`.
3. **Los dueños obligatorios se declaran no opcionales en el mapeo JPA**
   (no con chequeos en constructores): `InventoryItem.product`,
   `InventoryItem.warehouse`, `Order.buyerProfile`, `Product.sellerProfile` son
   `@ManyToOne(optional = false)` con `@JoinColumn(nullable = false)`;
   `BuyerProfile.user` es `@OneToOne(optional = false)`.

Además:

- La unicidad de `User.email` y `User.documentId` se declara con `@Column(unique = true)` (pista DDL únicamente; no hay verificación en runtime sin datasource ni repositorio).
- La unicidad compuesta `(product_id, warehouse_id)` de `InventoryItem` se declara con `@UniqueConstraint`.
- Un `Warehouse` de tipo `MARKETPLACE` puede no tener `sellerProfile`.
- `OrderItem` es un snapshot inmutable: `unitPrice` se fija al construirlo y no cambia.
- `Order.totalAmount` es un campo plano almacenado; el dominio no lo calcula.
- **No existe ningún campo o método derivado**: no hay `subtotal`, `getFinalPrice`, `recalculateTotal`, `refreshStatus`, `priceAdjustment` ni equivalentes.

## 6. Fuera de alcance en esta entrega (diferido, no es un defecto)

Los siguientes puntos se dejaron **explícitamente diferidos** a una entrega
posterior y no deben tratarse como huecos del modelo:

- Enforcement real de unicidad de `email` / `documentId` (hoy sólo columna `unique = true`; la búsqueda previa corresponde a un servicio futuro).
- Autorización del registro de vendedores por parte de un Administrador.
- La matriz de responsabilidades de autorización por rol (`UserRole`).
- Estado de inventario dañado y cualquier flujo de reserva / liberación de stock.
- Cálculo del total de una orden a partir de sus ítems.
- Numeración de facturas y cálculo de impuestos.
- Pruebas unitarias automatizadas (pospuestas de forma intencional para esta entrega).

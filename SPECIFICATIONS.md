# Especificación de NexusMarket

> Documento generado retroactivamente a partir del modelo de dominio ya implementado
> (`src/main/java/com/nexusmarket/domain` y `src/main/java/com/nexusmarket/valueObjects`).
> Su objetivo es dejar por escrito la especificación que debería haberse entregado
> antes de escribir el código, tal como puede reconstruirse observando las clases,
> sus constructores, sus setters validados y sus anotaciones JPA.

## 1. Descripción general

NexusMarket es un **marketplace de e-commerce multi-vendedor**: una plataforma donde
distintos vendedores (`SellerProfile`) publican productos (`Product`) con una o más
variantes vendibles (`ProductVariant`, con SKU propio y ajuste de precio sobre el
precio base del producto), y compradores (`BuyerProfile`) los agregan a un carrito
(`Cart`/`CartItem`) antes de concretar una compra (`Order`/`OrderItem`).

El sistema modela stock **multi-almacén**: cada variante de producto puede tener
inventario (`InventoryItem`) en distintos almacenes (`Warehouse`), que pueden ser
propios de la plataforma (`MARKETPLACE`) o de un vendedor concreto (`SELLER`), con
reserva y liberación de cantidades como operaciones explícitas de dominio. Toda
compra genera una factura (`BillingInvoice`) asociada uno a uno con la orden, y
existe un flujo de devoluciones (`ReturnRequest`) gestionado por un administrador.
La identidad de todos los actores (comprador, vendedor, operador logístico,
administrador, supervisor) se modela con una única entidad `User` con un rol
(`UserRole`), de la cual `BuyerProfile` y `SellerProfile` son perfiles asociados
uno a uno.

## 2. Stack tecnológico

Extraído de `pom.xml`:

- **Java 17** (`<java.version>17</java.version>`)
- **Spring Boot 4.1.0** (`spring-boot-starter-parent`)
- **Spring Data JPA** (`spring-boot-starter-data-jpa`) — persistencia relacional de las entidades de dominio (anotaciones `jakarta.persistence.*`)
- **Spring Data MongoDB** (`spring-boot-starter-data-mongodb`) — dependencia presente en el POM; ninguna clase del dominio actual la usa (no hay documentos Mongo en `domain`)
- **Spring Security** (`spring-boot-starter-security`)
- **Spring Web** (`spring-boot-starter-web`)
- **MySQL Connector/J** (`com.mysql:mysql-connector-j`, scope `runtime`) — driver de base de datos relacional
- **Lombok** (`org.projectlombok:lombok`, `optional`) — usado en todas las entidades para `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@ToString`
- **spring-boot-starter-test** (scope `test`) — presente en el POM pero, según el alcance de esta entrega, sin pruebas automatizadas escritas todavía (ver sección 6)

## 3. Modelo de dominio

### User

Aggregate root de identidad; representa a cualquier usuario del marketplace
(comprador, vendedor, operador logístico, administrador o supervisor).

- `id: String` — identificador generado (`GenerationType.IDENTITY`).
- `fullName: String` — no puede ser nulo ni vacío (validado en `setFullName`).
- `email: String` — no puede ser nulo ni vacío; único a nivel de columna (`unique = true`).
- `password: String` — no puede ser nulo ni vacío; excluido del `toString()`.
- `role: UserRole` — no puede ser nulo (validado en el constructor); persistido como `String` (`@Enumerated(EnumType.STRING)`).
- `status: UserStatus` — se inicializa automáticamente en `ACTIVE` al crear el usuario.

Relaciones: es el punto de referencia de `BuyerProfile.user`, `SellerProfile.user` y
`ReturnRequest.administrator` (todas relaciones `@OneToOne`/`@ManyToOne` hacia `User`).

### BuyerProfile

Perfil de comprador, asociado uno a uno a un `User`.

- `id: String`.
- `mainAddress: String` — sin validación (puede ser nulo).
- `commercialStatus: CommercialStatus` — se inicializa en `ACTIVE` al crear el perfil desde su constructor de dominio.
- `user: User` — no puede ser nulo (validado en constructor); `@OneToOne` con columna única (`user_id`).
- `shippingAddresses: List<ShippingAddress>` — `@OneToMany` (mappedBy `buyerProfile`, `cascade = ALL`, `orphanRemoval = true`).

Relaciones: dueño de `ShippingAddress` (cascada completa), referenciado por `Cart.buyerProfile` y `Order.buyerProfile`.

### SellerProfile

Perfil de vendedor, asociado uno a uno a un `User`. No tiene constructor de dominio
propio ni validaciones adicionales más allá de las anotaciones JPA (solo constructores
generados por Lombok).

- `id: String`.
- `businessName: String` — sin validación.
- `taxIdentification: String` — sin validación.
- `user: User` — `@OneToOne`, columna única `user_id` (`nullable = false` a nivel de columna, pero no hay chequeo explícito en código Java).
- `warehouses: List<Warehouse>` — `@OneToMany` (mappedBy `sellerProfile`), sin cascada.
- `products: List<Product>` — `@OneToMany` (mappedBy `sellerProfile`), sin cascada.

### ShippingAddress

Dirección de envío asociada a un comprador. Sin constructor de dominio propio ni
validaciones más allá de JPA.

- `id: String`.
- `addressDetails: String`, `city: String`, `zipCode: String` — sin validación.
- `isDefault: boolean` — marca si es la dirección por defecto del comprador (columna `is_default`).
- `buyerProfile: BuyerProfile` — `@ManyToOne`, `nullable = false`.

La invariante de unicidad de la dirección "default" se enforce desde `BuyerProfile.addShippingAddress`, no desde esta clase.

### Product

Aggregate root de catálogo. Un producto pertenece a un único vendedor y agrupa sus variantes vendibles.

- `id: String`.
- `name: String` — no puede ser nulo ni vacío (validado en constructor).
- `description: String` — sin validación, columna de hasta 2000 caracteres.
- `basePrice: BigDecimal` — debe ser mayor a cero (validado tanto en el constructor como en `setBasePrice` vía `validateBasePrice`).
- `type: ProductType` — sin validación de no-nulo en código Java (la columna es `nullable = false`).
- `status: ProductStatus` — se inicializa en `PUBLISHED` al crear el producto.
- `sellerProfile: SellerProfile` — no puede ser nulo (validado en constructor); `@ManyToOne`.
- `variants: List<ProductVariant>` — `@OneToMany` (mappedBy `product`, `cascade = ALL`, `orphanRemoval = true`).

### ProductVariant

Variante vendible de un producto (p. ej. talle/color), con su propio SKU y un ajuste de precio sobre el precio base.

- `id: String`.
- `sku: String` — no puede ser nulo ni vacío (validado en constructor); único a nivel de columna.
- `variantName: String` — sin validación.
- `priceAdjustment: BigDecimal` — si se pasa `null` al constructor, se normaliza a `BigDecimal.ZERO`.
- `product: Product` — no puede ser nulo (validado en constructor); `@ManyToOne`.
- Método `getFinalPrice()`: calcula `product.getBasePrice() + priceAdjustment` (precio final derivado, no persistido).

### Warehouse

Almacén físico donde se guarda inventario. Sin constructor de dominio propio ni
validaciones más allá de JPA.

- `id: String`.
- `name: String`, `location: String` — sin validación.
- `type: WarehouseType` — sin validación de no-nulo en código.
- `sellerProfile: SellerProfile` — `@ManyToOne`, `nullable = true` **a propósito**: el comentario del código aclara que un almacén de tipo `MARKETPLACE` pertenece a la plataforma y no a un vendedor, por lo que puede no tener `sellerProfile`.

### InventoryItem

Registro de stock de una variante de producto en un almacén determinado. Es la clase
con más lógica de invariantes de todo el dominio: la cantidad disponible nunca puede
quedar negativa, y toda mutación pasa por métodos de dominio explícitos, nunca por
asignación directa del campo.

- `id: String`.
- `availableQuantity: int` — nunca negativo (validado en constructor y en `setAvailableQuantity`).
- `reservedQuantity: int` — se inicializa en 0 al crear el ítem.
- `status: InventoryStatus` — derivado automáticamente (`refreshStatus`), nunca asignado manualmente desde fuera de la clase.
- `productVariant: ProductVariant` — no puede ser nulo (validado en constructor); `@ManyToOne(optional = false)`.
- `warehouse: Warehouse` — no puede ser nulo (validado en constructor); `@ManyToOne(optional = false)`.
- Restricción única compuesta a nivel de tabla: `(product_variant_id, warehouse_id)` — un mismo par variante/almacén no puede repetirse.

Métodos de dominio:
- `reserve(qty)`: `qty` debe ser mayor a cero; requiere que `availableQuantity >= qty`; mueve cantidad de disponible a reservado.
- `release(qty)`: `qty` debe ser mayor a cero; requiere que `reservedQuantity >= qty`; mueve cantidad de reservado a disponible.
- `decrementAvailable(qty)`: `qty` debe ser mayor a cero; no puede dejar `availableQuantity` en negativo; descuenta stock de forma definitiva (p. ej. al despachar).
- `refreshStatus()` (privado): recalcula `status` — `OUT_OF_STOCK` si ambas cantidades son 0, `RESERVED` si no queda disponible pero sí reservado, `AVAILABLE` en cualquier otro caso.

### Cart

Carrito de compras de un comprador.

- `id: String`.
- `createdAt: LocalDateTime` — se fija en el momento de creación del carrito.
- `buyerProfile: BuyerProfile` — no puede ser nulo (validado en constructor); `@OneToOne`, columna única `buyer_profile_id` (un comprador tiene a lo sumo un carrito).
- `items: List<CartItem>` — `@OneToMany` (mappedBy `cart`, `cascade = ALL`, `orphanRemoval = true`).
- Método `addItem(variant, quantity)`: `variant` no puede ser nulo; delega la validación de `quantity` al constructor de `CartItem`.

### CartItem

Línea de un carrito, referenciando una variante de producto y una cantidad.

- `id: String`.
- `quantity: int` — debe ser mayor a cero (validado en constructor y en `setQuantity`).
- `cart: Cart` — no puede ser nulo (validado en constructor); `@ManyToOne`.
- `productVariant: ProductVariant` — no puede ser nulo (validado en constructor); `@ManyToOne`.

### Order

Aggregate root de la compra. El comentario de la clase explicita que, a través de
`OrderItem`, se congela el precio pagado por cada variante, porque el precio de un
`ProductVariant` puede cambiar después de concretada la compra.

- `id: String`.
- `orderTrackingNumber: String` — no puede ser nulo ni vacío (validado en constructor); único a nivel de columna.
- `status: OrderStatus` — se inicializa en `CART` al crear la orden.
- `totalAmount: BigDecimal` — se inicializa en `BigDecimal.ZERO` y se recalcula con `recalculateTotal()`.
- `createdAt: LocalDateTime` — se fija en el momento de creación.
- `buyerProfile: BuyerProfile` — no puede ser nulo (validado en constructor); `@ManyToOne`.
- `items: List<OrderItem>` — `@OneToMany` (mappedBy `order`, `cascade = ALL`, `orphanRemoval = true`).

Métodos de dominio:
- `addItem(item)`: `item` no puede ser nulo; agrega el ítem y **recalcula el total automáticamente**.
- `recalculateTotal()`: suma el `subtotal` de cada `OrderItem` y actualiza `totalAmount`.

### OrderItem

Detalle congelado de una compra. La clase **intencionalmente no expone setters** de
cantidad ni precio (solo tiene `@Getter`, no `@Setter`): `unitPriceAtPurchase` es un
snapshot tomado en el momento de la compra que nunca se recalcula desde
`ProductVariant`, y `subtotal` se calcula una única vez en el constructor.

- `id: String`.
- `quantity: int` — debe ser mayor a cero (validado en constructor).
- `unitPriceAtPurchase: BigDecimal` — no puede ser nulo ni negativo (validado en constructor); es el precio "congelado" al momento de la compra.
- `subtotal: BigDecimal` — calculado como `unitPriceAtPurchase * quantity`, una única vez, en el constructor; no tiene setter.
- `order: Order` — no puede ser nulo (validado en constructor); `@ManyToOne`.
- `productVariant: ProductVariant` — no puede ser nulo (validado en constructor); `@ManyToOne`.

### BillingInvoice

Factura de una orden. Sin constructor de dominio propio ni validaciones más allá de JPA.

- `id: String`.
- `invoiceNumber: String` — único a nivel de columna; sin validación explícita en código.
- `tax: BigDecimal`, `totalPaid: BigDecimal` — sin validación.
- `issuedAt: LocalDateTime` — sin validación.
- `order: Order` — `@OneToOne`, columna única `order_id` (una orden tiene a lo sumo una factura).

### ReturnRequest

Solicitud de devolución sobre una orden, gestionada por un administrador.

- `id: String`.
- `reason: String` — sin validación explícita (columna de hasta 1000 caracteres).
- `status: ReturnStatus` — se inicializa en `REQUESTED` al crear la solicitud.
- `order: Order` — no puede ser nula (validado en constructor); `@ManyToOne`.
- `administrator: User` — no puede ser nulo (validado en constructor); `@ManyToOne`. El propio código deja explícito en un comentario que la validación de que este `User` tenga `role = ADMINISTRATOR` se hace en la capa de servicio, no en la entidad ("la entidad no conoce reglas de autorización").

## 4. Enumerados

### UserRole
Rol de un `User` dentro del marketplace.
- `BUYER` — comprador.
- `SELLER` — vendedor.
- `LOGISTICS_OPERATOR` — operador logístico.
- `ADMINISTRATOR` — administrador de la plataforma.
- `SUPERVISOR` — supervisor.

### UserStatus
Estado de la cuenta de un `User`.
- `ACTIVE` — cuenta activa (estado inicial por defecto).
- `BLOCKED` — cuenta bloqueada.
- `SUSPENDED` — cuenta suspendida.

### CommercialStatus
Estado comercial de un `BuyerProfile`.
- `ACTIVE` — comprador habilitado (estado inicial por defecto).
- `RESTRICTED` — comprador con restricciones comerciales.
- `SUSPENDED` — comprador suspendido.

### WarehouseType
Tipo de un `Warehouse`.
- `MARKETPLACE` — almacén propio de la plataforma (no pertenece a un vendedor).
- `SELLER` — almacén propio de un vendedor.

### ProductType
Naturaleza de un `Product`.
- `PHYSICAL` — producto físico.
- `DIGITAL` — producto digital.

### ProductStatus
Estado de publicación de un `Product`.
- `PUBLISHED` — publicado y visible (estado inicial por defecto).
- `SUSPENDED` — suspendido temporalmente.
- `DISCONTINUED` — descontinuado.

### OrderStatus
Ciclo de vida de una `Order`.
- `CART` — la orden todavía es un carrito, no confirmada (estado inicial por defecto).
- `PENDING_PAYMENT` — pendiente de pago.
- `PAID` — pagada.
- `DISPATCHED` — despachada.
- `DELIVERED_FINALIZED` — entregada y finalizada.

### InventoryStatus
Estado de stock de un `InventoryItem`. El propio código aclara en un comentario que
**no estaba especificado en el enunciado original** y que se agregó para soportar
las invariantes de reserva/liberación de inventario.
- `AVAILABLE` — hay cantidad disponible.
- `RESERVED` — no queda disponible, pero sí hay cantidad reservada.
- `OUT_OF_STOCK` — sin disponible ni reservado.

### ReturnStatus
Ciclo de vida de una `ReturnRequest`. El propio código aclara en un comentario que
**no estaba especificado en el enunciado original** y que se agregó para modelar el
flujo de aprobación de devoluciones.
- `REQUESTED` — solicitada (estado inicial por defecto).
- `UNDER_REVIEW` — en revisión.
- `APPROVED` — aprobada.
- `REJECTED` — rechazada.
- `COMPLETED` — completada.

## 5. Reglas de negocio

- Un `User` no puede tener `fullName` nulo o vacío.
- Un `User` no puede tener `email` nulo o vacío; el email es único en la base de datos.
- Un `User` no puede tener `password` nulo o vacío.
- Un `User` no puede tener `role` nulo.
- Al crear un `User`, su `status` se fija automáticamente en `ACTIVE`.
- Un `BuyerProfile` no puede crearse sin un `User` asociado (no nulo).
- Al crear un `BuyerProfile`, su `commercialStatus` se fija automáticamente en `ACTIVE`.
- Un `BuyerProfile` no puede agregar una `ShippingAddress` nula.
- Si la `ShippingAddress` agregada está marcada como `isDefault`, todas las demás direcciones del comprador se desmarcan automáticamente: solo puede existir una dirección default a la vez.
- Un `Product` no puede tener `name` nulo o vacío.
- Un `Product` no puede crearse sin un `SellerProfile` asociado (no nulo).
- El `basePrice` de un `Product` debe ser mayor a cero, tanto al crearlo como al modificarlo.
- Al crear un `Product`, su `status` se fija automáticamente en `PUBLISHED`.
- Un `ProductVariant` no puede tener `sku` nulo o vacío; el SKU es único.
- Un `ProductVariant` no puede crearse sin un `Product` asociado (no nulo).
- Si el `priceAdjustment` de un `ProductVariant` es nulo, se normaliza a `BigDecimal.ZERO`.
- El precio final de un `ProductVariant` (`getFinalPrice()`) es el precio base de su producto más su `priceAdjustment`.
- Un `InventoryItem` no puede crearse sin `ProductVariant` ni `Warehouse` asociados (ambos no nulos).
- La `availableQuantity` de un `InventoryItem` nunca puede ser negativa (validado en constructor y setter).
- Un `InventoryItem` no puede reservar (`reserve`) más cantidad que la disponible; la cantidad a reservar debe ser mayor a cero.
- Un `InventoryItem` no puede liberar (`release`) más cantidad que la reservada; la cantidad a liberar debe ser mayor a cero.
- Un `InventoryItem` no puede descontar (`decrementAvailable`) una cantidad que deje `availableQuantity` en negativo; la cantidad a descontar debe ser mayor a cero.
- El `status` de un `InventoryItem` se recalcula automáticamente tras cada operación de reserva/liberación/descuento, nunca se asigna manualmente desde fuera de la clase.
- No puede existir más de un `InventoryItem` para el mismo par (`productVariant`, `warehouse`) — restricción única a nivel de tabla.
- Un `Warehouse` de tipo `MARKETPLACE` puede no tener `sellerProfile` (pertenece a la plataforma, no a un vendedor); esta es la única razón documentada por la que esa relación es nullable.
- Un `Cart` no puede crearse sin un `BuyerProfile` asociado (no nulo); cada comprador tiene a lo sumo un carrito (columna única).
- Un `CartItem` no puede agregarse a un `Cart` con una `ProductVariant` nula.
- La `quantity` de un `CartItem` debe ser mayor a cero, tanto al crearlo como al modificarla.
- Una `Order` no puede tener `orderTrackingNumber` nulo o vacío; es único.
- Una `Order` no puede crearse sin un `BuyerProfile` asociado (no nulo).
- Al crear una `Order`, su `status` se fija en `CART` y su `totalAmount` en cero.
- Al agregar un `OrderItem` a una `Order`, el total de la orden se recalcula automáticamente sumando el subtotal de todos sus ítems.
- Un `OrderItem` no puede tener `quantity` menor o igual a cero.
- Un `OrderItem` no puede tener `unitPriceAtPurchase` nulo ni negativo.
- El `subtotal` de un `OrderItem` se calcula como `unitPriceAtPurchase * quantity`, una única vez en el constructor, y no puede modificarse después (no tiene setter): es un snapshot inmutable del precio pagado.
- Cada `Order` tiene a lo sumo una `BillingInvoice` (relación uno a uno con columna única); el número de factura es único.
- Una `ReturnRequest` no puede crearse sin una `Order` asociada (no nula).
- Una `ReturnRequest` no puede crearse sin un `User` administrador asociado (no nulo); sin embargo, la entidad **no valida** que ese usuario tenga efectivamente `role = ADMINISTRATOR` — esa validación de autorización queda delegada explícitamente a la capa de servicio.
- Al crear una `ReturnRequest`, su `status` se fija automáticamente en `REQUESTED`.

## 6. Fuera de alcance en esta entrega

Las pruebas unitarias automatizadas fueron intencionalmente pospuestas para una entrega posterior.

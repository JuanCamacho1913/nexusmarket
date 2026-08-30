# Especificación de NexusMarket

> Documento generado a partir del modelo de dominio simplificado
> (`src/main/java/com/nexusmarket/domain` y `src/main/java/com/nexusmarket/valueObjects`).
> Refleja el modelo tal como quedó tras la simplificación a **dominio anémico
> puro**: cada entidad es un POJO Lombok (`@Getter`, `@Setter`,
> `@NoArgsConstructor`) sin anotaciones de persistencia (`jakarta.persistence.*`)
> y sin lógica de negocio. Esta entrega deja explícitamente sin implementar la
> capa de persistencia y las reglas de negocio; ambas se agregarán en una
> entrega posterior (ver sección 6).

## 1. Descripción general

NexusMarket es un **marketplace de e-commerce multi-vendedor**: distintos
vendedores (`SellerProfile`) publican productos (`Product`) y los compradores
(`BuyerProfile`) concretan compras (`Order` / `OrderItem`). Cada producto lista
sus variantes vendibles como simples etiquetas de texto (`Product.variants`),
sin entidad propia.

El sistema modela stock **multi-almacén**: un producto puede tener inventario
(`InventoryItem`) en distintos almacenes (`Warehouse`), propios de la plataforma
(`MARKETPLACE`) o de un vendedor (`SELLER`). Toda compra puede generar una
factura (`BillingInvoice`) asociada uno a uno con la orden, y existe un registro
simple de devoluciones (`ReturnRequest`). La identidad de todos los actores
(comprador, vendedor, operador logístico, administrador, supervisor) se modela
con una única entidad `User` con un rol (`UserRole`); `BuyerProfile` y
`SellerProfile` son perfiles asociados uno a uno.

## 2. Stack tecnológico

Extraído de `pom.xml`:

- **Java 17** (`<java.version>17</java.version>`)
- **Spring Boot 4.1.0** (`spring-boot-starter-parent`)
- **Spring Data JPA** (`spring-boot-starter-data-jpa`) — dependencia presente en el POM; el dominio actual **no** usa ninguna anotación `jakarta.persistence.*` (se agregará en la capa de persistencia de una entrega futura)
- **Spring Data MongoDB** (`spring-boot-starter-data-mongodb`) — dependencia presente en el POM; ninguna clase del dominio actual la usa
- **Spring Security** (`spring-boot-starter-security`)
- **Spring Web** (`spring-boot-starter-web`)
- **MySQL Connector/J** (`com.mysql:mysql-connector-j`, scope `runtime`)
- **Lombok** (`org.projectlombok:lombok`, `optional`) — únicas anotaciones usadas en el dominio: `@Getter`, `@Setter`, `@NoArgsConstructor`
- **spring-boot-starter-test** (scope `test`) — presente en el POM; sin pruebas automatizadas en esta entrega (ver sección 6)

`application.properties` sólo define `spring.application.name`: no hay datasource
configurado ni esquema de base de datos, porque el dominio no tiene ninguna
anotación de mapeo.

## 3. Modelo de dominio

El dominio contiene **exactamente 10 entidades**, todas POJOs anémicos con
`@Getter @Setter @NoArgsConstructor` (sin excepciones — ninguna clase agrega
lógica ni restringe constructores o setters). Todo `id` es un `String` sin
estrategia de generación asignada (se definirá junto con la capa de
persistencia). Los importes son `BigDecimal`.

```
User ──1:1── BuyerProfile ──1:N── Order ──1:N── OrderItem ──N:1── Product
 │                                  │                              │
 │                                  ├──1:1── BillingInvoice        └──N:1── SellerProfile
 │                                  └──1:N── ReturnRequest
 └──1:1── SellerProfile ──1:N── Warehouse ──1:N── InventoryItem ──N:1── Product
```

### User

Aggregate root de identidad.

- `id: String`
- `fullName: String`
- `documentId: String`
- `email: String`
- `role: UserRole`
- `status: UserStatus` — default `ACTIVE` (valor inicial del campo)

No tiene `password` ni constructores de dominio validados. Es el destino de
`BuyerProfile.user` y `SellerProfile.user`.

### BuyerProfile

Perfil de comprador, asociado a un `User`.

- `id: String`
- `mainAddress: String`
- `additionalAddresses: List<String>` — default lista vacía
- `commercialStatus: CommercialStatus` — default `ACTIVE`
- `user: User`

### SellerProfile

Perfil de vendedor, asociado a un `User`.

- `id: String`
- `businessName: String`
- `taxIdentification: String`
- `user: User`
- `warehouses: List<Warehouse>` — default lista vacía
- `products: List<Product>` — default lista vacía

### Product

Aggregate root de catálogo. Un producto pertenece a un único vendedor.

- `id: String`
- `name: String`
- `description: String`
- `price: BigDecimal`
- `type: ProductType`
- `status: ProductStatus` — default `PUBLISHED`
- `sellerProfile: SellerProfile`
- `variants: List<String>` — default lista vacía

No hay precio derivado ni método de recálculo.

### Warehouse

Almacén físico donde se guarda inventario.

- `id: String`
- `name: String`, `location: String`
- `type: WarehouseType`
- `sellerProfile: SellerProfile` — puede ser `null`: un almacén `MARKETPLACE` pertenece a la plataforma y no a un vendedor.

### InventoryItem

Registro de stock de un producto en un almacén.

- `id: String`
- `quantity: int` — sin restricción de valor mínimo en esta entrega (ver sección 6)
- `product: Product`
- `warehouse: Warehouse`

### Order

Representa una compra.

- `id: String`
- `status: OrderStatus` — default `CART`
- `totalAmount: BigDecimal` — campo plano almacenado; el dominio no lo calcula; default `BigDecimal.ZERO`
- `buyerProfile: BuyerProfile`
- `items: List<OrderItem>` — default lista vacía, con getter/setter estándar

### OrderItem

Detalle de una compra.

- `id: String`
- `quantity: int`
- `unitPrice: BigDecimal`
- `order: Order`
- `product: Product`

No hay `subtotal` ni ningún campo derivado.

### BillingInvoice

Factura de una orden.

- `id: String`
- `amount: BigDecimal`
- `issuedAt: LocalDateTime`
- `order: Order`

No hay `invoiceNumber` ni `tax`.

### ReturnRequest

Registro simple de devolución sobre una orden.

- `id: String`
- `reason: String`
- `order: Order`

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

**Ninguna.** El dominio de esta entrega es intencionalmente anémico: no hay
constructores validados, ni setters restringidos, ni métodos que protejan
reglas de negocio. Cualquier código externo puede, por ejemplo, dejar
`InventoryItem.quantity` en negativo o modificar un `Order` en estado
`DELIVERED_FINALIZED` sin que el dominio lo impida.

Esta decisión sigue el patrón mostrado por la cátedra (entidades con solo
`@Getter/@Setter/@NoArgsConstructor`, sin lógica) y se documenta como una
decisión consciente para esta entrega, no como un defecto.

## 6. Fuera de alcance en esta entrega (diferido, no es un defecto)

Los siguientes puntos se dejaron **explícitamente diferidos** a una entrega
posterior y no deben tratarse como huecos del modelo:

- Toda anotación de persistencia (`jakarta.persistence.*`) y el mapeo a tablas.
- Toda regla de negocio / invariante de dominio, incluyendo:
  - Stock nunca negativo en `InventoryItem` (método `adjust(int)` u equivalente).
  - Inmutabilidad de una `Order` en estado `DELIVERED_FINALIZED`.
  - Inmutabilidad del snapshot de precio en `OrderItem`.
  - Obligatoriedad de relaciones (`InventoryItem.product/warehouse`, `Order.buyerProfile`, `Product.sellerProfile`, `BuyerProfile.user`).
- Enforcement real de unicidad de `email` / `documentId`.
- Autorización del registro de vendedores por parte de un Administrador.
- La matriz de responsabilidades de autorización por rol (`UserRole`).
- Estado de inventario dañado y cualquier flujo de reserva / liberación de stock.
- Cálculo del total de una orden a partir de sus ítems.
- Numeración de facturas y cálculo de impuestos.
- Pruebas unitarias automatizadas (pospuestas de forma intencional para esta entrega).

La lógica de negocio e invariantes listadas arriba se incorporarán más
adelante en la capa de persistencia y/o en la capa de servicio, según se vaya
viendo en clase.

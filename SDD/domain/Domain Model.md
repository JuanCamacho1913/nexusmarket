# Domain Model

> Extraído de `SPECIFICATIONS.md` (secciones 3-5). Contiene únicamente las
> especificaciones de la capa de dominio (`src/main/java/com/nexusmarket/domain`
> y `src/main/java/com/nexusmarket/valueObjects`) — sin stack tecnológico ni
> alcance de otras capas.

## 1. Modelo de dominio

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
- `quantity: int` — sin restricción de valor mínimo en esta entrega (ver sección 3)
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

## 2. Enumerados

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

## 3. Invariantes y reglas de negocio

**Ninguna.** El dominio de esta entrega es intencionalmente anémico: no hay
constructores validados, ni setters restringidos, ni métodos que protejan
reglas de negocio. Cualquier código externo puede, por ejemplo, dejar
`InventoryItem.quantity` en negativo o modificar un `Order` en estado
`DELIVERED_FINALIZED` sin que el dominio lo impida.

Esta decisión sigue el patrón mostrado por la cátedra (entidades con solo
`@Getter/@Setter/@NoArgsConstructor`, sin lógica) y se documenta como una
decisión consciente para esta entrega, no como un defecto.

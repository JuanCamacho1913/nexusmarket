# Domain Model

> Extracted from `SPECIFICATIONS.md` (sections 3-5). Contains only the
> specifications of the domain layer (`src/main/java/com/nexusmarket/domain`
> and `src/main/java/com/nexusmarket/valueObjects`) — without the technology
> stack or the scope of other layers.

## 1. Domain model

The domain contains **exactly 10 entities**, all anemic POJOs with
`@Getter @Setter @NoArgsConstructor` (no exceptions — no class adds logic or
restricts constructors or setters). Every `id` is a `String` with no assigned
generation strategy (it will be defined together with the persistence layer).
Amounts are `BigDecimal`.

```
User ──1:1── BuyerProfile ──1:N── Order ──1:N── OrderItem ──N:1── Product
 │                                  │                              │
 │                                  ├──1:1── BillingInvoice        └──N:1── SellerProfile
 │                                  └──1:N── ReturnRequest
 └──1:1── SellerProfile ──1:N── Warehouse ──1:N── InventoryItem ──N:1── Product
```

### User

Identity aggregate root.

- `id: String`
- `fullName: String`
- `documentId: String`
- `email: String`
- `role: UserRole`
- `status: UserStatus` — default `ACTIVE` (initial value of the field)

It has no `password` and no validated domain constructors. It is the target of
`BuyerProfile.user` and `SellerProfile.user`.

### BuyerProfile

Buyer profile, associated with a `User`.

- `id: String`
- `mainAddress: String`
- `additionalAddresses: List<String>` — default empty list
- `commercialStatus: CommercialStatus` — default `ACTIVE`
- `user: User`

### SellerProfile

Seller profile, associated with a `User`.

- `id: String`
- `businessName: String`
- `taxIdentification: String`
- `user: User`
- `warehouses: List<Warehouse>` — default empty list
- `products: List<Product>` — default empty list

### Product

Catalog aggregate root. A product belongs to a single seller.

- `id: String`
- `name: String`
- `description: String`
- `price: BigDecimal`
- `type: ProductType`
- `status: ProductStatus` — default `PUBLISHED`
- `sellerProfile: SellerProfile`
- `variants: List<String>` — default empty list

There is no derived price and no recalculation method.

### Warehouse

Physical warehouse where inventory is stored.

- `id: String`
- `name: String`, `location: String`
- `type: WarehouseType`
- `sellerProfile: SellerProfile` — may be `null`: a `MARKETPLACE` warehouse belongs to the platform and not to a seller.

### InventoryItem

Stock record of a product in a warehouse.

- `id: String`
- `quantity: int` — no minimum value constraint in this delivery (see section 3)
- `product: Product`
- `warehouse: Warehouse`

### Order

Represents a purchase.

- `id: String`
- `status: OrderStatus` — default `CART`
- `totalAmount: BigDecimal` — stored flat field; the domain does not compute it; default `BigDecimal.ZERO`
- `buyerProfile: BuyerProfile`
- `items: List<OrderItem>` — default empty list, with standard getter/setter

### OrderItem

Line item of a purchase.

- `id: String`
- `quantity: int`
- `unitPrice: BigDecimal`
- `order: Order`
- `product: Product`

There is no `subtotal` or any derived field.

### BillingInvoice

Invoice of an order.

- `id: String`
- `amount: BigDecimal`
- `issuedAt: LocalDateTime`
- `order: Order`

There is no `invoiceNumber` or `tax`.

### ReturnRequest

Simple return record on an order.

- `id: String`
- `reason: String`
- `order: Order`

There is no `status` and no reference to an administrator.

## 2. Enumerations

The `valueObjects` package contains **exactly 7 enumerations**.

### UserRole
- `BUYER`, `SELLER`, `LOGISTICS_OPERATOR`, `ADMINISTRATOR`, `SUPERVISOR`.

### UserStatus
- `ACTIVE` (default initial state), `BLOCKED`, `INACTIVE`.

### CommercialStatus
Commercial status of a `BuyerProfile`.
- `ACTIVE` (default), `RESTRICTED`, `SUSPENDED`.

### WarehouseType
- `MARKETPLACE` — the platform's own warehouse.
- `SELLER` — a seller's own warehouse.

### ProductType
- `PHYSICAL`, `DIGITAL`.

### ProductStatus
- `PUBLISHED` (default), `SUSPENDED`, `DISCONTINUED`.

### OrderStatus
- `CART` (default), `PENDING_PAYMENT`, `PAID`, `DISPATCHED`, `DELIVERED_FINALIZED`.

`InventoryStatus` and `ReturnStatus` were removed: inventory status is no longer
modeled (only `quantity` matters) and returns no longer have a lifecycle.

## 3. Invariants and business rules

**None.** The domain in this delivery is intentionally anemic: there are no
validated constructors, no restricted setters, and no methods that protect
business rules. Any external code can, for example, leave
`InventoryItem.quantity` negative or modify an `Order` in the
`DELIVERED_FINALIZED` state without the domain preventing it.

This decision follows the pattern shown by the course instructors (entities with
only `@Getter/@Setter/@NoArgsConstructor`, no logic) and is documented as a
conscious decision for this delivery, not as a defect.

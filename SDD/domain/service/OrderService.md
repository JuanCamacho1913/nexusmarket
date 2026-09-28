# OrderService

> Describes the current behavior of
> `src/main/java/com/nexusmarket/domain/service/OrderService.java`, verified
> against the code. Complements `SDD/domain/Domain Model.md`. It does not
> include unimplemented behavior.

## 1. Purpose and scope

Management of the lifecycle of `Order` and its `OrderItem`s: cart creation,
adding/removing items and changing their quantity, total calculation and status
advancement (`OrderStatus`).

## 2. Dependencies

`@Service` class with `@RequiredArgsConstructor` (constructor injection).

| Dependency | Type | Usage |
|---|---|---|
| `orderRepository` | `OrderRepositoryPort` | `findById`, `save` |
| `productRepository` | `ProductRepositoryPort` | `findById` (in `addItem`) |
| `idGenerator` | `IdGenerator` | `newId()` for `Order` and `OrderItem` |

It does not use `Clock`.

## 3. Public methods

| Method | What it does | Preconditions / invariants | Exceptions |
|---|---|---|---|
| `Order createOrder(Order order)` | Assigns `id`, forces `status = CART`, `totalAmount = 0` and empty items, and saves. | `buyerProfile` is not `null`. | `ValidationException` |
| `Order addItem(String orderId, String productId, int quantity)` | Creates an `OrderItem` with `unitPrice` copied from `product.price`, adds it, recalculates the total and saves. | The order exists and is not `DELIVERED_FINALIZED`; the product exists. | `EntityNotFoundException`, `InvariantViolationException` |
| `Order removeItem(String orderId, String orderItemId)` | Removes the item with that `id`, recalculates the total and saves. | The order exists and is not `DELIVERED_FINALIZED`. | `EntityNotFoundException`, `InvariantViolationException` |
| `Order changeQuantity(String orderId, String orderItemId, int newQuantity)` | Changes the item's quantity, recalculates the total and saves. | The order exists and is not `DELIVERED_FINALIZED`; the item exists in the order. | `EntityNotFoundException`, `InvariantViolationException` |
| `Order advanceStatus(String orderId, OrderStatus newStatus)` | Changes the status to the next allowed one and saves. | The order exists, is not `DELIVERED_FINALIZED`, and `newStatus` is the direct successor of the current status. | `EntityNotFoundException`, `InvariantViolationException` |

## 4. Business rules

- **Status transitions (linear, no skipping or going back):** `CART -> PENDING_PAYMENT -> PAID -> DISPATCHED -> DELIVERED_FINALIZED`. Any other `newStatus` (including repeating the current one) throws `InvariantViolationException`.
- **Immutability of a finalized order:** in `DELIVERED_FINALIZED`, all mutating operations (`addItem`, `removeItem`, `changeQuantity`, `advanceStatus`) throw `InvariantViolationException`.
- **Price snapshot:** `OrderItem.unitPrice` is copied from `Product.price` when the item is added; `changeQuantity` does not modify it.
- **Computed total:** `totalAmount` = sum of `unitPrice * quantity` of all items, with scale 2 and `RoundingMode.HALF_UP`. It is recalculated in `addItem`, `removeItem` and `changeQuantity`.
- **Forced initial values:** `createOrder` ignores the received `status`, `totalAmount` and `items`.
- **Immutable items list:** the service assigns `Collections.unmodifiableList` as the order's `items`.
- **Mandatory relationship:** `Order.buyerProfile` cannot be `null` on creation.
- **Identifiers:** the order and item `id`s are always assigned with `idGenerator.newId()`.

## 5. Notes

- `quantity`/`newQuantity` is not validated (it accepts zero or negative values), nor is a null `product.price`.
- `removeItem` does not fail if `orderItemId` does not exist in the order: it saves without changes.
- `addItem` does not check `ProductStatus` (a `SUSPENDED` or `DISCONTINUED` product can be added) and does not merge repeated items of the same product.
- `advanceStatus` does not check preconditions for advancing (e.g. an order without items or effective payment) and does not deduct/reserve stock.
- `Order` remains a mutable POJO: the immutability of `DELIVERED_FINALIZED` is guaranteed only through this service.
- Divergence from `SPECIFICATIONS.md` §6: the immutability of `DELIVERED_FINALIZED`, the price snapshot, the total calculation and the mandatory `Order.buyerProfile` are listed as deferred; the service already implements them. Stock reservation/release remains out of scope.

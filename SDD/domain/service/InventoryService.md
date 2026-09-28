# InventoryService

> Describes the current behavior of
> `src/main/java/com/nexusmarket/domain/service/InventoryService.java`,
> verified against the code. Complements `SDD/domain/Domain Model.md`. It does
> not include unimplemented behavior.

## 1. Purpose and scope

Creation of `InventoryItem` (stock of a `Product` in a `Warehouse`) and
adjustment of its `quantity`. It does not cover reservations, stock release or
transfers between warehouses.

## 2. Dependencies

`@Service` class with `@RequiredArgsConstructor` (constructor injection).

| Dependency | Type | Usage |
|---|---|---|
| `inventoryItemRepository` | `InventoryItemRepositoryPort` | `findById`, `save` |
| `idGenerator` | `IdGenerator` | `newId()` to assign the `id` before `save` |

It does not use `Clock`.

## 3. Public methods

| Method | What it does | Preconditions / invariants | Exceptions |
|---|---|---|---|
| `InventoryItem createItem(InventoryItem inventoryItem)` | Assigns a new `id` and saves the stock record. | `product` is not `null`; `warehouse` is not `null` (checked in that order). | `ValidationException` |
| `InventoryItem adjustStock(String itemId, int delta)` | Adds `delta` (positive or negative) to `quantity` and saves. | The item exists; `quantity + delta >= 0`. | `EntityNotFoundException`, `InvariantViolationException` |

## 4. Business rules

- **Stock never negative through adjustment:** `adjustStock` rejects any `delta` that leaves the quantity below zero; in that case the item is neither modified nor saved. A result of exactly zero is valid.
- **Mandatory relationships:** `InventoryItem.product` and `InventoryItem.warehouse` cannot be `null` on creation.
- **Identifiers:** the received `id` is always overwritten with `idGenerator.newId()`.

## 5. Notes

- `createItem` does not validate `quantity`: an item can be created with a negative quantity, so the non-negative stock invariant is only guaranteed in `adjustStock`.
- `adjustStock` does not treat `delta == 0` specially (it is an adjustment with no effect that still saves).
- There is no duplicate control by (`product`, `warehouse`) pair and no verification that the referenced objects exist.
- The `quantity + delta` computation uses `int` with no overflow protection.
- Divergence from `SPECIFICATIONS.md` §6: there, "stock never negative" and the mandatory `InventoryItem.product/warehouse` are listed as deferred; the service already implements them (the first only in `adjustStock`). Damaged inventory status and stock reservation/release remain out of scope.

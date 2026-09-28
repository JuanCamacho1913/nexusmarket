# ReturnRequestService

> Describes the current behavior of
> `src/main/java/com/nexusmarket/domain/service/ReturnRequestService.java`,
> verified against the code. Complements `SDD/domain/Domain Model.md`. It does
> not include unimplemented behavior.

## 1. Purpose and scope

Registration of a `ReturnRequest` on an `Order`. The return is a simple record
(`id`, `reason`, `order`) with no lifecycle or approval.

## 2. Dependencies

`@Service` class with `@RequiredArgsConstructor` (constructor injection).

| Dependency | Type | Usage |
|---|---|---|
| `orderRepository` | `OrderRepositoryPort` | `findById` |
| `returnRequestRepository` | `ReturnRequestRepositoryPort` | `save` |
| `idGenerator` | `IdGenerator` | `newId()` for the return `id` |

It does not use `Clock`.

## 3. Public methods

| Method | What it does | Preconditions / invariants | Exceptions |
|---|---|---|---|
| `ReturnRequest create(String orderId, String reason)` | Creates and saves a return associated with the order. | The order exists; its status is `DELIVERED_FINALIZED`; `reason` is neither `null` nor blank (checked in that order). | `EntityNotFoundException`, `InvariantViolationException`, `ValidationException` |

## 4. Business rules

- **Delivered orders only:** a return can only be requested if the order is in `DELIVERED_FINALIZED`; any other status throws `InvariantViolationException`.
- **Mandatory reason:** `reason` cannot be `null` or `isBlank()`.
- **Identifiers:** the `id` is assigned with `idGenerator.newId()`.

## 5. Notes

- Since the status check precedes the reason check, a non-finalized order with an empty `reason` produces `InvariantViolationException`, not `ValidationException`.
- There is no limit on returns per order: several can be created for the same `Order`.
- The return does not modify the order or the inventory, and does not validate items, quantities or deadlines.
- `reason` is stored as-is, without trimming whitespace.
- There is no status and no reference to an administrator (matches the domain model: `ReturnStatus` was removed).
- Divergence from `SPECIFICATIONS.md` §6: there are no rules about returns in that section; the restriction to `DELIVERED_FINALIZED` and the mandatory reason are service behavior.

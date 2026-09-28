# BillingInvoiceService

> Describes the current behavior of
> `src/main/java/com/nexusmarket/domain/service/BillingInvoiceService.java`,
> verified against the code. Complements `SDD/domain/Domain Model.md`. It does
> not include unimplemented behavior.

## 1. Purpose and scope

Issuing and querying the `BillingInvoice` associated with an `Order` (1:1 relationship).

## 2. Dependencies

`@Service` class with `@RequiredArgsConstructor` (constructor injection).

| Dependency | Type | Usage |
|---|---|---|
| `orderRepository` | `OrderRepositoryPort` | `findById` |
| `billingInvoiceRepository` | `BillingInvoiceRepositoryPort` | `findByOrderId`, `save` |
| `idGenerator` | `IdGenerator` | `newId()` for the invoice `id` |
| `clock` | `java.time.Clock` | `LocalDateTime.now(clock)` for `issuedAt` |

## 3. Public methods

| Method | What it does | Preconditions / invariants | Exceptions |
|---|---|---|---|
| `BillingInvoice issueFor(String orderId)` | Creates and saves an invoice with `amount = order.totalAmount` and `issuedAt = LocalDateTime.now(clock)`. | The order exists; its status is `PAID`, `DISPATCHED` or `DELIVERED_FINALIZED`; the order has no previous invoice (checked in that order). | `EntityNotFoundException`, `InvariantViolationException`, `DuplicateEntityException` |
| `Optional<BillingInvoice> findByOrder(String orderId)` | Returns the order's invoice, if it exists. | None. | None (returns `Optional.empty()` if there is no invoice) |

## 4. Business rules

- **Eligibility by status:** invoicing happens only in `PAID`, `DISPATCHED` or `DELIVERED_FINALIZED`. `CART` and `PENDING_PAYMENT` are rejected.
- **One invoice per order:** if `findByOrderId` already returns an invoice, `issueFor` throws `DuplicateEntityException`.
- **Amount:** direct copy of `Order.totalAmount` at the time of issuance.
- **Issue date:** comes from the injected `Clock` (not directly from the system clock).

## 5. Notes

- The invoice has no numbering or taxes: the `BillingInvoice` model only has `id`, `amount`, `issuedAt` and `order`.
- `findByOrder` does not validate that the order exists.
- Uniqueness is checked with a prior query; the service does not protect it against concurrency (it depends on the persistence layer).
- The current source code defines neither a `Clock` bean nor an `IdGenerator` bean: `UuidIdGenerator` is not annotated as a component and there is no `@Configuration` class in `src`. These beans must be provided by the application configuration.
- Divergence from `SPECIFICATIONS.md` §6: invoice numbering and taxes remain deferred (matches). Eligibility by status and uniqueness per order do not appear in §5/§6 and the service enforces them.

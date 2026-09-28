# ProductService

> Describes the current behavior of
> `src/main/java/com/nexusmarket/domain/service/ProductService.java`, verified
> against the code. Complements `SDD/domain/Domain Model.md`. It does not
> include unimplemented behavior.

## 1. Purpose and scope

Creation of `Product` (catalog aggregate root) and management of its lifecycle
through `ProductStatus` (`PUBLISHED`, `SUSPENDED`, `DISCONTINUED`).

## 2. Dependencies

`@Service` class with `@RequiredArgsConstructor` (constructor injection).

| Dependency | Type | Usage |
|---|---|---|
| `productRepository` | `ProductRepositoryPort` | `findById`, `save` |
| `idGenerator` | `IdGenerator` | `newId()` to assign the `id` before `save` |

It does not use `Clock`.

## 3. Public methods

| Method | What it does | Preconditions / invariants | Exceptions |
|---|---|---|---|
| `Product createProduct(Product product)` | Assigns a new `id` and saves the product. | `sellerProfile` is not `null`. | `ValidationException` |
| `Product publish(String productId)` | Changes the status to `PUBLISHED` and saves. | The product exists and is not `DISCONTINUED`. | `EntityNotFoundException`, `InvariantViolationException` |
| `Product suspend(String productId)` | Changes the status to `SUSPENDED` and saves. | Same as `publish`. | `EntityNotFoundException`, `InvariantViolationException` |
| `Product discontinue(String productId)` | Changes the status to `DISCONTINUED` and saves. | Same as `publish`. | `EntityNotFoundException`, `InvariantViolationException` |

The three transition methods share the private method
`requireTransitionable`, which looks up the product and verifies that it is not
`DISCONTINUED`.

## 4. Business rules

- **Mandatory relationship:** every product belongs to a seller (`sellerProfile != null`).
- **`DISCONTINUED` is terminal:** a discontinued product admits no further transition (including another call to `discontinue`).
- **Transitions between `PUBLISHED` and `SUSPENDED`:** there is no restriction; either one can move to the other or be repeated.
- **Identifiers:** the received `id` is always overwritten with `idGenerator.newId()`.

## 5. Notes

- `createProduct` does not force the initial status: the one carried by the entity is kept (by default `PUBLISHED`, the initial value of the field).
- `price` (null, zero or negative), `name` and `type` are not validated.
- There is no authorization: the service does not verify who requests the transition nor that they are the owning seller.
- Divergence from `SPECIFICATIONS.md` §6: the mandatory `Product.sellerProfile` is listed as deferred; the service already validates it. The `ProductStatus` lifecycle does not appear in §5/§6, and the service enforces `DISCONTINUED` as terminal.

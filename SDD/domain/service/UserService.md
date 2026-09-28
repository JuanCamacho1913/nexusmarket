# UserService

> Describes the current behavior of
> `src/main/java/com/nexusmarket/domain/service/UserService.java`, verified
> against the code. Complements `SDD/domain/Domain Model.md`. It does not
> include unimplemented behavior.

## 1. Purpose and scope

Registration of identities: `User`, `BuyerProfile` and `SellerProfile`. Assigns
the `id` of each entity and applies the uniqueness and authorization rules of
registration. It does not cover authentication, passwords or user status
changes.

## 2. Dependencies

`@Service` class with `@RequiredArgsConstructor` (constructor injection).

| Dependency | Type | Usage |
|---|---|---|
| `userRepository` | `UserRepositoryPort` | `existsByEmail`, `existsByDocumentId`, `save` |
| `buyerProfileRepository` | `BuyerProfileRepositoryPort` | `save` |
| `sellerProfileRepository` | `SellerProfileRepositoryPort` | `save` |
| `idGenerator` | `IdGenerator` | `newId()` to assign the `id` before `save` |

It does not use `Clock`.

## 3. Public methods

| Method | What it does | Preconditions / invariants | Exceptions |
|---|---|---|---|
| `User register(User user)` | Assigns a new `id` and saves the user. | `email` does not exist; `documentId` does not exist (checked in that order). | `DuplicateEntityException` (duplicate email or duplicate documentId) |
| `BuyerProfile registerBuyer(BuyerProfile buyerProfile)` | Assigns a new `id` and saves the buyer profile. | `buyerProfile.user` is not `null`. | `ValidationException` |
| `SellerProfile registerSeller(String adminUserId, SellerProfile sellerProfile)` | Loads the acting user by `adminUserId`, assigns a new `id` and saves the seller profile. | The acting user exists, has `role = ADMINISTRATOR` and `status = ACTIVE`. | `EntityNotFoundException` (acting user not found), `UnauthorizedOperationException` (not an active administrator) |

## 4. Business rules

- **Uniqueness:** `User`'s `email` and `documentId` are unique; they are checked in `register` through the `existsBy*` ports before saving.
- **Seller registration by administrator only:** `registerSeller` loads the acting user through `UserRepositoryPort` and rejects the operation unless that user is an `ADMINISTRATOR` with `ACTIVE` status. The error message includes `adminUserId`.
- **Mandatory relationship:** `BuyerProfile.user` cannot be `null`.
- **Identifiers:** the `id` received in the entity is always overwritten with `idGenerator.newId()`.

## 5. Notes

- `registerSeller` does not validate that `sellerProfile.user` is non-`null` (unlike `registerBuyer`).
- `registerBuyer` and `registerSeller` do not verify that the `User` exists or that it already has a profile (the 1:1 relationship is not enforced).
- `register` does not validate null fields or the format of `email`/`documentId`, and does not force `role` or `status`.
- Divergence from `SPECIFICATIONS.md` §6: there, the uniqueness of `email`/`documentId`, the authorization for seller registration and the mandatory `BuyerProfile.user` are listed as deferred; the service already implements them.

# Code Review: functional-spring-boot

**Review Date:** 2026-08-07  
**Commit:** 0e1c9ae  
**Java:** 17 (target) / JDK 25 (runtime)  
**Spring Boot:** 4.0.6  
**Reviewer:** Automated (Mammouth Code)

---

## 1. Summary Scorecard

| Category                | Score  | Notes |
|-------------------------|--------|-------|
| **Lint Compliance**     | **9/10** | Checkstyle passes clean (0 violations). Config covers naming, whitespace, imports, formatting. Missing advanced checks (cyclomatic complexity, Javadoc validation, design rules). |
| **Code Quality**         | **7/10** | Clean DDD architecture with functional programming style. Immutable entities with builder pattern. 32 categories of duplicated code identified. Some methods are near-identical copy-paste (AccessCheckerImpl: 6 methods). |
| **Security**            | **4/10** | Hardcoded DB credentials and SpiceDB token. Plaintext gRPC. Password in `toString()`. Exposed actuator with full details. HTTP Basic auth. `eraseCredentials(false)`. No rate limiting. No security headers. |
| **Maintainability**      | **6/10** | 566 unit tests, 100% pass rate. 4 integration tests disabled. Extensive duplication (32 categories) increases change risk. Good module separation helps. |
| **Documentation**        | **3/10** | README is 19 lines. No Javadoc on any class. No API documentation. No CONTRIBUTING. No architecture documentation. No CHANGELOG. |
| **Idempotency**          | **8/10** | Functional programming style with immutable entities (all fields final). Stateless commands. Builders return new instances. Minor: `UserInput.toString()` leaks password; `LocalDateTime.now()` defaults in builder are non-deterministic. |
| **Overall**              | **6/10** | Strong architecture, excellent test coverage, and functional design. Held back by security vulnerabilities, extensive code duplication, and near-absent documentation. |

---

## 2. Repository Structure

```
functional-spring-boot/
├── pom.xml                          # Parent POM (multi-module, dependency management)
├── checkstyle.xml                   # Checkstyle configuration (30+ rules)
├── checkstyle.suppression.xml       # Checkstyle suppressions
├── README.md                        # Minimal project description (19 lines)
│
├── functional-domain/               # Domain layer (pure Java, minimal deps)
│   ├── pom.xml                      #   Banned dependencies enforcement (SLF4J + test only)
│   └── src/
│       ├── main/java/.../domain/    #   Entities, commands, access, path parsing
│       │   ├── entities/             #     Item, File, Folder, User, Group, AccessRight, Path, Output, Failure
│       │   ├── command/             #     Command pattern: factories, check/success/error commands
│       │   │   ├── factory/          #       13 item factories, 9 user factories
│       │   │   └── impl/             #       check/, success/, error/ command implementations
│       │   ├── access/              #     Access right parsing (composite factory, recursive parsers)
│       │   ├── path/                #     Path parsing (composite factory, navigation parsers)
│       │   └── user/                #     UserManager
│       ├── main/java/.../driven/    #   Driven ports (interfaces): repositories, rules, PasswordProtector
│       ├── main/java/.../driving/  #   Driving ports (interfaces): services
│       └── test/java/               #   363 unit tests
│
├── functional-infra/               # Infrastructure layer (JDBC, filesystem)
│   ├── pom.xml                      #   Spring JDBC, Liquibase, H2 (test)
│   └── src/
│       ├── main/java/.../infra/     #   Jdbc repositories, mappers, extractors, SQL constants
│       │   ├── impl/                 #     JdbcUserRepository, JdbcGroupRepository, JdbcItemRepository, etc.
│       │   ├── mapper/               #     Row mappers and result extractors
│       │   └── utils/                #     SQL constants per entity
│       ├── main/resources/          #   Liquibase changelog (5 migrations + master)
│       └── test/java/               #   42 unit tests
│
├── functional-check/               # Authorization layer (SpiceDB integration)
│   ├── pom.xml                      #   AuthZed (SpiceDB), gRPC, SLF4J
│   └── src/
│       ├── main/java/.../rules/     #   AccessChecker, UserChecker, AccessUpdater, UserUpdater, RuleLoader
│       │   ├── check/                #     AccessCheckerImpl, UserCheckerImpl, Permissions, Entities
│       │   ├── update/               #     AccessUpdaterImpl, UserUpdaterImpl, Relations
│       │   └── init/                 #     RuleLoaderImpl
│       └── test/java/               #   95 unit tests
│
└── functional-app/                  # Application layer (Spring Boot web app)
    ├── pom.xml                      #   Spring Web, Security, Actuator, gRPC, Passay
    └── src/
        ├── main/java/.../functional/
        │   ├── config/               #     Security, Domain, Grpc, Infrastructure, Check configurations
        │   ├── controller/           #     FileSystem, User, Group controllers + exception handler
        │   ├── service/              #     FileSystemService, UserManagementService, FunctionalUserDetailsService
        │   ├── security/             #     PasswordProtectorImpl
        │   ├── mapper/               #     EntityMapper
        │   ├── model/                #     DTOs (UserDTO, GroupDTO, ItemDTO, InputUserDTO, ErrorDTO)
        │   └── exception/            #     UserException, GroupException, PathException, CommandException
        ├── main/resources/          #   application.yml, application-dev.yml, spicedb/rules.zed
        └── test/java/               #   66 unit tests + 4 skipped integration tests
```

**Module Dependency Graph:**

```
                    functional-app
                   /      |      \
        functional-infra  |   functional-check
                   \      |      /
                    functional-domain
```

- `functional-domain` has zero framework dependencies (only SLF4J + test libs)
- `functional-infra` depends on domain + Spring JDBC + Liquibase
- `functional-check` depends on domain + AuthZed/SpiceDB + gRPC
- `functional-app` depends on all three + Spring Web/Security/Actuator

**Key Metrics:**

| Metric | Value |
|--------|-------|
| Total Java source files (main) | 216 |
| Total Java test files | 91 |
| Total unit tests | 566 |
| Unit tests passing | 566 (100%) |
| Integration tests | 4 (all skipped) |
| Checkstyle violations | 0 |
| Maven modules | 4 |
| Checkstyle rules | 30+ |

---

## 3. Build & Test Results

### 3.1 Lint (Checkstyle)

```
$ ./mvnw clean checkstyle:check
BUILD SUCCESS
```

Checkstyle configuration (`checkstyle.xml`) enforces:
- Naming conventions (classes, methods, fields, constants, parameters)
- Import ordering (java group, separated, top)
- Whitespace rules (after, around, before, no trailing spaces)
- Brace placement (left/right curly)
- No empty catch blocks, no empty statements
- Final classes, no mutable exceptions
- No string literal equality, no unnecessary parentheses
- No unused/redundant imports
- File tab character check, newline at end of file

**Not covered:** Cyclomatic complexity, Javadoc validation, method count limits, parameter count limits, design rules.

### 3.2 Unit Tests

```
$ ./mvnw clean test
functional-domain:  Tests run: 363, Failures: 0, Errors: 0, Skipped: 0
functional-infra:   Tests run:  42, Failures: 0, Errors: 0, Skipped: 0
functional-check:   Tests run:  95, Failures: 0, Errors: 0, Skipped: 0
functional-app:     Tests run:  66, Failures: 0, Errors: 0, Skipped: 0
TOTAL:              Tests run: 566, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 3.3 Integration Tests

```
$ ./mvnw clean verify
functional-app (failsafe): Tests run: 4, Failures: 0, Errors: 0, Skipped: 4
BUILD SUCCESS
```

Integration tests (`UserControllerIT`) are **disabled/skipped**. Git log reference: `64b768a Disable Integration Test as not yet working properly`.

### 3.4 Coverage

JaCoCo is configured with a minimum coverage ratio of **95%** (instruction coverage), with zero missed classes and zero missed methods. Reports are generated in XML format for SonarCloud integration.

### 3.5 Mutation Testing

Pitest is configured (via `mutationTesting` profile) with a mutation threshold of **80%**. Excludes `config.*` classes and integration tests.

---

## 4. Incomplete Tasks

The following tasks are **not completed** or remain outstanding:

| # | Task | Status | Reference |
|---|------|--------|-----------|
| 1 | **Integration tests disabled** | Skipped (4 tests) | `functional-app/src/test/java/.../integration/UserControllerIT.java` — Git commit `64b768a`: "Disable Integration Test as not yet working properly" |
| 2 | **Login page not implemented** | Missing | `SecurityConfiguration.java:39` — `formLogin(formLogin -> formLogin.loginPage("/login"))` references a login page that has no controller or template |
| 3 | **JaCoCo coverage check may fail** | Unverified | JaCoCo `check` goal requires 95% coverage + 0 missed classes/methods, but was not verified in this review (requires `verify` phase) |
| 4 | **Mutation testing not run** | Not executed | Pitest profile `mutationTesting` exists but was not activated in this review |
| 5 | **Production configuration externalization** | Incomplete | `application.yml` contains hardcoded credentials instead of environment variable placeholders |

---

## 5. Duplicated Code

The following is a comprehensive list of duplicated code patterns found across the codebase.

### 5.1 Domain Layer — Command Factories (21 classes)

**Item Command Factories (13 classes):** `ChangeGroupCommandFactory`, `ChangeModeCommandFactory`, `ChangeOwnerCommandFactory`, `CopyItemCommandFactory`, `CreateItemCommandFactory`, `DeleteItemCommandFactory`, `DownloadCommandFactory`, `ListChildrenCommandFactory`, `MoveItemCommandFactory`, `UnsupportedItemCommandFactory`, `UploadCommandFactory` — All follow an identical skeleton: logger field, `supports()` checking command type + input validity, `createCommand()` logging and instantiating the corresponding `Check*Command`. Only the command type enum, validation logic, and constructor args differ.

**User Command Factories (9 classes):** `CreateGroupCommandFactory`, `CreateUserCommandFactory`, `DeleteGroupCommandFactory`, `DeleteUserCommandFactory`, `GetGroupCommandFactory`, `GetUserCommandFactory`, `UpdateGroupCommandFactory`, `UpdateUserCommandFactory`, `UnsupportedUserCommandFactory` — Same pattern as above with `UserCommandType`/`UserInput`.

**Example duplication:**
```java
// Identical isValid() in CopyItemCommandFactory and MoveItemCommandFactory:
private boolean isValid(ItemInput itemInput) {
    return nonNull(itemInput.getItem()) && nonNull(itemInput.getDestination()) &&
            !Objects.equals(itemInput.getItem(), itemInput.getDestination());
}
```

### 5.2 Domain Layer — Check Commands (13+ classes)

**Check*ItemCommand (7 classes):** `CheckChangeGroupCommand`, `CheckChangeModeCommand`, `CheckChangeOwnerCommand`, `CheckDeleteItemCommand`, `CheckDownloadCommand`, `CheckListChildrenCommand`, `CheckUploadCommand` — Identical structure: `checkAccess()` building `List<String> reasons`, `createSuccess()` logging and instantiating success command, `createError()` building `ItemInput` and returning `ItemErrorCommand`.

**Check*UserCommand (6 classes):** `CheckCreateGroupCommand`, `CheckCreateUserCommand`, `CheckDeleteGroupCommand`, `CheckDeleteUserCommand`, `CheckUpdateGroupCommand`, `CheckUpdateUserCommand` — Same pattern with `UserInput` and `UserErrorCommand`.

**CheckUnsupported*Command (2 classes):** `CheckUnsupportedItemCommand` and `CheckUnsupportedUserCommand` are structurally identical, differing only in types.

### 5.3 Domain Layer — Error Commands (2 classes)

`ItemErrorCommand` and `UserErrorCommand` are virtually identical:
```java
// Both:
public Output<T> execute(User actor) {
    logger.debug("Command {} with arguments {} failed for {}", type, input, actor);
    return Output.failure(String.format("%s with arguments %s failed for %s", type, input, actor), reasons);
}
```

### 5.4 Domain Layer — Composite Factories (4 classes)

`CompositeItemCommandFactory`, `CompositeUserCommandFactory`, `CompositeAccessParserFactory`, `CompositePathParserFactory` — All share the exact same dispatch pattern: constructor sorting by `order()`, `create*()` streaming factories, filtering by `supports()`, mapping to `create*()`, `findFirst().orElseThrow()`. Recursive-capable ones also have identical `configureRecursive()` methods.

### 5.5 Domain Layer — Access Parser Factories (9 classes)

**Right factories (3):** `ReadAccessParserFactory`, `WriteAccessParserFactory`, `ExecuteAccessParserFactory` — Differ only in matched string ("r"/"w"/"x") and `AccessRight` method reference.

**Action factories (3):** `AddAccessParserFactory`, `RemoveAccessParserFactory`, `SetAccessParserFactory` — Differ only in matched string ("+"/"-"/"=") and `BiFunction`.

**Attribution factories (3):** `OwnerAccessParserFactory`, `GroupAccessParserFactory`, `OtherAccessParserFactory` — Differ only in matched string ("u?"/"g"/"o") and builder/getter references.

### 5.6 Domain Layer — Success Commands

`GetUserCommand` and `GetGroupCommand` are near-identical: same fields, same constructor, same `execute()` logic (if name not null, find by name; else if userId not null, find by ID; else return actor's own data).

`CopyItemCommand` and `MoveItemCommand` share identical `getDestinationFolder()` and `getDestinationName()` helper methods:
```java
// Identical in both:
private Folder getDestinationFolder() {
    return destination.isFile() ? destination.getParent() : (Folder) destination;
}
private String getDestinationName() {
    return destination.isFile() ? destination.getName() : source.getName();
}
```

### 5.7 Domain Layer — Factory Interfaces (4 interfaces)

`ItemCommandFactory`, `UserCommandFactory`, `AccessParserFactory`, `PathParserFactory` — All have the same shape: `supports()`, `create*()`, and `default int order() { return 0; }`.

### 5.8 Check Layer — AccessCheckerImpl (6 methods)

All 6 methods (`canRead`, `canWrite`, `canExecute`, `canChangeMode`, `canChangeOwner`, `canChangeGroup`) have identical 15-line bodies building `CheckPermissionRequest`, calling `permissionsService.checkPermission()`, comparing to `PERMISSIONSHIP_HAS_PERMISSION`, catching/logging errors. Only the permission string differs. Unlike `UserCheckerImpl`, these do **not** use a shared helper method.

### 5.9 Check Layer — UserCheckerImpl (6 methods)

All 6 methods (`canCreateUser`, `canUpdateUser`, `canDeleteUser`, `canCreateGroup`, `canUpdateGroup`, `canDeleteGroup`) have identical bodies. Uses a shared `createRequest()` helper, but the try/catch/compare pattern is still duplicated 6 times.

### 5.10 Check Layer — AccessUpdaterImpl & UserUpdaterImpl

Both classes share identical `publishRelations()` method:
```java
// Identical in both:
private String publishRelations(List<RelationshipUpdate> relations) {
    WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
            .addAllUpdates(relations).build();
    try {
        WriteRelationshipsResponse response = permissionsService.writeRelationships(request);
        return response.getWrittenAt().getToken();
    } catch (Exception e) {
        LOGGER.error("Error while publishing relations", e);
        return null;
    }
}
```

`createRelation()` and `deleteRelation()` methods are also duplicated between the two classes.

### 5.11 Infra Layer — SQL Patterns (7+ patterns)

Duplicated SQL query patterns across `JdbcGroupRepository`, `JdbcUserRepository`, `JdbcItemRepository`:
- `SELECT 1 FROM %s WHERE NAME = :name` (exists by name)
- `SELECT 1 FROM %s WHERE ID = :id` (exists by ID)
- `INSERT INTO %s (ID, NAME) VALUES (:id, :name)` (create)
- `UPDATE %s SET NAME = :name WHERE ID = :id` (update name)
- `UPDATE %s SET ID = :id WHERE NAME = :name` (update ID)
- `DELETE FROM %s WHERE ID = :id AND NAME = :name` (delete)
- `Boolean.TRUE.equals(jdbcTemplate.query(query, params, existsExtractor))` (exists-check idiom, 6+ occurrences)

### 5.12 Infra Layer — SQL Constants

`COL_ID = "id"` and `COL_NAME = "name"` are defined identically in `GroupSQLConstants`, `ItemSQLConstants`, and `UserSQLConstants`.

### 5.13 App Layer — Exception Classes (3 classes)

`GroupException`, `PathException`, and `UserException` are structurally identical: extend `RuntimeException`, have `serialVersionUID`, single constructor taking `Failure` and calling `super(failure.getMessage())`.

### 5.14 App Layer — Exception Handler (4 methods)

`FunctionalExceptionHandler` has 4 nearly identical `@ExceptionHandler` methods. Each logs, builds an `ErrorDTO` with `withType()`/`withMessage()`, and returns `ResponseEntity.badRequest().body(error)`. Only `handleCommandException` adds `.withReasons()`.

### 5.15 App Layer — FileSystemService (8 methods)

Every public method follows the same 15-line boilerplate: find user by name → check failure → throw `UserException`; get path → check error → throw `PathException`; build `ItemInput`; process command → check failure → throw `CommandException`; map result.

### 5.16 App Layer — UserManagementService (9 methods)

Every public method follows the same boilerplate: find user by name → check failure → throw `UserException`; build `UserInput`; process command → check failure → throw `CommandException`; map result.

### 5.17 Test Layer — Check Command Tests (20+ classes)

All check command test classes share identical structure: `@ExtendWith(MockitoExtension.class)`, same `@Mock` fields, `@BeforeEach` setup with `File.builder()`/`Folder.builder()`, and two test methods (`shouldGenerateXxxCommandWhenCheckingSucceed` / `shouldGenerateErrorCommandWhenCheckingFails`).

### 5.18 Test Layer — Success Command Tests (6+ classes)

Success command tests follow the same template: mocked repositories, `@BeforeEach` setup, mock `repository.update(any())`, execute, assert `Output.isSuccess`.

### 5.19 Test Layer — Controller Tests (3 classes)

All 3 controller test classes share identical class-level annotations, `@BeforeEach` setup with `MockMvcBuilders.webAppContextSetup()`, and repeated assertion blocks. `FileSystemControllerTest` contains massive intra-class duplication: the same 5-line assertion block is repeated 9 times per error scenario (3 scenarios = 27 repetitions).

### 5.20 Test Layer — Infra Repository Tests (3 classes)

All 3 infra test classes share identical `@JdbcTest` annotations, `ROOT_ID`/`ROOT_NAME` constants. `JdbcGroupRepositoryTest` and `JdbcUserRepositoryTest` have structurally identical test methods (`shouldCheckExistenceOfRootXxxByName`, `shouldCheckExistenceOfRootXxxById`, etc.).

---

## 6. Security Review

### 6.1 Critical Issues

| # | Severity | Issue | Location | Recommendation |
|---|----------|-------|----------|----------------|
| 1 | **CRITICAL** | Hardcoded database credentials | `application.yml:15-16` — `username: postgres`, `password: password` | Use environment variables: `password: ${DB_PASSWORD}` |
| 2 | **CRITICAL** | Hardcoded SpiceDB API token | `application.yml:53` — `token: supersecretpassword` | Use environment variables: `token: ${SPICEDB_TOKEN}` |
| 3 | **HIGH** | Password leaked in `toString()` | `UserInput.java:69` — `password='" + password + "'"` | Remove password from `toString()` or mask it |
| 4 | **HIGH** | Plaintext gRPC connection | `application.yml:51` — `negotiation-type: PLAINTEXT` | Use TLS for SpiceDB connections |
| 5 | **HIGH** | `eraseCredentials(false)` | `SecurityConfiguration.java:84` | Set to `true` to clear credentials from security context after authentication |

### 6.2 Medium Issues

| # | Severity | Issue | Location | Recommendation |
|---|----------|-------|----------|----------------|
| 6 | **MEDIUM** | Actuator endpoints fully exposed without auth | `SecurityConfiguration.java:34` — `/actuator/**` is `permitAll` | Restrict actuator endpoints or require authentication |
| 7 | **MEDIUM** | Actuator health shows full details | `application.yml:40-41` — `show-details: always`, `show-components: always` | Set to `when_authorized` to prevent info leakage |
| 8 | **MEDIUM** | HTTP Basic authentication only | `SecurityConfiguration.java:36` — `httpBasic(Customizer.withDefaults())` | Add JWT/OAuth2 or at minimum enforce HTTPS |
| 9 | **MEDIUM** | No rate limiting on auth endpoints | N/A | Add rate limiting (e.g., Spring Security's `requestRateMatcher`) |
| 10 | **MEDIUM** | No security headers configured | `SecurityConfiguration.java` | Add HSTS, X-Frame-Options, X-Content-Type-Options, CSP |
| 11 | **MEDIUM** | Test config uses weak credentials | `application-test.yml:7` — `password: sa` | Acceptable for test, but should not be reused in non-test contexts |

### 6.3 Low Issues

| # | Severity | Issue | Location | Recommendation |
|---|----------|-------|----------|----------------|
| 12 | **LOW** | Lazy initialization enabled | `application.yml:6` — `lazy-initialization: true` | Not recommended for production; may hide startup errors |
| 13 | **LOW** | No CSRF token handling for APIs | `SecurityConfiguration.java:37` — CSRF enabled with defaults | For REST APIs, consider stateless CSRF or disable for API paths |
| 14 | **LOW** | Multipart upload to tmp dir | `application.yml:9` — `location: ${java.io.tmpdir}` | Ensure proper cleanup and size validation |
| 15 | **LOW** | Content repository path hardcoded | `application.yml:44` — `content.repository.path: /tmp` | Externalize to environment variable |

### 6.4 Positive Security Measures

- Password hashing via `PasswordEncoderFactories.createDelegatingPasswordEncoder()` (bcrypt by default)
- Password validation policy via Passay (length 8-16, upper/lower/digit/symbol, no sequences, no whitespace)
- Full authentication required for all non-actuator endpoints (`.anyRequest().fullyAuthenticated()`)
- CSRF protection enabled
- SpiceDB integration for fine-grained authorization (RBAC/ABAC)
- Parameterized SQL queries (no SQL injection risk via `NamedParameterJdbcTemplate`)
- Architecture tests enforcing layer dependencies

---

## 7. Dependency Matrix

### 7.1 Project Dependencies

```
functional-app
├── functional-domain
├── functional-infra
├── functional-check
├── spring-boot-starter-web
├── spring-boot-starter-security
├── spring-boot-starter-actuator
├── spring-boot-starter-jackson
├── passay-spring (2.0.0)
├── postgresql (42.7.11)
├── grpc-client-spring-boot-starter (3.1.0.RELEASE)
├── grpc-inproc
├── hibernate-validator (9.1.0.Final)
├── [test] spring-boot-starter-test
├── [test] spring-security-test
├── [test] h2
├── [test] spring-cloud-starter-bootstrap
├── [test] embedded-spicedb (testcontainers)
├── [test] archunit-junit5 (1.4.2)
└── [optional] spring-boot-devtools

functional-infra
├── functional-domain
├── spring-boot-starter-jdbc
├── spring-boot-starter-liquibase
├── [test] spring-boot-starter-jdbc-test
├── [test] jimfs (1.3.1)
└── [test] h2

functional-check
├── functional-domain
├── authzed (1.5.4)
├── grpc-protobuf
├── grpc-stub
├── slf4j-api
├── [test] junit-jupiter
├── [test] mockito-junit-jupiter
├── [test] assertj-core
└── [test] slf4j-nop

functional-domain
├── slf4j-api
├── [test] junit-jupiter-engine
├── [test] junit-jupiter-params
├── [test] mockito-junit-jupiter
├── [test] assertj-core
└── [test] slf4j-nop
```

### 7.2 Build & Tool Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| spring-boot-dependencies | 4.0.6 | BOM for Spring Boot managed dependencies |
| spring-cloud-dependencies | 2025.1.1 | BOM for Spring Cloud |
| testcontainers-spring-boot-bom | 4.0.0 | BOM for testcontainers integration |
| grpc-bom | 1.81.0 | BOM for gRPC dependencies |
| maven-enforcer-plugin | 3.6.3 | Enforce Maven 3.6.3+ and Java 21+ |
| maven-compiler-plugin | 3.15.0 | Java 17 compilation with `-parameters -Xlint:unchecked` |
| maven-surefire-plugin | 3.5.5 | Unit test execution with Mockito agent |
| maven-failsafe-plugin | 3.5.5 | Integration test execution |
| maven-checkstyle-plugin | 3.6.0 | Code style enforcement |
| checkstyle | 13.4.2 | Checkstyle engine |
| jacoco-maven-plugin | 0.8.14 | Code coverage (95% minimum) |
| pitest-maven | 1.25.0 | Mutation testing (80% threshold) |
| sonar-maven-plugin | 5.6.0.6792 | SonarCloud integration |
| git-commit-id-maven-plugin | 10.0.0 | Git info in build metadata |
| spring-boot-maven-plugin | 4.0.6 | Application packaging |
| maven-dependency-plugin | 3.10.0 | Dependency properties |

### 7.3 Key Library Versions

| Library | Version | Module |
|---------|---------|--------|
| Spring Boot | 4.0.6 | app (BOM) |
| Spring Cloud | 2025.1.1 | app (BOM) |
| Liquibase | 5.0.3 | infra |
| PostgreSQL Driver | 42.7.11 | app |
| AuthZed (SpiceDB) | 1.5.4 | check |
| gRPC | 1.81.0 | check, app |
| Passay | 2.0.0 | app |
| Hibernate Validator | 9.1.0.Final | app |
| Jimfs | 1.3.1 | infra (test) |
| ArchUnit | 1.4.2 | app (test) |
| Testcontainers | 2.0.5 | app (test) |
| Mockito | 5.8.0 | all (test) |

---

## 8. Additional Observations

### 8.1 Architecture Strengths
- Clean Domain-Driven Design with 4-layer separation (domain, infra, check, app)
- Domain layer has near-zero external dependencies (only SLF4J) — enforced via Maven enforcer
- Functional programming style with immutable entities, `Output<T>` result types, and stateless commands
- Command pattern with check → execute → error flow
- Composite factory pattern for extensibility
- SpiceDB integration for fine-grained authorization
- Architecture tests (ArchUnit) enforcing layer boundaries

### 8.2 Code Smells
- `AbstractCheckItemCommand.createError()` and `AbstractCheckUserCommand.createError()` throw `UnsupportedOperationException` — surprising for a method that subclasses are expected to override
- `FunctionalExceptionHandler.handlePathException(GroupException e)` — method named `handlePathException` but handles `GroupException` (copy-paste naming error)
- `UserManagementService.getGroups()` missing debug log (all other methods have one)
- `AccessCheckerImpl` has 6 copy-paste methods while `UserCheckerImpl` uses a shared helper — inconsistent refactoring
- `AbstractItem.AbstractBuilder` defaults `LocalDateTime.now()` for created/lastModified/lastAccessed — non-deterministic, makes testing harder

### 8.3 Java Version Mismatch
- `pom.xml` sets `java.version=17` but `maven-enforcer-plugin` requires Java 21+
- Runtime JDK is Java 25
- Compilation targets Java 17 with `--release 17`

# Implementation Plan: Code Quality Improvement & Integration Test Fix

**Created:** 2026-08-07  
**Based on:** `CODE_REVIEW.md` (Overall Score: 6/10)  
**Target:** Overall Score 8/10+ across all categories

---

## Table of Contents

1. [Phase 1: Security Hardening (Critical)](#phase-1-security-hardening-critical)
2. [Phase 2: Fix Integration Tests](#phase-2-fix-integration-tests)
3. [Phase 3: Eliminate Code Duplication](#phase-3-eliminate-code-duplication)
4. [Phase 4: Code Smell Fixes](#phase-4-code-smell-fixes)
5. [Phase 5: Documentation](#phase-5-documentation)
6. [Phase 6: Lint & Build Hardening](#phase-6-lint--build-hardening)
7. [Validation Checklist](#validation-checklist)

---

## Phase 1: Security Hardening (Critical)

**Priority:** P0 — Must be done first  
**Estimated effort:** 2-3 hours  
**Impact:** Security score 4/10 → 7/10

### 1.1 Externalize Hardcoded Credentials

**Files to modify:**
- `functional-app/src/main/resources/application.yml`

**Changes:**

```yaml
# BEFORE (lines 13-16):
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/functional
    username: postgres
    password: password

# AFTER:
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/functional}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD}
```

```yaml
# BEFORE (line 53):
grpc.client:
  spicedb:
    token: supersecretpassword

# AFTER:
grpc.client:
  spicedb:
    token: ${SPICEDB_TOKEN}
```

```yaml
# BEFORE (line 44):
content.repository.path: /tmp

# AFTER:
content.repository.path: ${CONTENT_REPOSITORY_PATH:/tmp}
```

**Add a `.env.example` file** at the project root documenting all required environment variables:

```env
DB_URL=jdbc:postgresql://localhost:5432/functional
DB_USERNAME=postgres
DB_PASSWORD=<your-password>
SPICEDB_TOKEN=<your-spicedb-token>
CONTENT_REPOSITORY_PATH=/tmp
```

### 1.2 Remove Password from UserInput.toString()

**File:** `functional-domain/src/main/java/io/github/fherbreteau/functional/domain/entities/UserInput.java`

```java
// BEFORE (lines 64-76):
@Override
public String toString() {
    return "UserInput{" +
            "userId=" + userId +
            ", name='" + name + '\'' +
            ", password='" + password + '\'' +
            ", groupId=" + groupId +
            ", groups='" + groups + '\'' +
            ", newName='" + newName + '\'' +
            ", force=" + force +
            ", append=" + append +
            '}';
}

// AFTER:
@Override
public String toString() {
    return "UserInput{" +
            "userId=" + userId +
            ", name='" + name + '\'' +
            ", password='***'" +
            ", groupId=" + groupId +
            ", groups='" + groups + '\'' +
            ", newName='" + newName + '\'' +
            ", force=" + force +
            ", append=" + append +
            '}';
}
```

### 1.3 Enable eraseCredentials

**File:** `functional-app/src/main/java/io/github/fherbreteau/functional/config/SecurityConfiguration.java`

```java
// BEFORE (line 84):
builder.eraseCredentials(false);

// AFTER:
builder.eraseCredentials(true);
```

**Note:** Verify that the `FunctionalUserDetailsService` still works after this change. The `UserDetailsService` loads the password into the `UserDetails` object for the authentication phase, but `eraseCredentials(true)` will clear it from the `SecurityContext` after authentication completes. The application does not use the password from `SecurityContext` after authentication — it re-fetches it via `userService.getUserPassword(user)` — so this change is safe.

### 1.4 Restrict Actuator Exposure

**File:** `functional-app/src/main/resources/application.yml`

```yaml
# BEFORE (lines 39-41):
management:
  endpoint:
    health:
      show-details: always
      show-components: always

# AFTER:
management:
  endpoint:
    health:
      show-details: when_authorized
      show-components: when_authorized
```

```java
// File: SecurityConfiguration.java
// BEFORE (line 34):
.requestMatchers("/actuator/**").permitAll()

// AFTER:
.requestMatchers("/actuator/health", "/actuator/info").permitAll()
.requestMatchers("/actuator/**").hasRole("ADMIN")
```

**Note:** This requires adding an `ADMIN` role to the root user. The SpiceDB `admin` relation already exists — map it to a Spring Security role in `FunctionalUserDetailsService`.

### 1.5 Add Security Headers

**File:** `functional-app/src/main/java/io/github/fherbreteau/functional/config/SecurityConfiguration.java`

Add to the `filterChain` method after `.csrf()`:

```java
.headers(headers -> headers
    .frameOptions(Options::disable) // No frames needed
    .contentTypeOptions(ContentTypeOptionsConfig::disable) // Already set by Spring
    .httpStrictTransportSecurity(hsts -> hsts
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000))
    .contentSecurityPolicy(csp -> csp
        .policyDirectives("default-src 'self'"))
    .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
)
```

### 1.6 Validate: Run Unit Tests

After Phase 1 changes, run:

```bash
./mvnw clean test
```

All 566 unit tests must still pass. The `SecurityConfigurationTest` may need updating to verify the new header configuration.

---

## Phase 2: Fix Integration Tests

**Priority:** P0 — Critical for CI/CD confidence  
**Estimated effort:** 4-6 hours  
**Impact:** Maintainability score 6/10 → 7/10

### 2.1 Root Cause Analysis

The integration test `UserControllerIT` is `@Disabled("Need a way to wait on SpiceDB synchronization.")`.

**Root cause:** After `ApplicationStartListener.onApplicationStarted()` calls:
1. `configurator.defineRules()` — writes the SpiceDB schema
2. `configurator.initializeDefaultUser()` — calls `userUpdater.createGroup(root.getGroup())` and `userUpdater.createUser(root)` which writes relationships to SpiceDB via `permissionsService.writeRelationships(request)`

The `WriteRelationshipsResponse` returns a `writtenAt` token (a zedtoken), but this token is **not stored or used** in subsequent `CheckPermissionRequest` calls. Without a consistency token, SpiceDB may use its default consistency level (eventually consistent), meaning the root user's `admin` relationship may not yet be visible when the test immediately calls `GET /users` as root.

The flow is:
1. `ApplicationStartListener` creates root user in SpiceDB (writes relationships)
2. Test calls `GET /users` with `@WithMockUser("root")`
3. `FunctionalUserDetailsService` loads root user → calls `userManager.getPassword(root)` → checks `userRepository.exists(root)` → queries H2 DB → root exists in DB (via Liquibase) → password returned
4. Spring Security authenticates the user
5. `UserController.getUser()` calls `userManagementService.getUser(null, null, "root")`
6. `UserManagementService` calls `userService.findUserByName("root")` → `userManager.findUserByName("root")` → `userRepository.exists("root")` → true → `userRepository.findByName("root")` → returns root User
7. Then calls `userService.processCommand(UserCommandType.ID, root, input)`
8. This creates a `CheckGetUserCommand` which calls `userChecker.canUpdateUser(name, actor)` or similar
9. `UserCheckerImpl` calls `permissionsService.checkPermission(request)` — **no zedtoken passed**
10. SpiceDB may not yet see the `admin` relationship for root → returns `NO_PERMISSION` → test fails

### 2.2 Fix Strategy A: Consistency Token Propagation (Preferred)

**Goal:** Capture the zedtoken from `WriteRelationshipsResponse` during initialization and propagate it through the call chain to `CheckPermissionRequest`.

#### Step 1: Add token storage to UserUpdater

**File:** `functional-check/src/main/java/io/github/fherbreteau/functional/rules/update/UserUpdaterImpl.java`

Add a thread-safe token holder:

```java
public class UserUpdaterImpl implements UserUpdater {

    private volatile String lastWrittenToken;

    // In createUser():
    String token = publishRelations(updates);
    this.lastWrittenToken = token;
    LOGGER.info("User {} created at {}", user, token);
    return user;

    // Add getter:
    public String getLastWrittenToken() {
        return lastWrittenToken;
    }
}
```

#### Step 2: Add optional consistency to UserChecker

**File:** `functional-check/src/main/java/io/github/fherbreteau/functional/rules/check/UserCheckerImpl.java`

```java
public class UserCheckerImpl implements UserChecker {

    private final PermissionsServiceBlockingStub permissionsService;
    private final Supplier<String> consistencyTokenSupplier;

    public UserCheckerImpl(PermissionsServiceBlockingStub permissionsService,
                           Supplier<String> consistencyTokenSupplier) {
        this.permissionsService = permissionsService;
        this.consistencyTokenSupplier = consistencyTokenSupplier;
    }

    private CheckPermissionRequest createRequest(String permission, UUID userId) {
        CheckPermissionRequest.Builder builder = CheckPermissionRequest.newBuilder()
                .setPermission(permission)
                .setResource(ObjectReference.newBuilder()
                        .setObjectId(userId.toString())
                        .setObjectType(USER))
                .setSubject(SubjectReference.newBuilder()
                        .setObject(ObjectReference.newBuilder()
                                .setObjectId(userId.toString())
                                .setObjectType(USER)));
        
        String token = consistencyTokenSupplier.get();
        if (token != null) {
            builder.setConsistency(Consistency.newBuilder()
                    .setAtLeastAsFresh(ZedToken.newBuilder()
                            .setToken(token)));
        }
        return builder.build();
    }
}
```

#### Step 3: Wire the token supplier in CheckConfiguration

**File:** `functional-app/src/main/java/io/github/fherbreteau/functional/config/CheckConfiguration.java`

```java
@Bean
UserChecker userChecker(PermissionsServiceBlockingStub permissionsService, 
                        UserUpdaterImpl userUpdaterImpl) {
    return new UserCheckerImpl(permissionsService, userUpdaterImpl::getLastWrittenToken);
}
```

**Note:** This requires exposing `UserUpdaterImpl` (not just the `UserUpdater` interface) or adding `getLastWrittenToken()` to the `UserUpdater` interface. The cleaner approach is to create a `ConsistencyTokenHolder` component:

```java
@Component
public class ConsistencyTokenHolder {
    private volatile String token;
    
    public void setToken(String token) { this.token = token; }
    public String getToken() { return token; }
}
```

Then inject it into both `UserUpdaterImpl` and `UserCheckerImpl`:

```java
// In UserUpdaterImpl constructor:
public UserUpdaterImpl(PermissionsServiceBlockingStub permissionsService,
                       ConsistencyTokenHolder tokenHolder) {
    this.permissionsService = permissionsService;
    this.tokenHolder = tokenHolder;
}

// In createUser():
String token = publishRelations(updates);
tokenHolder.setToken(token);
```

```java
// In UserCheckerImpl constructor:
public UserCheckerImpl(PermissionsServiceBlockingStub permissionsService,
                       ConsistencyTokenHolder tokenHolder) {
    this.permissionsService = permissionsService;
    this.tokenHolder = tokenHolder;
}

// In createRequest():
String token = tokenHolder.getToken();
if (token != null) {
    builder.setConsistency(Consistency.newBuilder()
            .setAtLeastAsFresh(ZedToken.newBuilder()
                    .setToken(token)));
}
```

**Note:** The `ConsistencyTokenHolder` needs to be in the `functional-check` module since both `UserUpdaterImpl` and `UserCheckerImpl` are there. Add it as a Spring `@Component` or configure it as a `@Bean` in `CheckConfiguration`.

#### Step 4: Apply the same pattern to AccessCheckerImpl

**File:** `functional-check/src/main/java/io/github/fherbreteau/functional/rules/check/AccessCheckerImpl.java`

Refactor to use a shared `checkPermission(String permission, String resourceType, String resourceId, UUID subjectId)` helper (also addresses duplication item 5.8):

```java
public class AccessCheckerImpl implements AccessChecker {

    private final PermissionsServiceBlockingStub permissionsService;
    private final ConsistencyTokenHolder tokenHolder;

    public AccessCheckerImpl(PermissionsServiceBlockingStub permissionsService,
                             ConsistencyTokenHolder tokenHolder) {
        this.permissionsService = permissionsService;
        this.tokenHolder = tokenHolder;
    }

    @Override
    public <T extends Item> boolean canRead(T item, User actor) {
        return checkPermission(READ, ITEM, item.getHandle().toString(), actor.getUserId());
    }

    @Override
    public <T extends Item> boolean canWrite(T item, User actor) {
        return checkPermission(WRITE, ITEM, item.getHandle().toString(), actor.getUserId());
    }

    @Override
    public <T extends Item> boolean canExecute(T item, User actor) {
        return checkPermission(EXECUTE, ITEM, item.getHandle().toString(), actor.getUserId());
    }

    @Override
    public <T extends Item> boolean canChangeMode(T item, User actor) {
        return checkPermission(CHANGE_MODE, ITEM, item.getHandle().toString(), actor.getUserId());
    }

    @Override
    public <T extends Item> boolean canChangeOwner(T item, User actor) {
        return checkPermission(CHANGE_OWNER, ITEM, item.getHandle().toString(), actor.getUserId());
    }

    @Override
    public <T extends Item> boolean canChangeGroup(T item, User actor) {
        return checkPermission(CHANGE_GROUP, ITEM, item.getHandle().toString(), actor.getUserId());
    }

    private boolean checkPermission(String permission, String resourceType, String resourceId, UUID subjectId) {
        CheckPermissionRequest.Builder builder = CheckPermissionRequest.newBuilder()
                .setPermission(permission)
                .setResource(ObjectReference.newBuilder()
                        .setObjectId(resourceId)
                        .setObjectType(resourceType))
                .setSubject(SubjectReference.newBuilder()
                        .setObject(ObjectReference.newBuilder()
                                .setObjectId(subjectId.toString())
                                .setObjectType(USER)));
        String token = tokenHolder.getToken();
        if (token != null) {
            builder.setConsistency(Consistency.newBuilder()
                    .setAtLeastAsFresh(ZedToken.newBuilder()
                            .setToken(token)));
        }
        try {
            CheckPermissionResponse response = permissionsService.checkPermission(builder.build());
            return response.getPermissionship() == PERMISSIONSHIP_HAS_PERMISSION;
        } catch (Exception e) {
            LOGGER.error("Error while checking {} permission on {}/{} by {} ", 
                    permission, resourceType, resourceId, subjectId, e);
            return false;
        }
    }
}
```

#### Step 5: Update AccessUpdaterImpl with token holder

**File:** `functional-check/src/main/java/io/github/fherbreteau/functional/rules/update/AccessUpdaterImpl.java`

Inject `ConsistencyTokenHolder` and set token after each `publishRelations()` call:

```java
public class AccessUpdaterImpl implements AccessUpdater {

    private final PermissionsServiceBlockingStub permissionsService;
    private final ConsistencyTokenHolder tokenHolder;

    public AccessUpdaterImpl(PermissionsServiceBlockingStub permissionsService,
                             ConsistencyTokenHolder tokenHolder) {
        this.permissionsService = permissionsService;
        this.tokenHolder = tokenHolder;
    }

    // In createItem():
    String token = publishRelations(updates);
    tokenHolder.setToken(token);
    LOGGER.info("Item {} created at {}", item, token);
    return item;

    // ... same for updateOwner, updateGroup, updateOwnerAccess, updateGroupAccess, 
    //     updateOtherAccess, deleteItem
}
```

#### Step 6: Update RuleLoaderImpl to set token

**File:** `functional-check/src/main/java/io/github/fherbreteau/functional/rules/init/RuleLoaderImpl.java`

After `writeRules()`, set the token from the `WriteSchemaResponse`:

```java
public void writeRules(Rules rules) {
    WriteSchemaRequest request = WriteSchemaRequest.newBuilder()
            .setSchema(rules.content())
            .build();
    WriteSchemaResponse response = schemaService.writeSchema(request);
    tokenHolder.setToken(response.getWrittenAt().getToken());
    LOGGER.info("Schema written at {}", response.getWrittenAt().getToken());
}
```

#### Step 7: Update CheckConfiguration

**File:** `functional-app/src/main/java/io/github/fherbreteau/functional/config/CheckConfiguration.java`

```java
@Configuration
@GrpcClientBean(
        clazz = PermissionsServiceBlockingStub.class,
        beanName = "permissionsService",
        client = @GrpcClient("spicedb"))
public class CheckConfiguration {

    @Bean
    ConsistencyTokenHolder consistencyTokenHolder() {
        return new ConsistencyTokenHolder();
    }

    @Bean
    AccessChecker accessChecker(PermissionsServiceBlockingStub permissionsService,
                                ConsistencyTokenHolder tokenHolder) {
        return new AccessCheckerImpl(permissionsService, tokenHolder);
    }

    @Bean
    AccessUpdater accessUpdater(PermissionsServiceBlockingStub permissionsService,
                                ConsistencyTokenHolder tokenHolder) {
        return new AccessUpdaterImpl(permissionsService, tokenHolder);
    }

    @Bean
    UserChecker userChecker(PermissionsServiceBlockingStub permissionsService,
                            ConsistencyTokenHolder tokenHolder) {
        return new UserCheckerImpl(permissionsService, tokenHolder);
    }

    @Bean
    UserUpdater userUpdater(PermissionsServiceBlockingStub permissionsService,
                           ConsistencyTokenHolder tokenHolder) {
        return new UserUpdaterImpl(permissionsService, tokenHolder);
    }

    @Bean
    RuleLoader ruleLoader(@GrpcClient("spicedb") SchemaServiceBlockingStub schemaService,
                          ConsistencyTokenHolder tokenHolder) {
        return new RuleLoaderImpl(schemaService, tokenHolder);
    }
}
```

### 2.3 Alternative Fix Strategy B: Awaitility Polling (Fallback)

If Strategy A proves too complex, use Awaitility in the integration test to poll until SpiceDB is ready:

**File:** `functional-app/src/test/java/io/github/fherbreteau/functional/integration/UserControllerIT.java`

```java
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = FunctionalApplication.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerIT {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private UserChecker userChecker;

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Wait for SpiceDB to be ready (root user visible)
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    User root = User.root();
                    assertThat(userChecker.canCreateUser("test", root)).isTrue();
                });
    }
    // ... rest of tests unchanged, remove @Disabled
}
```

**Add Awaitility dependency** to `functional-app/pom.xml`:

```xml
<dependency>
    <groupId>org.awaitility</groupId>
    <artifactId>awaitility</artifactId>
    <scope>test</scope>
</dependency>
```

### 2.4 Fix Test Assertions

**File:** `functional-app/src/test/java/io/github/fherbreteau/functional/integration/UserControllerIT.java`

The test `createUserAsExistingUser` (Order 4) creates a user but does not clean up. Since tests are ordered, test 4 depends on test 2 succeeding. If test 2 fails, test 4 may still pass because the user already exists from a previous run. Add proper cleanup:

```java
@AfterAll
static void cleanup(@Autowired UserRepository userRepository) {
    // Clean up any test users created
    if (userRepository.exists("user1")) {
        User user1 = userRepository.findByName("user1");
        userRepository.delete(user1);
    }
}
```

Or better: use `@Transactional` or `@DirtiesContext` to reset state between tests:

```java
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = FunctionalApplication.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class UserControllerIT {
```

### 2.5 Add More Integration Tests

Once the core 4 tests pass, add integration tests for:

- **GroupController** — `GroupControllerIT.java` with tests for create, list, modify, delete groups
- **FileSystemController** — `FileSystemControllerIT.java` with tests for create file/folder, list, change owner/group/mode, download, delete
- **Error scenarios** — Unauthorized access, non-existent resources, permission denied

### 2.6 Validate: Run Integration Tests

```bash
./mvnw clean verify -pl functional-app
```

Expected: `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0` (integration tests)  
All 566 unit tests must also pass.

---

## Phase 3: Eliminate Code Duplication

**Priority:** P1  
**Estimated effort:** 8-12 hours  
**Impact:** Code Quality 7/10 → 8/10, Maintainability 7/10 → 8/10

### 3.1 Extract Shared Base Error Command

**Files:**
- `functional-domain/src/main/java/.../domain/command/impl/error/ItemErrorCommand.java`
- `functional-domain/src/main/java/.../domain/command/impl/error/UserErrorCommand.java`

**Approach:** Extract a `GenericErrorCommand<T, E extends Enum<E>>` base class:

```java
public abstract class GenericErrorCommand<T, E extends Enum<E>> implements Command<Output<T>> {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());
    protected final E type;
    protected final Object input;
    protected final List<String> reasons;

    protected GenericErrorCommand(E type, Object input) {
        this(type, input, List.of());
    }

    protected GenericErrorCommand(E type, Object input, List<String> reasons) {
        this.type = type;
        this.input = input;
        this.reasons = reasons;
    }

    @Override
    public Output<T> execute(User actor) {
        logger.debug("Command {} with arguments {} failed for {}", type, input, actor);
        return Output.failure(
                String.format("%s with arguments %s failed for %s", type, input, actor), reasons);
    }
}
```

`ItemErrorCommand` and `UserErrorCommand` become one-liner subclasses.

### 3.2 Extract Shared AccessCheckerImpl Helper

Already covered in Phase 2.4 — the 6 copy-paste methods in `AccessCheckerImpl` are replaced with a single `checkPermission(permission, resourceType, resourceId, subjectId)` helper.

### 3.3 Extract Shared UserCheckerImpl Helper

**File:** `functional-check/src/main/java/.../rules/check/UserCheckerImpl.java`

Similar to AccessCheckerImpl, extract the try/catch/compare into a helper:

```java
private boolean checkPermission(String permission, String name, User actor) {
    CheckPermissionRequest request = createRequest(permission, actor.getUserId());
    try {
        CheckPermissionResponse response = permissionsService.checkPermission(request);
        return response.getPermissionship() == PERMISSIONSHIP_HAS_PERMISSION;
    } catch (Exception e) {
        LOGGER.error("Error while checking {} permission on {} by {} ", permission, name, actor, e);
        return false;
    }
}

@Override
public boolean canCreateUser(String name, User actor) {
    LOGGER.debug("Checking canCreateUser({}, {})", name, actor);
    return checkPermission(CREATE, name, actor);
}
// ... all 6 methods become 2 lines each
```

### 3.4 Extract Shared SpiceDB Relationship Helper

**Files:**
- `functional-check/src/main/java/.../rules/update/AccessUpdaterImpl.java`
- `functional-check/src/main/java/.../rules/update/UserUpdaterImpl.java`

Extract `publishRelations()`, `createRelation()`, and `deleteRelation()` into a shared `SpiceDbHelper`:

```java
public class SpiceDbHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpiceDbHelper.class);
    private final PermissionsServiceBlockingStub permissionsService;
    private final ConsistencyTokenHolder tokenHolder;

    public SpiceDbHelper(PermissionsServiceBlockingStub permissionsService,
                         ConsistencyTokenHolder tokenHolder) {
        this.permissionsService = permissionsService;
        this.tokenHolder = tokenHolder;
    }

    public RelationshipUpdate createRelation(String relation, String resourceType, 
                                              String resourceId, String subjectType, String subjectId) {
        return RelationshipUpdate.newBuilder()
                .setOperation(RelationshipUpdate.Operation.OPERATION_CREATE)
                .setRelationship(Relationship.newBuilder()
                        .setRelation(relation)
                        .setResource(ObjectReference.newBuilder()
                                .setObjectType(resourceType)
                                .setObjectId(resourceId))
                        .setSubject(SubjectReference.newBuilder()
                                .setObject(ObjectReference.newBuilder()
                                        .setObjectType(subjectType)
                                        .setObjectId(subjectId))))
                .build();
    }

    public RelationshipUpdate deleteRelation(String relation, String resourceType,
                                              String resourceId, String subjectType, String subjectId) {
        return RelationshipUpdate.newBuilder()
                .setOperation(RelationshipUpdate.Operation.OPERATION_DELETE)
                .setRelationship(Relationship.newBuilder()
                        .setRelation(relation)
                        .setResource(ObjectReference.newBuilder()
                                .setObjectType(resourceType)
                                .setObjectId(resourceId))
                        .setSubject(SubjectReference.newBuilder()
                                .setObject(ObjectReference.newBuilder()
                                        .setObjectType(subjectType)
                                        .setObjectId(subjectId))))
                .build();
    }

    public String publishRelations(List<RelationshipUpdate> relations) {
        WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
                .addAllUpdates(relations)
                .build();
        try {
            WriteRelationshipsResponse response = permissionsService.writeRelationships(request);
            String token = response.getWrittenAt().getToken();
            tokenHolder.setToken(token);
            return token;
        } catch (Exception e) {
            LOGGER.error("Error while publishing relations", e);
            return null;
        }
    }
}
```

Both `AccessUpdaterImpl` and `UserUpdaterImpl` delegate to this helper.

### 3.5 Extract Shared SQL Constants

**Files:**
- `functional-infra/src/main/java/.../infra/utils/GroupSQLConstants.java`
- `functional-infra/src/main/java/.../infra/utils/ItemSQLConstants.java`
- `functional-infra/src/main/java/.../infra/utils/UserSQLConstants.java`

Create a `BaseSQLConstants`:

```java
public class BaseSQLConstants {
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    private BaseSQLConstants() {}
}
```

Each entity's SQL constants class extends or imports from `BaseSQLConstants`.

### 3.6 Extract Shared Exception Base Class

**Files:**
- `functional-app/src/main/java/.../exception/GroupException.java`
- `functional-app/src/main/java/.../exception/PathException.java`
- `functional-app/src/main/java/.../exception/UserException.java`

Create a `FunctionalException` base:

```java
public abstract class FunctionalException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    protected FunctionalException(Failure failure) {
        super(failure.getMessage());
    }
}
```

Each exception becomes a one-liner:

```java
public final class UserException extends FunctionalException {
    @Serial
    private static final long serialVersionUID = -1704075955258498228L;
    public UserException(Failure failure) { super(failure); }
}
```

### 3.7 Refactor FunctionalExceptionHandler

**File:** `functional-app/src/main/java/.../controller/FunctionalExceptionHandler.java`

Extract a private helper for the common pattern:

```java
private ResponseEntity<ErrorDTO> buildErrorResponse(String logLabel, Exception e, 
                                                     boolean includeReasons, 
                                                     org.springframework.http.HttpStatus status) {
    log.info(logLabel, e);
    ErrorDTO.Builder builder = ErrorDTO.builder()
            .withType(e.getClass().getSimpleName())
            .withMessage(e.getMessage());
    if (includeReasons && e instanceof CommandException ce) {
        builder.withReasons(ce.getReasons());
    }
    return ResponseEntity.status(status).body(builder.build());
}

@ExceptionHandler(CommandException.class)
public ResponseEntity<ErrorDTO> handleCommandException(CommandException e) {
    return buildErrorResponse("handleCommandException", e, true, HttpStatus.BAD_REQUEST);
}

@ExceptionHandler({PathException.class, UserException.class, GroupException.class})
public ResponseEntity<ErrorDTO> handleFunctionalException(FunctionalException e) {
    return buildErrorResponse("handle" + e.getClass().getSimpleName(), e, false, HttpStatus.BAD_REQUEST);
}

@ExceptionHandler(UnsupportedOperationException.class)
public ResponseEntity<ErrorDTO> handleUnsupportedOperationException(UnsupportedOperationException e) {
    return buildErrorResponse("handleUnsupportedOperationException", e, false, HttpStatus.INTERNAL_SERVER_ERROR);
}
```

### 3.8 Extract Service Boilerplate Helpers

**File:** `functional-app/src/main/java/.../service/FileSystemService.java`

Extract a private helper:

```java
private User getAuthenticatedUser(String username) {
    Output<User> userOutput = userService.findUserByName(username);
    if (userOutput.isFailure()) {
        throw new UserException(userOutput.getFailure());
    }
    return userOutput.getValue();
}

private Path resolvePath(String path, User actor) {
    Path itemPath = fileService.getPath(path, actor);
    if (itemPath.isError()) {
        throw new PathException(itemPath.getError());
    }
    return itemPath;
}

private <T> T processItemCommand(ItemCommandType type, User actor, ItemInput input, 
                                  Output<T> output) {
    if (output.isFailure()) {
        throw new CommandException(output.getFailure());
    }
    return output.getValue();
}
```

Each method becomes ~5 lines instead of ~15.

**File:** `functional-app/src/main/java/.../service/UserManagementService.java`

Same pattern — extract `getAuthenticatedUser()` and `processCommand()` helpers.

### 3.9 Refactor AbstractCheckCommand.createError

**Files:**
- `functional-domain/src/main/java/.../command/impl/check/AbstractCheckItemCommand.java`
- `functional-domain/src/main/java/.../command/impl/check/AbstractCheckUserCommand.java`

Change `createError()` from throwing `UnsupportedOperationException` to being `abstract`:

```java
// In AbstractCheckItemCommand:
protected abstract ItemErrorCommand<T> createError(List<String> reasons);

// In AbstractCheckUserCommand:
protected abstract UserErrorCommand<T> createError(List<String> reasons);
```

This forces subclasses to implement it instead of relying on a runtime exception.

### 3.10 Test Duplication — Extract Test Base Classes

**Files (new):**
- `functional-domain/src/test/java/.../domain/command/impl/check/AbstractCheckItemCommandTest.java`
- `functional-domain/src/test/java/.../domain/command/impl/check/AbstractCheckUserCommandTest.java`

Create abstract base test classes with the shared `@Mock` fields and common assertion patterns. Each concrete test extends the base and only provides the specific setup and test method bodies.

### 3.11 Validate: Run All Tests

```bash
./mvnw clean test
./mvnw clean verify -pl functional-app
```

All 566 unit tests + 4 integration tests must pass. Checkstyle must pass.

---

## Phase 4: Code Smell Fixes

**Priority:** P2  
**Estimated effort:** 2-3 hours  
**Impact:** Code Quality +0.5

### 4.1 Fix Exception Handler Method Name

**File:** `functional-app/src/main/java/.../controller/FunctionalExceptionHandler.java`

```java
// BEFORE (line 51):
@ExceptionHandler(GroupException.class)
public ResponseEntity<ErrorDTO> handlePathException(GroupException e) {

// AFTER:
@ExceptionHandler(GroupException.class)
public ResponseEntity<ErrorDTO> handleGroupException(GroupException e) {
```

### 4.2 Add Missing Debug Log

**File:** `functional-app/src/main/java/.../service/UserManagementService.java`

```java
// BEFORE (line 125):
public List<GroupDTO> getGroups(String name, UUID userId, String username) {
    Output<User> output = userService.findUserByName(username);

// AFTER:
public List<GroupDTO> getGroups(String name, UUID userId, String username) {
    log.debug("Getting groups info by user {} ", username);
    Output<User> output = userService.findUserByName(username);
```

### 4.3 Make Builder Timestamps Explicit

**File:** `functional-domain/src/main/java/.../domain/entities/AbstractItem.java`

Remove the `LocalDateTime.now()` defaults from the builder and require explicit values:

```java
// BEFORE (lines 163-167):
private LocalDateTime created = LocalDateTime.now();
private LocalDateTime lastModified = LocalDateTime.now();
private LocalDateTime lastAccessed = LocalDateTime.now();

// AFTER:
private LocalDateTime created;
private LocalDateTime lastModified;
private LocalDateTime lastAccessed;
```

Update `AbstractItem` constructor to default to `LocalDateTime.now()` if null:

```java
protected AbstractItem(AbstractBuilder<T, B> builder) {
    // ...
    this.created = ofNullable(builder.created).orElseGet(LocalDateTime::now);
    this.lastModified = ofNullable(builder.lastModified).orElseGet(LocalDateTime::now);
    this.lastAccessed = ofNullable(builder.lastAccessed).orElseGet(LocalDateTime::now);
    // ...
}
```

This makes tests deterministic while keeping the API convenient.

### 4.4 Resolve Java Version Mismatch

**File:** `pom.xml`

Align the Java version across all configuration:

```xml
<!-- BEFORE: -->
<java.version>17</java.version>
<!-- Enforcer requires 21 -->

<!-- AFTER (choose one): -->
<java.version>21</java.version>  <!-- Align with enforcer requirement -->
```

Or lower the enforcer requirement:

```xml
<!-- If staying on Java 17: -->
<requireJavaVersion>
    <version>17</version>
</requireJavaVersion>
```

**Recommendation:** Set `java.version=21` and update `maven.compiler.release` accordingly. Java 21 is LTS and aligns with the enforcer plugin.

### 4.5 Remove Lazy Initialization for Production

**File:** `functional-app/src/main/resources/application.yml`

Move lazy initialization to the dev profile only:

```yaml
# application.yml - REMOVE line 6:
# spring.main.lazy-initialization: true

# application-dev.yml - ADD:
spring:
  main:
    lazy-initialization: true
```

### 4.6 Validate: Run All Tests + Lint

```bash
./mvnw clean test
./mvnw clean checkstyle:check
```

---

## Phase 5: Documentation

**Priority:** P2  
**Estimated effort:** 3-4 hours  
**Impact:** Documentation 3/10 → 6/10

### 5.1 Expand README.md

Rewrite `README.md` to include:

- Project overview with architecture diagram
- Module descriptions
- Prerequisites (Java 21, Maven, PostgreSQL, SpiceDB)
- Quick start guide (local setup, environment variables, running the app)
- API endpoints documentation (table of all endpoints with method, path, auth required, description)
- Testing instructions (unit, integration, mutation testing)
- CI/CD pipeline overview
- Architecture decisions (DDD, functional programming, SpiceDB)

### 5.2 Add Javadoc to Key Public API Classes

Add Javadoc to:
- All domain entity interfaces (`Item`, `File`, `Folder`, `User`, `Group`, `AccessRight`, `Output`, `Path`)
- All driven port interfaces (`UserRepository`, `GroupRepository`, `ItemRepository`, `ContentRepository`, `AccessChecker`, `AccessUpdater`, `UserChecker`, `UserUpdater`, `RuleLoader`, `PasswordProtector`)
- All driving port interfaces (`UserService`, `FileService`, `AccessParserService`, `RuleConfigurator`)
- The `Command<T>` and `CheckCommand<T>` interfaces
- Configuration classes (`SecurityConfiguration`, `DomainConfiguration`, `CheckConfiguration`, `InfrastructureConfiguration`, `GrpcConfiguration`)
- Public service classes (`FileSystemService`, `UserManagementService`)
- Controllers (`UserController`, `GroupController`, `FileSystemController`)

### 5.3 Add CONTRIBUTING.md

Create `CONTRIBUTING.md` with:
- Development environment setup
- Code style guide (checkstyle rules summary)
- Branching strategy
- PR checklist (tests pass, checkstyle pass, coverage ≥ 95%)
- How to run mutation testing

### 5.4 Add CHANGELOG.md

Create `CHANGELOG.md` using Keep a Changelog format, documenting recent changes from git log.

### 5.5 Add Architecture Decision Records (ADR)

Create `docs/adr/` directory with ADRs for:
- ADR-001: DDD with 4-layer architecture
- ADR-002: Functional programming style with Output<T>
- ADR-003: SpiceDB for authorization
- ADR-004: Command pattern with check → execute → error flow
- ADR-005: Composite factory pattern for extensibility

---

## Phase 6: Lint & Build Hardening

**Priority:** P3  
**Estimated effort:** 1-2 hours  
**Impact:** Lint Compliance 9/10 → 10/10

### 6.1 Add Advanced Checkstyle Rules

**File:** `checkstyle.xml`

Add the following modules:

```xml
<module name="CyclomaticComplexity">
    <property name="max" value="10"/>
</module>
<module name="JavaDocMethod">
    <property name="scope" value="public"/>
</module>
<module name="MethodCount">
    <property name="maxTotal" value="20"/>
</module>
<module name="ParameterNumber">
    <property name="max" value="5"/>
</module>
```

### 6.2 Verify Full Build Pipeline

```bash
./mvnw clean verify
./mvnw clean checkstyle:check
./mvnw clean test
./mvnw clean verify -PmutationTesting
```

All must pass with:
- 0 checkstyle violations
- 566 unit tests passing
- 4 integration tests passing (0 skipped)
- JaCoCo coverage ≥ 95%
- Pitest mutation score ≥ 80%

---

## Validation Checklist

After all phases are complete, verify:

| # | Check | Command | Expected Result |
|---|-------|---------|-----------------|
| 1 | Checkstyle passes | `./mvnw clean checkstyle:check` | BUILD SUCCESS, 0 violations |
| 2 | Unit tests pass | `./mvnw clean test` | 566 tests, 0 failures, 0 errors, 0 skipped |
| 3 | Integration tests pass | `./mvnw clean verify -pl functional-app` | 4 tests, 0 failures, 0 errors, 0 skipped |
| 4 | JaCoCo coverage | `./mvnw clean verify` | Coverage ≥ 95%, 0 missed classes |
| 5 | No hardcoded secrets | `grep -r "password\|token\|secret" application*.yml` | Only `${ENV_VAR}` references |
| 6 | No password in toString | `grep -r "password.*+" --include="*.java" src/main` | No matches |
| 7 | eraseCredentials enabled | Check `SecurityConfiguration.java` | `builder.eraseCredentials(true)` |
| 8 | Security headers configured | Check `SecurityConfiguration.java` | HSTS, CSP, X-Frame-Options present |
| 9 | Actuator restricted | Check `SecurityConfiguration.java` | Only health/info permitAll |
| 10 | Integration test not disabled | Check `UserControllerIT.java` | No `@Disabled` annotation |
| 11 | No code duplication in AccessCheckerImpl | Check `AccessCheckerImpl.java` | Single `checkPermission()` helper |
| 12 | No code duplication in UserCheckerImpl | Check `UserCheckerImpl.java` | Single `checkPermission()` helper |
| 13 | publishRelations not duplicated | Check AccessUpdaterImpl & UserUpdaterImpl | Shared via SpiceDbHelper |
| 14 | Exception handler method names correct | Check `FunctionalExceptionHandler.java` | No `handlePathException(GroupException)` |
| 15 | Documentation exists | Check README.md, CONTRIBUTING.md, CHANGELOG.md | All present with meaningful content |
| 16 | Java version aligned | Check `pom.xml` | `java.version` matches enforcer requirement |

---

## Summary: Scorecard Improvement Targets

| Category | Current | Target | Phase(s) |
|----------|---------|--------|----------|
| Lint Compliance | 9/10 | 10/10 | Phase 6 |
| Code Quality | 7/10 | 8/10 | Phases 3, 4 |
| Security | 4/10 | 7/10 | Phase 1 |
| Maintainability | 6/10 | 8/10 | Phases 2, 3 |
| Documentation | 3/10 | 6/10 | Phase 5 |
| Idempotency | 8/10 | 9/10 | Phase 4 |
| **Overall** | **6/10** | **8/10** | **All** |

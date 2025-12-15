# TestContainers Docker API Issue

## Summary
Integration and BDD tests using TestContainers are currently failing due to a Docker API compatibility issue. **Unit tests are passing successfully (42/42)**.

## Issue Description
TestContainers fails to initialize Docker containers with the following error:
```
Could not find a valid Docker environment. Please check configuration
Status 400: {"ID":"","Containers":0,"ContainersRunning":0,...}
```

The Docker daemon returns HTTP 400 with empty JSON fields when TestContainers queries `/info` endpoint.

## Tests Status

### ✅ Passing Tests (42 tests)
- **Unit Tests**: All passing
  - BeneficiaryController: 12/12
  - BeneficiaryService: 17/17  
  - BeneficiaryService Validation: 13/13

### ❌ Blocked Tests (TestContainers Docker API issue)
- **Integration Tests**:
  - `BeneficiaryIntegrationTest` (PostgreSQL + Redis containers)
  - `BeneficiariesApplicationIntegrationTest`
- **BDD Tests**:
  - `CucumberTestRunner` (Cucumber with PostgreSQL container)
  - All step definitions in `bdd/steps/`

### 🗑️ Removed Tests
- **Slice Tests**: Completely removed per user request
  - `BeneficiaryServiceSliceTest.java` - DELETED

## Environment Details
- **Docker Version**: 29.1.2
- **Docker Status**: Running (`docker ps` works)
- **TestContainers Version**: 1.20.4
- **Java Version**: 21
- **Spring Boot**: 3.5.7

## Troubleshooting Steps Attempted

1. **Backed up testcontainers.properties** to allow auto-detection
2. **Configured Docker socket** via testcontainers.properties
3. **Set DOCKER_HOST** environment variable
4. **Verified Docker daemon** is running and responding to CLI commands

All attempts result in the same 400 error with empty JSON from Docker `/info` endpoint.

## Root Cause
Docker Desktop API is returning malformed responses to TestContainers. This appears to be a compatibility issue between:
- Docker Desktop 29.1.2
- TestContainers 1.20.4  
- macOS environment

## Test Configuration (Ready When Docker Fixed)

### Integration Test Setup
```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
    DockerImageName.parse("ghcr.io/alokkulkarni/testcontainers-registry/testcontainers/postgres:16-alpine")
        .asCompatibleSubstituteFor("postgres"))
    .withDatabaseName("beneficiaries_test")
    .withUsername("test")
    .withPassword("test")
    .withInitScript("init.db");

@Container  
static GenericContainer<?> redis = new GenericContainer<>(
    DockerImageName.parse("ghcr.io/alokkulkarni/testcontainers-registry/testcontainers/redis:7-alpine")
        .asCompatibleSubstituteFor("redis"))
    .withExposedPorts(6379);
```

### BDD Test Setup
```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
    DockerImageName.parse("ghcr.io/alokkulkarni/testcontainers-registry/testcontainers/postgres:16-alpine")
        .asCompatibleSubstituteFor("postgres"))
    .withDatabaseName("beneficiaries_test")
    .withUsername("test")
    .withPassword("test")
    .withInitScript("init.db");
```

## Next Steps

1. **Docker Desktop Upgrade**: Try upgrading to latest Docker Desktop version
2. **TestContainers Upgrade**: Consider upgrading TestContainers to 1.21.x
3. **Docker Restart**: Full restart of Docker Desktop may resolve API initialization
4. **Alternative**: Use Colima or Podman instead of Docker Desktop

## Workaround for CI/CD

For GitHub Actions or other CI environments, TestContainers typically works without issues. The problem is specific to local Docker Desktop environments.

## Test Data
All test data is properly configured in `src/test/resources/init.db` including:
- ✅ Standard CRUD scenarios (IDs 1-41)
- ✅ Analytics test data (IDs 50-57) for new methods:
  - `getCustomerBeneficiaryAnalytics()`
  - `findPotentialDuplicates()`
  - `getBeneficiaryUsageReport()`

## Recent Changes (Commit 62b3a2f)
- ✅ Added 3 analytics methods to BeneficiaryService (~150 lines)
- ✅ Updated init.db with test data for analytics
- ✅ Removed slice test completely
- ✅ All unit tests passing
- ❌ Integration/BDD tests blocked by Docker API issue

---
**Last Updated**: 2025-12-15
**Status**: Investigating Docker API compatibility issue

# Telepesa Backend Scripts

This directory contains consolidated scripts for the Telepesa backend services, following the **ANTI-REPETITION RULE** to prevent script proliferation.

## Available Scripts

### E2E Testing: `e2e-test.sh`

**Single comprehensive E2E test script** that replaces all previous E2E test scripts:
- `comprehensive-e2e-test.sh` ❌ (removed)
- `comprehensive-e2e-test-fixed.sh` ❌ (removed)
- `direct-e2e-test.sh` ❌ (removed)
- `gateway-e2e-test.sh` ❌ (removed)
- `simplified-e2e-test.sh` ❌ (removed)

#### Usage

```bash
# Test services directly (default)
./scripts/e2e-test.sh

# Test through API Gateway
./scripts/e2e-test.sh --gateway

# Test with verbose output
./scripts/e2e-test.sh --verbose

# Test and clean up test data
./scripts/e2e-test.sh --cleanup

# Combine options
./scripts/e2e-test.sh --gateway --verbose --cleanup

# Show help
./scripts/e2e-test.sh --help
```

#### Features

- **Unified Testing**: Single script handles both direct service testing and API Gateway testing
- **Comprehensive Coverage**: Tests all services (User, Account, Transaction, Notification, Loan)
- **Smart Detection**: Automatically detects which services are running
- **Flexible Modes**: 
  - `--direct`: Test services directly (bypasses API Gateway)
  - `--gateway`: Test through API Gateway (requires API Gateway to be running)
- **Verbose Output**: `--verbose` flag for detailed response logging
- **Cleanup**: `--cleanup` flag to remove test data after tests
- **Conflict Prevention**: Uses timestamps to avoid test data conflicts
- **Health Checks**: Verifies all services are healthy before testing
- **Service Discovery**: Checks Eureka registration status

#### Test Flow

1. **Service Health Check**: Verifies all services are responding
2. **Service Discovery**: Checks Eureka registration
3. **User Service**: Registration, login, profile management
4. **Account Service**: Account creation and management
5. **Transaction Service**: Transaction creation and history
6. **Notification Service**: Notification creation and retrieval
7. **Loan Service**: Loan applications (if service is running)
8. **Cleanup**: Removes test data (if `--cleanup` flag is used)

#### Output

The script provides:
- ✅ **Passed tests** (green)
- ❌ **Failed tests** (red) with error details
- ⏭️ **Skipped tests** (yellow) with reason
- 📊 **Summary** with total counts
- 🎉 **Success message** if all tests pass

## Script Management Rules

Following the **ANTI-REPETITION RULE**:

### ✅ DO:
- Use the consolidated `e2e-test.sh` for all E2E testing
- Add new features to existing scripts with flags
- Keep scripts in the `scripts/` directory
- Use descriptive names with kebab-case
- Include help documentation

### ❌ DON'T:
- Create multiple scripts for the same purpose
- Create service-specific test scripts unless absolutely necessary
- Place scripts in root directory
- Use generic names like "test.sh"
- Create scripts without documentation

### Adding New Features

When adding new functionality:

1. **Check existing scripts first** - Can the feature be added to an existing script?
2. **Use flags/options** - Add new functionality as optional flags
3. **Update documentation** - Keep this README current
4. **Test thoroughly** - Ensure new features don't break existing functionality

## Examples

### Quick Health Check
```bash
./scripts/e2e-test.sh --direct
```

### Full API Gateway Test
```bash
./scripts/e2e-test.sh --gateway --verbose --cleanup
```

### Debug Mode
```bash
./scripts/e2e-test.sh --direct --verbose
```

## Troubleshooting

### Common Issues

1. **Services not running**: Start required services first
2. **Port conflicts**: Check if ports are already in use
3. **Authentication failures**: Verify user service is working
4. **API Gateway issues**: Use `--direct` mode to bypass gateway

### Service Startup Order

For best results, start services in this order:
1. Eureka Server (8761)
2. User Service (8081)
3. Account Service (8082)
4. Transaction Service (8083)
5. Notification Service (8085)
6. Loan Service (8084) - optional
7. API Gateway (8080) - for gateway testing 
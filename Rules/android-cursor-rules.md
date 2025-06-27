# Telepesa: Android Project Development Rules

These rules are based on best practices from a well-structured Android project using modern development patterns.

## Project Structure & Architecture

### Modular Architecture
- Use multi-module architecture with clear separation of concerns
- Follow this module structure:
  - `app/` - Main application module
  - `core/` - Core modules (analytics, common, data, database, datastore, designsystem, domain, model, network, notifications, security, testing, ui)
  - `feature/` - Feature-specific modules (accounts, calculator, home, login, services, etc.)
  - `build-logic/` - Build convention plugins
  - `sync/` - Background sync operations
  - `benchmarks/` - Performance benchmarks
  - `lint/` - Custom lint rules

### Package Naming
- Use reverse domain naming: `com.{company}.{project}.{module}.{feature}`
- Example: `com.maelcolium.telepesa.feature.login`
- Core modules: `com.maelcolium.telepesa.core.{module_name}`

## Build Configuration

### Version Catalog
- Always use Gradle version catalogs (`gradle/libs.versions.toml`)
- Group dependencies logically in the catalog
- Use alias references in build files: `alias(libs.plugins.android.application)`

### Convention Plugins
- Create custom convention plugins in `build-logic/convention/`
- Use convention plugins for consistent module configuration:
  - `telepesa.android.application`
  - `telepesa.android.library`
  - `telepesa.android.feature`
  - `telepesa.android.library.compose`
  - `telepesa.android.hilt`
  - `telepesa.android.room`

### Build Files
- Use Kotlin DSL (`.gradle.kts`) for all build files
- Apply convention plugins for consistent configuration
- Use typed project accessors: `implementation(projects.core.common)`

## Code Organization

### Feature Modules
- Each feature should be a separate module
- Features should depend on core modules, not other features
- Include these standard dependencies in feature modules:
  ```kotlin
  implementation(projects.core.model)
  implementation(projects.core.ui)
  implementation(projects.core.designsystem)
  implementation(projects.core.data)
  implementation(projects.core.common)
  implementation(projects.core.domain)
  implementation(projects.core.analytics)
  ```

### Core Modules
- `core:model` - Data models and DTOs
- `core:data` - Repository implementations and data layer
- `core:database` - Room database setup
- `core:datastore` - DataStore preferences
- `core:network` - API clients and network layer
- `core:common` - Shared utilities and constants
- `core:ui` - Shared UI components
- `core:designsystem` - Design system components
- `core:domain` - Use cases and business logic
- `core:testing` - Test utilities and fake implementations

## UI & Compose

### Compose Conventions
- Use `@Composable` functions for UI components
- Follow naming convention: `ComponentNameScreen` for screens, `ComponentName` for reusable components
- Use state hoisting pattern
- Implement proper preview functions

### Resource Naming
- Use resource prefixes based on module name
- Example: `core_designsystem_` prefix for designsystem module resources
- Follow naming: `{module}_{type}_{name}`

## Dependency Injection

### Hilt Setup
- Use Hilt for dependency injection
- Apply `@HiltViewModel` to ViewModels
- Use `@Inject` constructor injection
- Create Hilt modules for binding interfaces

## Data Layer

### Repository Pattern
- Create repository interfaces in `core:domain`
- Implement repositories in `core:data`
- Use `@Inject` constructor for repository dependencies
- Follow naming: `{Feature}Repository` interface, `{Feature}RepositoryImpl` implementation
- Example structure:
  ```kotlin
  // In core:domain
  interface AuthenticationRepository {
      suspend fun login(request: LoginRequest): Flow<LoginResponse>
      suspend fun logout(): Flow<Unit>
  }
  
  // In core:data
  class AuthenticationRepositoryImpl @Inject constructor(
      private val apiService: AuthApiService,
      private val dataStore: UserPreferencesDataSource
  ) : AuthenticationRepository {
      
      override suspend fun login(request: LoginRequest): Flow<LoginResponse> {
          return flow {
              emit(apiService.login(request))
          }.flowOn(Dispatchers.IO)
      }
  }
  ```

### Dependency Injection Setup
- Bind repository interfaces to implementations in Hilt modules
- Use `@Binds` for interface binding
- Example:
  ```kotlin
  @Module
  @InstallIn(SingletonComponent::class)
  interface DataModule {
      
      @Binds
      fun bindsAuthenticationRepository(
          authenticationRepository: AuthenticationRepositoryImpl
      ): AuthenticationRepository
  }
  ```

### Result Handling
- Use a Result wrapper for API responses
- Implement `.asResult()` extension for Flow transformation
- Handle Loading, Success, and Error states consistently
- Example Result implementation:
  ```kotlin
  sealed interface Result<out T> {
      data class Success<T>(val data: T) : Result<T>
      data class Error(val exception: Throwable) : Result<Nothing>
      object Loading : Result<Nothing>
  }
  
  fun <T> Flow<T>.asResult(): Flow<Result<T>> {
      return this
          .map<T, Result<T>> { Result.Success(it) }
          .onStart { emit(Result.Loading) }
          .catch { emit(Result.Error(it)) }
  }
  ```

### API Call Patterns
- Use suspend functions for single API calls
- Use Flow for reactive data streams
- Handle exceptions at the repository level
- Example:
  ```kotlin
  override suspend fun getUserAccounts(): Flow<List<UserAccount>> {
      return flow {
          try {
              val response = apiService.getUserAccounts()
              if (response.header.responseCode == 200) {
                  emit(response.body ?: emptyList())
              } else {
                  throw ApiException(response.header.responseMessage)
              }
          } catch (e: Exception) {
              throw e.updatedThrowable()
          }
      }.flowOn(Dispatchers.IO)
  }
  ```

### Data Transformation
- Transform network models to domain models in repositories
- Use extension functions for mapping
- Cache data when appropriate
- Example:
  ```kotlin
  override suspend fun getNotifications(): Flow<List<Notification>> {
      return apiService.getNotifications()
          .map { response -> 
              response.body?.map { it.toDomainModel() } ?: emptyList()
          }
          .flowOn(Dispatchers.IO)
  }
  
  private fun NotificationDto.toDomainModel(): Notification {
      return Notification(
          id = this.id,
          title = this.title,
          message = this.message,
          timestamp = this.timestamp.toLocalDateTime()
      )
  }
  ```

### Error Handling
- Create custom exception types for different error scenarios
- Map HTTP errors to domain-specific errors
- Provide meaningful error messages
- Example:
  ```kotlin
  class ApiException(message: String) : Exception(message)
  class NetworkException(message: String) : Exception(message)
  class AuthenticationException(message: String) : Exception(message)
  
  fun Throwable.updatedThrowable(): Throwable {
      return when (this) {
          is HttpException -> {
              when (code()) {
                  401 -> AuthenticationException("Authentication failed")
                  404 -> ApiException("Resource not found")
                  else -> ApiException("Server error: ${message()}")
              }
          }
          is IOException -> NetworkException("Network error: ${message}")
          else -> this
      }
  }
  ```

### Caching Strategy
- Use Room database for local caching
- Implement cache-first or network-first strategies
- Use DataStore for preferences and simple data
- Example:
  ```kotlin
  override suspend fun getUserData(): Flow<UserData> {
      return combine(
          localDataSource.getUserData(),
          networkDataSource.getUserData().asResult()
      ) { local, network ->
          when (network) {
              is Result.Success -> {
                  localDataSource.saveUserData(network.data)
                  network.data
              }
              is Result.Error -> local ?: throw network.exception
              is Result.Loading -> local ?: UserData()
          }
      }
  }
  ```

### Data Models
- Separate network DTOs from domain models
- Use `@Serializable` for JSON serialization
- Follow naming: `{Model}Response` for API responses, `{Model}Request` for API requests
- Example:
  ```kotlin
  // Network DTO
  @Serializable
  data class LoginResponse(
      @SerialName("accessToken") val accessToken: String,
      @SerialName("refreshToken") val refreshToken: String,
      @SerialName("user") val user: UserDto
  )
  
  // Domain Model
  data class LoginResult(
      val accessToken: String,
      val refreshToken: String,
      val user: User
  )
  ```

### Repository Testing
- Create test implementations for repositories
- Use fake data in test repositories
- Follow the same interface contract
- Example:
  ```kotlin
  class TestAuthenticationRepository : AuthenticationRepository {
      
      private var shouldReturnError = false
      
      fun setShouldReturnError(value: Boolean) {
          shouldReturnError = value
      }
      
      override suspend fun login(request: LoginRequest): Flow<LoginResponse> {
          return flow {
              if (shouldReturnError) {
                  throw Exception("Test error")
              } else {
                  emit(LoginResponse("test_token", "refresh_token", testUser))
              }
          }
      }
  }
  ```

## Testing

### Test Structure
- Include test modules: `core:testing`, `core:data-test`, `core:datastore-test`
- Create fake implementations in testing modules
- Use consistent test naming without "test" prefix
- Organize tests by feature and layer (unit, integration, UI)

### Test Naming Convention
- Unit tests: `methodName_condition_expectedResult`
- Android tests: `given_when_then` or `when_then` format
- Example: `validateOtp_handlesResultStatesProperly`

### Test Setup
- Use `@get:Rule val mainDispatcherRule = MainDispatcherRule()`
- Create test repositories extending production interfaces
- Use `runTest` for coroutine testing

### Unit Testing Best Practices

#### Test Structure (AAA Pattern)
- **Arrange**: Set up test data and mocks
- **Act**: Execute the method under test
- **Assert**: Verify the results
- Example:
  ```kotlin
  @Test
  fun login_withValidCredentials_emitsSuccessState() = runTest {
      // Arrange
      val expectedUser = User(id = "123", name = "John")
      val repository = TestAuthenticationRepository()
      repository.setLoginResult(Result.Success(expectedUser))
      val viewModel = LoginViewModel(repository)
      
      // Act
      viewModel.login("user@example.com", "password")
      
      // Assert
      assertEquals(LoginUiState.Success, viewModel.loginUiState.value)
  }
  ```

#### Mocking and Test Doubles
- Use test implementations over mocking frameworks when possible
- Create fake repositories for consistent behavior
- Use `mockk` for complex scenarios requiring behavior verification
- Example:
  ```kotlin
  class TestUserRepository : UserRepository {
      private var users = mutableListOf<User>()
      private var shouldThrowError = false
      
      fun setError(shouldThrow: Boolean) {
          shouldThrowError = shouldThrow
      }
      
      override suspend fun getUser(id: String): User {
          if (shouldThrowError) throw Exception("Network error")
          return users.find { it.id == id } ?: throw UserNotFoundException()
      }
      
      override suspend fun saveUser(user: User) {
          users.add(user)
      }
  }
  ```

#### Test Data Builders
- Use builder pattern for complex test data
- Create reusable test data factories
- Example:
  ```kotlin
  class UserTestDataBuilder {
      private var id = "default-id"
      private var name = "Default Name"
      private var email = "default@example.com"
      
      fun withId(id: String) = apply { this.id = id }
      fun withName(name: String) = apply { this.name = name }
      fun withEmail(email: String) = apply { this.email = email }
      
      fun build() = User(id = id, name = name, email = email)
  }
  
  // Usage
  val testUser = UserTestDataBuilder()
      .withId("123")
      .withName("John Doe")
      .build()
  ```

#### Testing Flows and StateFlow
- Test reactive streams properly
- Use `turbine` library for Flow testing
- Example:
  ```kotlin
  @Test
  fun userDataFlow_emitsUpdatedData() = runTest {
      repository.userData.test {
          // Initial state
          assertEquals(UserData.Empty, awaitItem())
          
          // Update data
          repository.updateUser(testUser)
          assertEquals(UserData(user = testUser), awaitItem())
          
          cancelAndIgnoreRemainingEvents()
      }
  }
  ```

#### Testing Coroutines
- Use `runTest` for coroutine testing
- Test different coroutine scenarios (success, failure, cancellation)
- Example:
  ```kotlin
  @Test
  fun loadUserData_cancellation_doesNotUpdateState() = runTest {
      val job = launch {
          viewModel.loadUserData()
      }
      
      // Cancel before completion
      job.cancel()
      
      // State should remain unchanged
      assertEquals(UiState.Idle, viewModel.uiState.value)
  }
  ```

#### Testing Error Scenarios
- Test all error paths and edge cases
- Verify error messages and recovery mechanisms
- Example:
  ```kotlin
  @Test
  fun login_withNetworkError_emitsErrorState() = runTest {
      // Arrange
      repository.setError(true)
      
      // Act
      viewModel.login("user@example.com", "password")
      
      // Assert
      val state = viewModel.loginUiState.value
      assertTrue(state is LoginUiState.Error)
      assertEquals("Network error", (state as LoginUiState.Error).message)
  }
  ```

#### Test Coverage Guidelines
- Aim for 80%+ code coverage on business logic
- Focus on testing critical paths and edge cases
- Don't test framework code or simple getters/setters
- Test both positive and negative scenarios

#### Parameterized Tests
- Use `@ParameterizedTest` for testing multiple scenarios
- Example:
  ```kotlin
  @ParameterizedTest
  @ValueSource(strings = ["", " ", "invalid-email", "@domain.com"])
  fun validateEmail_withInvalidEmails_returnsFalse(email: String) {
      assertFalse(emailValidator.isValid(email))
  }
  ```

### Integration Testing
- Test interaction between components
- Use TestRule for database and network testing
- Test repository implementations with real dependencies
- Example:
  ```kotlin
  @Test
  fun userRepository_saveAndRetrieve_worksCorrectly() = runTest {
      val user = UserTestDataBuilder().build()
      
      repository.saveUser(user)
      val retrievedUser = repository.getUser(user.id)
      
      assertEquals(user, retrievedUser)
  }
  ```

### UI Testing with Compose
- Use `createComposeRule()` for Compose UI tests
- Test user interactions and state changes
- Example:
  ```kotlin
  @Test
  fun loginScreen_validInput_enablesLoginButton() {
      composeTestRule.setContent {
          LoginScreen(
              onLogin = {},
              uiState = LoginUiState.Idle
          )
      }
      
      composeTestRule.onNodeWithText("Email").performTextInput("user@example.com")
      composeTestRule.onNodeWithText("Password").performTextInput("password123")
      
      composeTestRule.onNodeWithText("Login").assertIsEnabled()
  }
  ```

### Test Organization
- Group related tests in inner classes
- Use descriptive test class names
- Separate unit tests from integration tests
- Example:
  ```kotlin
  class LoginViewModelTest {
      
      @Nested
      inner class LoginFunction {
          @Test
          fun withValidCredentials_emitsSuccessState() { ... }
          
          @Test
          fun withInvalidCredentials_emitsErrorState() { ... }
      }
      
      @Nested
      inner class StateManagement {
          @Test
          fun resetState_resetsToIdle() { ... }
      }
  }
  ```

## ViewModels

### ViewModel Structure
- Use `@HiltViewModel` annotation
- Inject dependencies via constructor
- Use `StateFlow` for state management
- Use `private val _state = MutableStateFlow()` and `val state = _state.asStateFlow()`
- Initialize in `init` block when needed
- Example structure:
  ```kotlin
  @HiltViewModel
  class LoginViewModel @Inject constructor(
      private val authRepository: AuthenticationRepository,
      private val userDataRepository: UserDataRepository
  ) : ViewModel() {
      
      private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
      val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()
      
      init {
          // Initialize state if needed
      }
  }
  ```

### State Management
- Create sealed classes/interfaces for UI states
- Use consistent state naming: `{Feature}UiState`
- Include Loading, Success, Error, and Idle states
- Example:
  ```kotlin
  sealed interface LoginUiState {
      object Idle : LoginUiState
      object Loading : LoginUiState
      object Success : LoginUiState
      data class Error(val message: String) : LoginUiState
  }
  ```

### StateFlow Patterns
- Use `SharingStarted.WhileSubscribed(5_000)` for UI-driven flows
- Use `SharingStarted.Eagerly` for critical app state
- Transform repository flows with `.map()` and `.stateIn()`
- Example:
  ```kotlin
  val uiState: StateFlow<UiState> = repository.data
      .map { UiState.Success(it) }
      .stateIn(
          scope = viewModelScope,
          started = SharingStarted.WhileSubscribed(5_000),
          initialValue = UiState.Loading
      )
  ```

### Coroutine Management
- Use `viewModelScope.launch` for fire-and-forget operations
- Use `viewModelScope.launch` with proper exception handling
- Use `Mutex` for thread-safe operations when needed
- Example:
  ```kotlin
  private val saveMutex = Mutex()
  
  fun saveData() {
      viewModelScope.launch {
          saveMutex.withLock {
              try {
                  _state.value = UiState.Loading
                  val result = repository.saveData()
                  _state.value = UiState.Success(result)
              } catch (e: Exception) {
                  _state.value = UiState.Error(e.message ?: "Unknown error")
              }
          }
      }
  }
  ```

### Input Handling
- Use `mutableStateOf` for simple form inputs
- Provide update functions for each input field
- Use `by` delegate for cleaner syntax
- Example:
  ```kotlin
  var userName by mutableStateOf("")
      private set
  var password by mutableStateOf("")
      private set
      
  fun updateUserName(value: String) {
      userName = value
  }
  
  fun updatePassword(value: String) {
      password = value
  }
  ```

### State Reset
- Provide reset methods for UI states
- Call reset methods after navigation or on success
- Example:
  ```kotlin
  fun resetLoginState() {
      _loginUiState.value = LoginUiState.Idle
  }
  ```

### Error Handling
- Handle Repository Result types properly
- Map exceptions to user-friendly messages
- Use consistent error state structure
- Example:
  ```kotlin
  fun login() {
      viewModelScope.launch {
          repository.login(userName, password)
              .asResult()
              .collect { result ->
                  _loginUiState.value = when (result) {
                      is Result.Loading -> LoginUiState.Loading
                      is Result.Success -> LoginUiState.Success
                      is Result.Error -> LoginUiState.Error(result.exception.message ?: "Login failed")
                  }
              }
      }
  }
  ```

## SOLID Principles

### Single Responsibility Principle (SRP)
- Each class should have only one reason to change
- ViewModels should only handle UI state management
- Repositories should only handle data operations
- Example:
  ```kotlin
  // Good: Single responsibility
  class UserRepository {
      suspend fun getUser(id: String): User { ... }
      suspend fun updateUser(user: User): User { ... }
  }
  
  class UserValidator {
      fun validateEmail(email: String): Boolean { ... }
      fun validatePhoneNumber(phone: String): Boolean { ... }
  }
  
  // Avoid: Multiple responsibilities
  class UserManager {
      suspend fun getUser(id: String): User { ... }
      fun validateEmail(email: String): Boolean { ... }
      fun sendNotification(message: String) { ... }
  }
  ```

### Open/Closed Principle (OCP)
- Open for extension, closed for modification
- Use interfaces and abstract classes for extensibility
- Example:
  ```kotlin
  interface PaymentProcessor {
      suspend fun processPayment(amount: Double): PaymentResult
  }
  
  class CreditCardProcessor : PaymentProcessor {
      override suspend fun processPayment(amount: Double): PaymentResult { ... }
  }
  
  class PayPalProcessor : PaymentProcessor {
      override suspend fun processPayment(amount: Double): PaymentResult { ... }
  }
  
  // Extension without modification
  class ApplePayProcessor : PaymentProcessor {
      override suspend fun processPayment(amount: Double): PaymentResult { ... }
  }
  ```

### Liskov Substitution Principle (LSP)
- Subtypes must be substitutable for their base types
- Derived classes should not weaken base class contracts
- Example:
  ```kotlin
  abstract class DataSource {
      abstract suspend fun fetchData(): List<Item>
  }
  
  class RemoteDataSource : DataSource() {
      override suspend fun fetchData(): List<Item> {
          // Never return null, always return empty list if no data
          return apiService.getItems() ?: emptyList()
      }
  }
  
  class LocalDataSource : DataSource() {
      override suspend fun fetchData(): List<Item> {
          return database.getAllItems()
      }
  }
  ```

### Interface Segregation Principle (ISP)
- Clients should not depend on interfaces they don't use
- Create specific, focused interfaces
- Example:
  ```kotlin
  // Good: Segregated interfaces
  interface UserReader {
      suspend fun getUser(id: String): User
  }
  
  interface UserWriter {
      suspend fun saveUser(user: User)
  }
  
  interface UserDeleter {
      suspend fun deleteUser(id: String)
  }
  
  // Repository implements only what it needs
  class ReadOnlyUserRepository : UserReader {
      override suspend fun getUser(id: String): User { ... }
  }
  
  // Avoid: Fat interface
  interface UserRepository {
      suspend fun getUser(id: String): User
      suspend fun saveUser(user: User)
      suspend fun deleteUser(id: String)
      suspend fun exportUsers(): File
      suspend fun importUsers(file: File)
      suspend fun generateReport(): Report
  }
  ```

### Dependency Inversion Principle (DIP)
- Depend on abstractions, not concretions
- High-level modules should not depend on low-level modules
- Example:
  ```kotlin
  // Good: Depends on abstraction
  class LoginViewModel @Inject constructor(
      private val authRepository: AuthenticationRepository, // Interface
      private val userRepository: UserRepository // Interface
  ) : ViewModel() { ... }
  
  // Avoid: Depends on concrete implementation
  class LoginViewModel @Inject constructor(
      private val authRepositoryImpl: AuthenticationRepositoryImpl,
      private val userRepositoryImpl: UserRepositoryImpl
  ) : ViewModel() { ... }
  ```

## Kotlin Conventions

### Language Features
- Use Kotlin coroutines for asynchronous operations
- Prefer `viewModelScope.launch` in ViewModels
- Use `Flow` for reactive data streams
- Use data classes for models
- Prefer sealed classes for state representation

### Code Style
- Use ktlint for code formatting
- Configure ktlint with these disabled rules:
  ```kotlin
  disabledRules.set(setOf(
      "no-wildcard-imports",
      "max-line-length", 
      "filename"
  ))
  ```

## Flavors & Build Types

### Product Flavors
- Use flavor dimensions for build variants
- Standard flavors: `demo`, `prod`
- Configure flavors in convention plugins

### Build Types
- Standard build types: `debug`, `release`, `benchmark`
- Configure signing configs properly
- Use build config fields for feature flags

## Security & Proguard

### Code Obfuscation
- Enable minification and shrinking for release builds
- Use ProGuard rules appropriately
- Exclude necessary classes from obfuscation

### Signing Configuration
- Store signing configs in separate properties files
- Never commit signing keys to version control
- Use different keys for debug and release

## Documentation

### Code Documentation
- Document public APIs with KDoc
- Include parameter descriptions and return value documentation
- Document complex business logic

### Project Documentation
- Maintain CHANGELOG.md following Keep a Changelog format
- Use semantic versioning
- Document breaking changes

### Resource Documentation
- Use meaningful resource names
- Group related resources together
- Document complex animations or styles

## Analytics & Monitoring

### Firebase Integration
- Use Firebase for analytics and crash reporting
- Configure different Firebase projects for different flavors
- Use Firebase Remote Config for feature flags

### Logging
- Use Timber for logging
- Different log levels for debug vs release
- Avoid logging sensitive information

## Background Work

### WorkManager
- Use WorkManager for background tasks
- Create separate sync module for background operations
- Use Hilt Worker injection

## Performance

### Baseline Profiles
- Include baseline profile generation
- Configure automatic generation for release builds
- Use macrobenchmark module for performance testing

### Memory Management
- Use appropriate lifecycle scopes
- Avoid memory leaks in ViewModels
- Use proper disposal of resources

## Lint Rules

### Custom Lint
- Create custom lint rules in dedicated lint module
- Enforce naming conventions
- Check for common anti-patterns
- Example: Enforce test method naming conventions

### Static Analysis
- Use Android Lint for static analysis
- Configure lint checks appropriately
- Address lint warnings in CI/CD

## Navigation

### Compose Navigation
- Use Compose Navigation for screen navigation
- Define navigation graphs
- Use type-safe navigation arguments

## Error Handling

### Exception Management
- Use proper exception handling in repositories
- Convert exceptions to domain-specific errors
- Handle network errors gracefully

### User Feedback
- Show appropriate error messages to users
- Use consistent error state handling across features
- Implement retry mechanisms where appropriate

## Accessibility & User Experience

### Accessibility (A11y)
- Use semantic content descriptions for all interactive elements
- Implement proper focus management
- Support TalkBack and other accessibility services
- Test with accessibility scanner
- Example:
  ```kotlin
  @Composable
  fun AccessibleButton(
      text: String,
      onClick: () -> Unit,
      contentDescription: String? = null
  ) {
      Button(
          onClick = onClick,
          modifier = Modifier.semantics {
              contentDescription?.let { this.contentDescription = it }
              role = Role.Button
          }
      ) {
          Text(text)
      }
  }
  ```

### Internationalization (i18n)
- Use string resources for all user-facing text
- Support RTL layouts with `android:supportsRtl="true"`
- Use `ContextCompat.getDrawable()` for locale-aware drawables
- Test with different locales and text lengths
- Example:
  ```kotlin
  // strings.xml
  <string name="welcome_message">Welcome, %1$s!</string>
  
  // Usage
  getString(R.string.welcome_message, userName)
  ```

### Dark Theme Support
- Implement proper dark theme support
- Use theme-aware colors and drawables
- Test all screens in both light and dark modes
- Follow Material Design dark theme guidelines

## Offline & Connectivity

### Offline Support
- Implement proper offline-first architecture
- Cache critical data locally
- Show appropriate offline states
- Sync data when connectivity returns
- Example:
  ```kotlin
  @Composable
  fun OfflineAwareContent(
      isOnline: Boolean,
      content: @Composable () -> Unit
  ) {
      if (isOnline) {
          content()
      } else {
          OfflineBanner()
      }
  }
  ```

### Network Monitoring
- Monitor network connectivity changes
- Implement connection quality checks
- Handle different network types appropriately
- Use `ConnectivityManager` for network state

## Security & Privacy

### Data Encryption
- Encrypt sensitive data at rest using EncryptedSharedPreferences
- Use Android Keystore for cryptographic keys
- Implement certificate pinning for network security
- Validate all input data properly

### Privacy Compliance
- Implement proper data consent flows
- Provide clear privacy controls
- Handle data deletion requests
- Audit data collection and usage

### Code Obfuscation & Security
- Use R8/ProGuard for release builds
- Implement root detection if needed
- Protect against reverse engineering
- Use HTTPS for all network communications

## Push Notifications

### Firebase Cloud Messaging (FCM)
- Implement FCM for push notifications
- Handle notification permissions properly
- Create notification channels for Android 8.0+
- Track notification analytics
- Example:
  ```kotlin
  class MessagingService : FirebaseMessagingService() {
      override fun onMessageReceived(remoteMessage: RemoteMessage) {
          // Handle notification data
          remoteMessage.notification?.let {
              showNotification(it.title, it.body)
          }
      }
  }
  ```

## Real-time Features

### WebSocket/Socket.IO Integration
- Implement real-time communication when needed
- Handle connection states properly
- Implement reconnection logic
- Manage WebSocket lifecycle with app state

## Dependency Management

### Dependency Updates
- Use dependency guard plugin
- Regularly update dependencies
- Test thoroughly after dependency updates

### Version Alignment
- Keep related dependencies aligned (AGP, Kotlin, Compose)
- Use BOM dependencies where available
- Document version compatibility requirements 
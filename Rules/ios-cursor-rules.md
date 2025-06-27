# Telepesa: iOS Swift Development Rules

These rules are based on best practices for building modern, scalable iOS applications using Swift, SwiftUI, and contemporary development patterns.

## Project Structure & Architecture

### Xcode Project Organization
- Use feature-based folder organization
- Follow this structure:
  ```
  Telepesa/
    App/
      TelepesaApp.swift
      ContentView.swift
    Features/
      Authentication/
        Views/
        ViewModels/
        Models/
        Services/
      Dashboard/
        Views/
        ViewModels/
        Models/
      Transactions/
        Views/
        ViewModels/
        Models/
      Payments/
        Views/
        ViewModels/
        Models/
    Shared/
      Components/       # Reusable UI components
      Services/         # Network, storage services
      Models/          # Domain models and DTOs
      Extensions/      # Swift extensions
      Utils/           # Utility functions
      Constants/       # App constants
    Resources/
      Assets.xcassets
      Localizable.strings
      Info.plist
  ```

### Package Organization
- Use Swift Package Manager for dependencies
- Create local packages for shared code
- Organize by feature domains, not technical layers
- Example package structure:
  ```
  TelepesaCore/
    Sources/
      TelepesaCore/
        Networking/
        Storage/
        Models/
        Extensions/
  TelepesaUI/
    Sources/
      TelepesaUI/
        Components/
        Modifiers/
        Styles/
  ```

### Bundle Identifiers
- Use reverse domain naming: `com.maelcolium.telepesa`
- Feature modules: `com.maelcolium.telepesa.{feature}`
- Extensions: `com.maelcolium.telepesa.{extension-type}`

## SwiftUI Architecture

### View Architecture
- Follow MVVM pattern with SwiftUI
- Use `@StateObject` for view models
- Use `@ObservedObject` for passed objects
- Implement proper view composition
- Example:
  ```swift
  struct DashboardView: View {
      @StateObject private var viewModel = DashboardViewModel()
      @EnvironmentObject private var authService: AuthenticationService
      
      var body: some View {
          NavigationView {
              ScrollView {
                  LazyVStack(spacing: 16) {
                      ForEach(viewModel.accounts) { account in
                          AccountBalanceCard(account: account)
                      }
                      
                      TransactionSection(transactions: viewModel.recentTransactions)
                  }
                  .padding()
              }
              .navigationTitle("Dashboard")
              .refreshable {
                  await viewModel.refresh()
              }
          }
          .task {
              await viewModel.loadData()
          }
      }
  }
  ```

### State Management
- Use `@State` for local view state
- Use `@StateObject` for view model ownership
- Use `@ObservedObject` for dependency injection
- Use `@Published` in view models for reactive updates
- Example:
  ```swift
  @MainActor
  class DashboardViewModel: ObservableObject {
      @Published var accounts: [Account] = []
      @Published var recentTransactions: [Transaction] = []
      @Published var isLoading = false
      @Published var error: AppError?
      
      private let accountService: AccountService
      private let transactionService: TransactionService
      
      init(
          accountService: AccountService = .shared,
          transactionService: TransactionService = .shared
      ) {
          self.accountService = accountService
          self.transactionService = transactionService
      }
      
      func loadData() async {
          isLoading = true
          defer { isLoading = false }
          
          do {
              async let accountsTask = accountService.fetchAccounts()
              async let transactionsTask = transactionService.fetchRecent()
              
              accounts = try await accountsTask
              recentTransactions = try await transactionsTask
          } catch {
              self.error = AppError.from(error)
          }
      }
  }
  ```

### View Composition
- Create small, focused views
- Use view builders for complex layouts
- Implement proper view modifiers
- Example:
  ```swift
  struct AccountBalanceCard: View {
      let account: Account
      
      var body: some View {
          VStack(alignment: .leading, spacing: 8) {
              HStack {
                  Text(account.name)
                      .font(.subheadline)
                      .foregroundColor(.secondary)
                  Spacer()
                  AccountTypeIcon(type: account.type)
              }
              
              Text(account.balance.formatted(.currency(code: "KES")))
                  .font(.title2)
                  .fontWeight(.bold)
          }
          .padding()
          .background(Color(.systemBackground))
          .cornerRadius(12)
          .shadow(radius: 2)
      }
  }
  ```

## Data Layer & Networking

### Repository Pattern
- Create repository protocols for data abstraction
- Implement concrete repositories with dependency injection
- Use async/await for asynchronous operations
- Example:
  ```swift
  protocol AccountRepository {
      func fetchAccounts() async throws -> [Account]
      func updateAccount(_ account: Account) async throws -> Account
      func deleteAccount(id: String) async throws
  }
  
  class DefaultAccountRepository: AccountRepository {
      private let networkService: NetworkService
      private let cacheService: CacheService
      
      init(
          networkService: NetworkService,
          cacheService: CacheService
      ) {
          self.networkService = networkService
          self.cacheService = cacheService
      }
      
      func fetchAccounts() async throws -> [Account] {
          // Try cache first
          if let cachedAccounts = await cacheService.getCachedAccounts(),
             !cachedAccounts.isEmpty {
              return cachedAccounts
          }
          
          // Fetch from network
          let accounts = try await networkService.request(
              AccountsEndpoint.fetchAll,
              responseType: [AccountDTO].self
          ).map { $0.toDomain() }
          
          // Cache the results
          await cacheService.cacheAccounts(accounts)
          return accounts
      }
  }
  ```

### Network Layer
- Use URLSession with async/await
- Implement proper error handling
- Use Codable for JSON serialization
- Example:
  ```swift
  class NetworkService {
      private let session: URLSession
      private let baseURL: URL
      
      init(baseURL: URL, session: URLSession = .shared) {
          self.baseURL = baseURL
          self.session = session
      }
      
      func request<T: Codable>(
          _ endpoint: APIEndpoint,
          responseType: T.Type
      ) async throws -> T {
          let request = try buildRequest(for: endpoint)
          
          let (data, response) = try await session.data(for: request)
          
          guard let httpResponse = response as? HTTPURLResponse else {
              throw NetworkError.invalidResponse
          }
          
          guard 200...299 ~= httpResponse.statusCode else {
              throw NetworkError.serverError(httpResponse.statusCode)
          }
          
          do {
              return try JSONDecoder().decode(T.self, from: data)
          } catch {
              throw NetworkError.decodingError(error)
          }
      }
      
      private func buildRequest(for endpoint: APIEndpoint) throws -> URLRequest {
          let url = baseURL.appendingPathComponent(endpoint.path)
          var request = URLRequest(url: url)
          request.httpMethod = endpoint.method.rawValue
          request.setValue("application/json", forHTTPHeaderField: "Content-Type")
          
          // Add authentication if available
          if let token = AuthTokenManager.shared.currentToken {
              request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
          }
          
          if let body = endpoint.body {
              request.httpBody = try JSONSerialization.data(withJSONObject: body)
          }
          
          return request
      }
  }
  ```

### Core Data Integration
- Use Core Data for local storage
- Implement proper data models
- Use NSPersistentContainer for setup
- Example:
  ```swift
  class CoreDataStack {
      static let shared = CoreDataStack()
      
      lazy var persistentContainer: NSPersistentContainer = {
          let container = NSPersistentContainer(name: "TelepesaModel")
          container.loadPersistentStores { _, error in
              if let error = error {
                  fatalError("Core Data failed to load: \(error)")
              }
          }
          return container
      }()
      
      var context: NSManagedObjectContext {
          persistentContainer.viewContext
      }
      
      func save() {
          if context.hasChanges {
              try? context.save()
          }
      }
  }
  
  extension Account {
      func toManagedObject(context: NSManagedObjectContext) -> AccountEntity {
          let entity = AccountEntity(context: context)
          entity.id = self.id
          entity.name = self.name
          entity.balance = NSDecimalNumber(decimal: self.balance)
          entity.type = self.type.rawValue
          return entity
      }
  }
  ```

## UI Components & Design System

### Reusable Components
- Create a design system with consistent components
- Use ViewModifiers for styling
- Implement accessibility features
- Example:
  ```swift
  struct PrimaryButton: View {
      let title: String
      let action: () -> Void
      var isLoading: Bool = false
      var isDisabled: Bool = false
      
      var body: some View {
          Button(action: action) {
              HStack {
                  if isLoading {
                      ProgressView()
                          .progressViewStyle(CircularProgressViewStyle(tint: .white))
                          .scaleEffect(0.8)
                  } else {
                      Text(title)
                          .fontWeight(.semibold)
                  }
              }
              .frame(maxWidth: .infinity)
              .frame(height: 50)
              .foregroundColor(.white)
              .background(
                  (isDisabled || isLoading) ? Color.gray : Color.blue
              )
              .cornerRadius(12)
          }
          .disabled(isDisabled || isLoading)
          .accessibilityLabel(title)
          .accessibilityHint(isLoading ? "Loading" : "Tap to \(title.lowercased())")
      }
  }
  
  struct InputField: View {
      let title: String
      @Binding var text: String
      var placeholder: String = ""
      var keyboardType: UIKeyboardType = .default
      var isSecure: Bool = false
      var errorMessage: String?
      
      var body: some View {
          VStack(alignment: .leading, spacing: 4) {
              Text(title)
                  .font(.caption)
                  .foregroundColor(.secondary)
              
              Group {
                  if isSecure {
                      SecureField(placeholder, text: $text)
                  } else {
                      TextField(placeholder, text: $text)
                  }
              }
              .textFieldStyle(RoundedBorderTextFieldStyle())
              .keyboardType(keyboardType)
              
              if let errorMessage = errorMessage {
                  Text(errorMessage)
                      .font(.caption)
                      .foregroundColor(.red)
              }
          }
      }
  }
  ```

### Financial UI Components
- Create specialized components for fintech features
- Implement proper currency formatting
- Add security indicators
- Example:
  ```swift
  struct CurrencyAmountView: View {
      let amount: Decimal
      let currency: String = "KES"
      let isPositive: Bool
      
      var body: some View {
          Text(formatAmount())
              .font(.title2)
              .fontWeight(.bold)
              .foregroundColor(isPositive ? .green : .red)
      }
      
      private func formatAmount() -> String {
          let formatter = NumberFormatter()
          formatter.numberStyle = .currency
          formatter.currencyCode = currency
          formatter.locale = Locale(identifier: "en_KE")
          
          let prefix = isPositive ? "+" : ""
          return prefix + (formatter.string(from: NSDecimalNumber(decimal: amount)) ?? "")
      }
  }
  
  struct TransactionRow: View {
      let transaction: Transaction
      
      var body: some View {
          HStack {
              TransactionIcon(type: transaction.type)
                  .frame(width: 40, height: 40)
              
              VStack(alignment: .leading, spacing: 2) {
                  Text(transaction.description)
                      .font(.body)
                      .fontWeight(.medium)
                  
                  Text(transaction.date, style: .relative)
                      .font(.caption)
                      .foregroundColor(.secondary)
              }
              
              Spacer()
              
              VStack(alignment: .trailing, spacing: 2) {
                  CurrencyAmountView(
                      amount: transaction.amount,
                      isPositive: transaction.type == .credit
                  )
                  
                  TransactionStatusBadge(status: transaction.status)
              }
          }
          .padding(.vertical, 8)
      }
  }
  
  struct TransactionStatusBadge: View {
      let status: TransactionStatus
      
      var body: some View {
          Text(status.displayName)
              .font(.caption)
              .fontWeight(.semibold)
              .foregroundColor(status.textColor)
              .padding(.horizontal, 8)
              .padding(.vertical, 2)
              .background(status.backgroundColor)
              .cornerRadius(4)
      }
  }
  ```

## Testing

### Unit Testing
- Test view models and business logic
- Use XCTest framework
- Mock dependencies properly
- Example:
  ```swift
  class DashboardViewModelTests: XCTestCase {
      var viewModel: DashboardViewModel!
      var mockAccountService: MockAccountService!
      var mockTransactionService: MockTransactionService!
      
      override func setUp() {
          super.setUp()
          mockAccountService = MockAccountService()
          mockTransactionService = MockTransactionService()
          viewModel = DashboardViewModel(
              accountService: mockAccountService,
              transactionService: mockTransactionService
          )
      }
      
      func testLoadDataSuccess() async {
          // Given
          let expectedAccounts = [Account.mock]
          let expectedTransactions = [Transaction.mock]
          
          mockAccountService.accountsToReturn = expectedAccounts
          mockTransactionService.transactionsToReturn = expectedTransactions
          
          // When
          await viewModel.loadData()
          
          // Then
          XCTAssertEqual(viewModel.accounts, expectedAccounts)
          XCTAssertEqual(viewModel.recentTransactions, expectedTransactions)
          XCTAssertFalse(viewModel.isLoading)
          XCTAssertNil(viewModel.error)
      }
      
      func testLoadDataFailure() async {
          // Given
          mockAccountService.shouldThrowError = true
          
          // When
          await viewModel.loadData()
          
          // Then
          XCTAssertTrue(viewModel.accounts.isEmpty)
          XCTAssertNotNil(viewModel.error)
          XCTAssertFalse(viewModel.isLoading)
      }
  }
  
  class MockAccountService: AccountService {
      var accountsToReturn: [Account] = []
      var shouldThrowError = false
      
      func fetchAccounts() async throws -> [Account] {
          if shouldThrowError {
              throw NetworkError.serverError(500)
          }
          return accountsToReturn
      }
  }
  ```

### UI Testing
- Test user interactions and flows
- Use XCUITest framework
- Test critical user journeys
- Example:
  ```swift
  class LoginFlowUITests: XCTestCase {
      var app: XCUIApplication!
      
      override func setUp() {
          super.setUp()
          app = XCUIApplication()
          app.launch()
      }
      
      func testSuccessfulLogin() {
          // Navigate to login
          app.buttons["Login"].tap()
          
          // Enter credentials
          let emailField = app.textFields["Email"]
          emailField.tap()
          emailField.typeText("test@example.com")
          
          let passwordField = app.secureTextFields["Password"]
          passwordField.tap()
          passwordField.typeText("password123")
          
          // Submit login
          app.buttons["Sign In"].tap()
          
          // Verify navigation to dashboard
          XCTAssertTrue(app.navigationBars["Dashboard"].waitForExistence(timeout: 5))
      }
      
      func testLoginValidation() {
          app.buttons["Login"].tap()
          
          // Try to login without entering credentials
          app.buttons["Sign In"].tap()
          
          // Verify error messages
          XCTAssertTrue(app.staticTexts["Email is required"].exists)
          XCTAssertTrue(app.staticTexts["Password is required"].exists)
      }
  }
  ```

## Security

### Keychain Integration
- Store sensitive data in Keychain
- Implement secure token storage
- Use proper access controls
- Example:
  ```swift
  class KeychainService {
      static let shared = KeychainService()
      
      private let service = "com.maelcolium.telepesa"
      
      func store(data: Data, for key: String) throws {
          let query: [String: Any] = [
              kSecClass as String: kSecClassGenericPassword,
              kSecAttrService as String: service,
              kSecAttrAccount as String: key,
              kSecValueData as String: data,
              kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlockedThisDeviceOnly
          ]
          
          // Delete existing item
          SecItemDelete(query as CFDictionary)
          
          // Add new item
          let status = SecItemAdd(query as CFDictionary, nil)
          guard status == errSecSuccess else {
              throw KeychainError.unableToStore
          }
      }
      
      func retrieve(for key: String) throws -> Data {
          let query: [String: Any] = [
              kSecClass as String: kSecClassGenericPassword,
              kSecAttrService as String: service,
              kSecAttrAccount as String: key,
              kSecReturnData as String: true,
              kSecMatchLimit as String: kSecMatchLimitOne
          ]
          
          var result: AnyObject?
          let status = SecItemCopyMatching(query as CFDictionary, &result)
          
          guard status == errSecSuccess,
                let data = result as? Data else {
              throw KeychainError.unableToRetrieve
          }
          
          return data
      }
      
      func delete(for key: String) throws {
          let query: [String: Any] = [
              kSecClass as String: kSecClassGenericPassword,
              kSecAttrService as String: service,
              kSecAttrAccount as String: key
          ]
          
          let status = SecItemDelete(query as CFDictionary)
          guard status == errSecSuccess || status == errSecItemNotFound else {
              throw KeychainError.unableToDelete
          }
      }
  }
  
  enum KeychainError: Error {
      case unableToStore
      case unableToRetrieve
      case unableToDelete
  }
  ```

### Biometric Authentication
- Implement Face ID/Touch ID
- Handle authentication states
- Provide fallback options
- Example:
  ```swift
  import LocalAuthentication
  
  class BiometricAuthService: ObservableObject {
      @Published var isAuthenticated = false
      @Published var authError: AuthError?
      
      func authenticateUser() async {
          let context = LAContext()
          var error: NSError?
          
          // Check if biometric authentication is available
          guard context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) else {
              await MainActor.run {
                  self.authError = .biometricNotAvailable
              }
              return
          }
          
          do {
              let success = try await context.evaluatePolicy(
                  .deviceOwnerAuthenticationWithBiometrics,
                  localizedReason: "Authenticate to access your account"
              )
              
              await MainActor.run {
                  self.isAuthenticated = success
              }
          } catch {
              await MainActor.run {
                  self.authError = AuthError.from(error)
              }
          }
      }
  }
  ```

## Accessibility

### VoiceOver Support
- Add accessibility labels and hints
- Implement proper navigation order
- Test with VoiceOver enabled
- Example:
  ```swift
  struct AccessibleTransactionRow: View {
      let transaction: Transaction
      
      var body: some View {
          HStack {
              TransactionIcon(type: transaction.type)
              
              VStack(alignment: .leading) {
                  Text(transaction.description)
                  Text(transaction.date, style: .relative)
              }
              
              Spacer()
              
              CurrencyAmountView(
                  amount: transaction.amount,
                  isPositive: transaction.type == .credit
              )
          }
          .accessibilityElement(children: .combine)
          .accessibilityLabel(accessibilityDescription)
          .accessibilityHint("Double tap to view transaction details")
          .accessibilityAction {
              // Handle tap action
          }
      }
      
      private var accessibilityDescription: String {
          let amountDescription = transaction.amount.formatted(.currency(code: "KES"))
          let typeDescription = transaction.type == .credit ? "received" : "sent"
          let dateDescription = transaction.date.formatted(.relative(presentation: .named))
          
          return "\(typeDescription) \(amountDescription) for \(transaction.description), \(dateDescription)"
      }
  }
  ```

### Dynamic Type Support
- Use system fonts that scale
- Test with different text sizes
- Implement proper layout adjustments
- Example:
  ```swift
  struct DynamicTypeCard: View {
      let title: String
      let subtitle: String
      
      var body: some View {
          VStack(alignment: .leading, spacing: 8) {
              Text(title)
                  .font(.headline)
                  .lineLimit(nil)
              
              Text(subtitle)
                  .font(.subheadline)
                  .foregroundColor(.secondary)
                  .lineLimit(nil)
          }
          .padding()
          .background(Color(.systemBackground))
          .cornerRadius(12)
          .dynamicTypeSize(...DynamicTypeSize.accessibility3)
      }
  }
  ```

This comprehensive iOS Swift rules file provides guidelines for building a modern, scalable iOS application that integrates well with your Spring Boot backend and follows the same quality standards as your other platform applications.

## Internationalization (i18n)

### Localization Setup
- Use NSLocalizedString for all user-facing text
- Create Localizable.strings files for each language
- Support RTL languages properly
- Example:
  ```swift
  // Localizable.strings (English)
  "welcome_message" = "Welcome to Telepesa";
  "balance_label" = "Account Balance";
  "send_money" = "Send Money";
  
  // Usage in code
  extension String {
      var localized: String {
          NSLocalizedString(self, comment: "")
      }
      
      func localized(with arguments: CVarArg...) -> String {
          String(format: NSLocalizedString(self, comment: ""), arguments: arguments)
      }
  }
  
  // In SwiftUI views
  Text("welcome_message".localized)
  Text("balance_amount".localized(with: balance))
  ```

### Currency and Number Formatting
- Use NumberFormatter for locale-specific formatting
- Support multiple currencies
- Handle different number systems
- Example:
  ```swift
  class CurrencyFormatter {
      static let shared = CurrencyFormatter()
      
      private let numberFormatter: NumberFormatter = {
          let formatter = NumberFormatter()
          formatter.numberStyle = .currency
          return formatter
      }()
      
      func format(
          amount: Decimal,
          currencyCode: String = "KES",
          locale: Locale = .current
      ) -> String {
          numberFormatter.currencyCode = currencyCode
          numberFormatter.locale = locale
          return numberFormatter.string(from: NSDecimalNumber(decimal: amount)) ?? ""
      }
  }
  ```

## Performance Optimization

### Memory Management
- Use weak references to avoid retain cycles
- Implement proper image caching
- Use lazy loading for expensive operations
- Example:
  ```swift
  class ImageCache {
      static let shared = ImageCache()
      private let cache = NSCache<NSString, UIImage>()
      
      private init() {
          cache.countLimit = 100
          cache.totalCostLimit = 50 * 1024 * 1024 // 50MB
      }
      
      func image(for key: String) -> UIImage? {
          cache.object(forKey: key as NSString)
      }
      
      func setImage(_ image: UIImage, for key: String) {
          cache.setObject(image, forKey: key as NSString)
      }
  }
  
  struct AsyncImageView: View {
      let url: URL
      @State private var image: UIImage?
      @State private var isLoading = false
      
      var body: some View {
          Group {
              if let image = image {
                  Image(uiImage: image)
                      .resizable()
                      .aspectRatio(contentMode: .fit)
              } else if isLoading {
                  ProgressView()
              } else {
                  Rectangle()
                      .fill(Color.gray.opacity(0.3))
              }
          }
          .onAppear {
              loadImage()
          }
      }
      
      private func loadImage() {
          let cacheKey = url.absoluteString
          
          if let cachedImage = ImageCache.shared.image(for: cacheKey) {
              self.image = cachedImage
              return
          }
          
          isLoading = true
          
          Task {
              do {
                  let (data, _) = try await URLSession.shared.data(from: url)
                  if let downloadedImage = UIImage(data: data) {
                      ImageCache.shared.setImage(downloadedImage, for: cacheKey)
                      await MainActor.run {
                          self.image = downloadedImage
                          self.isLoading = false
                      }
                  }
              } catch {
                  await MainActor.run {
                      self.isLoading = false
                  }
              }
          }
      }
  }
  ```

### Background Processing
- Use Task for async operations
- Implement proper background task handling
- Handle app lifecycle events
- Example:
  ```swift
  class BackgroundTaskManager {
      static let shared = BackgroundTaskManager()
      private var backgroundTaskID: UIBackgroundTaskIdentifier = .invalid
      
      func startBackgroundTask() {
          endBackgroundTask()
          
          backgroundTaskID = UIApplication.shared.beginBackgroundTask {
              self.endBackgroundTask()
          }
      }
      
      func endBackgroundTask() {
          if backgroundTaskID != .invalid {
              UIApplication.shared.endBackgroundTask(backgroundTaskID)
              backgroundTaskID = .invalid
          }
      }
  }
  
  @MainActor
  class SyncService: ObservableObject {
      @Published var isSyncing = false
      
      func syncData() async {
          guard !isSyncing else { return }
          
          isSyncing = true
          BackgroundTaskManager.shared.startBackgroundTask()
          
          defer {
              isSyncing = false
              BackgroundTaskManager.shared.endBackgroundTask()
          }
          
          do {
              // Perform sync operations
              try await performDataSync()
          } catch {
              print("Sync failed: \(error)")
          }
      }
      
      private func performDataSync() async throws {
          // Sync implementation
      }
  }
  ```

## Real-time Features

### WebSocket Integration
- Use URLSessionWebSocketTask for WebSocket connections
- Implement proper connection management
- Handle reconnection logic
- Example:
  ```swift
  class WebSocketManager: ObservableObject {
      @Published var isConnected = false
      @Published var lastMessage: String?
      
      private var webSocketTask: URLSessionWebSocketTask?
      private let url: URL
      
      init(url: URL) {
          self.url = url
      }
      
      func connect() {
          disconnect() // Ensure clean state
          
          var request = URLRequest(url: url)
          if let token = AuthTokenManager.shared.currentToken {
              request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
          }
          
          webSocketTask = URLSession.shared.webSocketTask(with: request)
          webSocketTask?.resume()
          
          isConnected = true
          receiveMessage()
      }
      
      func disconnect() {
          webSocketTask?.cancel(with: .goingAway, reason: nil)
          webSocketTask = nil
          isConnected = false
      }
      
      func sendMessage(_ message: String) {
          let message = URLSessionWebSocketTask.Message.string(message)
          webSocketTask?.send(message) { error in
              if let error = error {
                  print("WebSocket send error: \(error)")
              }
          }
      }
      
      private func receiveMessage() {
          webSocketTask?.receive { [weak self] result in
              switch result {
              case .success(let message):
                  switch message {
                  case .string(let text):
                      DispatchQueue.main.async {
                          self?.lastMessage = text
                      }
                  case .data(let data):
                      // Handle binary data
                      break
                  @unknown default:
                      break
                  }
                  self?.receiveMessage() // Continue receiving
                  
              case .failure(let error):
                  print("WebSocket receive error: \(error)")
                  DispatchQueue.main.async {
                      self?.isConnected = false
                  }
              }
          }
      }
  }
  ```

### Push Notifications
- Implement APNs integration
- Handle notification permissions
- Support rich notifications
- Example:
  ```swift
  import UserNotifications
  
  class NotificationManager: NSObject, ObservableObject {
      @Published var authorizationStatus: UNAuthorizationStatus = .notDetermined
      
      override init() {
          super.init()
          UNUserNotificationCenter.current().delegate = self
      }
      
      func requestPermission() async {
          do {
              let granted = try await UNUserNotificationCenter.current()
                  .requestAuthorization(options: [.alert, .badge, .sound])
              
              await MainActor.run {
                  self.authorizationStatus = granted ? .authorized : .denied
              }
              
              if granted {
                  await UIApplication.shared.registerForRemoteNotifications()
              }
          } catch {
              print("Notification permission error: \(error)")
          }
      }
      
      func scheduleLocalNotification(
          title: String,
          body: String,
          timeInterval: TimeInterval
      ) {
          let content = UNMutableNotificationContent()
          content.title = title
          content.body = body
          content.sound = .default
          
          let trigger = UNTimeIntervalNotificationTrigger(
              timeInterval: timeInterval,
              repeats: false
          )
          
          let request = UNNotificationRequest(
              identifier: UUID().uuidString,
              content: content,
              trigger: trigger
          )
          
          UNUserNotificationCenter.current().add(request)
      }
  }
  
  extension NotificationManager: UNUserNotificationCenterDelegate {
      func userNotificationCenter(
          _ center: UNUserNotificationCenter,
          willPresent notification: UNNotification,
          withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
      ) {
          completionHandler([.banner, .sound])
      }
      
      func userNotificationCenter(
          _ center: UNUserNotificationCenter,
          didReceive response: UNNotificationResponse,
          withCompletionHandler completionHandler: @escaping () -> Void
      ) {
          // Handle notification tap
          completionHandler()
      }
  }
  ```

## Fintech-Specific Patterns

### Payment Flow Components
- Create secure payment interfaces
- Implement amount validation
- Handle payment confirmations
- Example:
  ```swift
  struct PaymentFlowView: View {
      @StateObject private var viewModel = PaymentFlowViewModel()
      @State private var currentStep: PaymentStep = .amount
      
      var body: some View {
          NavigationView {
              VStack {
                  ProgressView(value: currentStep.progress)
                      .padding()
                  
                  switch currentStep {
                  case .amount:
                      AmountInputView(
                          amount: $viewModel.amount,
                          onNext: { currentStep = .recipient }
                      )
                  case .recipient:
                      RecipientSelectionView(
                          recipient: $viewModel.recipient,
                          onNext: { currentStep = .confirmation }
                      )
                  case .confirmation:
                      PaymentConfirmationView(
                          amount: viewModel.amount,
                          recipient: viewModel.recipient,
                          onConfirm: {
                              Task {
                                  await viewModel.processPayment()
                                  currentStep = .success
                              }
                          }
                      )
                  case .success:
                      PaymentSuccessView()
                  }
                  
                  Spacer()
              }
              .navigationTitle("Send Money")
          }
      }
  }
  
  struct AmountInputView: View {
      @Binding var amount: Decimal
      let onNext: () -> Void
      @State private var amountText = ""
      
      var body: some View {
          VStack(spacing: 24) {
              Text("Enter Amount")
                  .font(.title2)
                  .fontWeight(.semibold)
              
              VStack {
                  HStack {
                      Text("KES")
                          .font(.title)
                          .foregroundColor(.blue)
                      
                      TextField("0.00", text: $amountText)
                          .font(.largeTitle)
                          .fontWeight(.bold)
                          .keyboardType(.decimalPad)
                          .multilineTextAlignment(.center)
                          .onChange(of: amountText) { newValue in
                              if let decimal = Decimal(string: newValue) {
                                  amount = decimal
                              }
                          }
                  }
                  
                  Rectangle()
                      .frame(height: 2)
                      .foregroundColor(.blue)
              }
              .padding()
              
              PrimaryButton(title: "Continue", action: onNext)
                  .disabled(amount <= 0)
          }
          .padding()
      }
  }
  ```

### Card Management
- Implement card input forms
- Add card validation
- Show card types and security features
- Example:
  ```swift
  struct CardInputView: View {
      @State private var cardNumber = ""
      @State private var expiryDate = ""
      @State private var cvv = ""
      @State private var cardholderName = ""
      
      var body: some View {
          VStack(spacing: 16) {
              // Card preview
              CardPreview(
                  cardNumber: cardNumber,
                  expiryDate: expiryDate,
                  cardholderName: cardholderName
              )
              
              // Input fields
              VStack(spacing: 12) {
                  CardNumberField(cardNumber: $cardNumber)
                  
                  HStack {
                      ExpiryDateField(expiryDate: $expiryDate)
                      CVVField(cvv: $cvv)
                  }
                  
                  TextField("Cardholder Name", text: $cardholderName)
                      .textFieldStyle(RoundedBorderTextFieldStyle())
                      .textContentType(.name)
              }
              
              PrimaryButton(title: "Add Card") {
                  // Process card addition
              }
              .disabled(!isValidCard)
          }
          .padding()
      }
      
      private var isValidCard: Bool {
          cardNumber.isValidCardNumber &&
          expiryDate.isValidExpiryDate &&
          cvv.isValidCVV &&
          !cardholderName.isEmpty
      }
  }
  
  struct CardNumberField: View {
      @Binding var cardNumber: String
      
      var body: some View {
          TextField("Card Number", text: $cardNumber)
              .textFieldStyle(RoundedBorderTextFieldStyle())
              .keyboardType(.numberPad)
              .textContentType(.creditCardNumber)
              .onChange(of: cardNumber) { newValue in
                  cardNumber = formatCardNumber(newValue)
              }
      }
      
      private func formatCardNumber(_ input: String) -> String {
          let digits = input.filter { $0.isNumber }
          let grouped = digits.chunked(into: 4).joined(separator: " ")
          return String(grouped.prefix(19)) // 16 digits + 3 spaces
      }
  }
  ```

## Build Configuration & CI/CD

### Xcode Configuration
- Use xcconfig files for build settings
- Implement proper code signing
- Configure different schemes for environments
- Example xcconfig:
  ```
  // Debug.xcconfig
  API_BASE_URL = https://api-dev.telepesa.com
  BUNDLE_ID_SUFFIX = .dev
  APP_NAME = Telepesa Dev
  
  // Release.xcconfig
  API_BASE_URL = https://api.telepesa.com
  BUNDLE_ID_SUFFIX = 
  APP_NAME = Telepesa
  ```

### Swift Package Manager
- Use SPM for dependency management
- Create local packages for shared code
- Version dependencies properly
- Example Package.swift:
  ```swift
  // swift-tools-version: 5.7
  import PackageDescription
  
  let package = Package(
      name: "TelepesaCore",
      platforms: [
          .iOS(.v15)
      ],
      products: [
          .library(name: "TelepesaCore", targets: ["TelepesaCore"]),
          .library(name: "TelepesaUI", targets: ["TelepesaUI"])
      ],
      dependencies: [
          .package(url: "https://github.com/Alamofire/Alamofire.git", from: "5.6.0"),
          .package(url: "https://github.com/realm/realm-swift.git", from: "10.0.0")
      ],
      targets: [
          .target(
              name: "TelepesaCore",
              dependencies: ["Alamofire"]
          ),
          .target(
              name: "TelepesaUI",
              dependencies: ["TelepesaCore"]
          ),
          .testTarget(
              name: "TelepesaCoreTests",
              dependencies: ["TelepesaCore"]
          )
      ]
  )
  ```

### Fastlane Integration
- Automate builds and deployments
- Implement code signing management
- Configure TestFlight uploads
- Example Fastfile:
  ```ruby
  default_platform(:ios)
  
  platform :ios do
    desc "Run tests"
    lane :test do
      run_tests(
        scheme: "Telepesa",
        device: "iPhone 14"
      )
    end
    
    desc "Build and upload to TestFlight"
    lane :beta do
      increment_build_number
      build_app(
        scheme: "Telepesa",
        export_method: "app-store"
      )
      upload_to_testflight
    end
    
    desc "Release to App Store"
    lane :release do
      increment_version_number
      build_app(
        scheme: "Telepesa",
        export_method: "app-store"
      )
      upload_to_app_store
    end
  end
  ```

## Error Handling & Logging

### Structured Error Handling
- Create custom error types
- Implement proper error propagation
- Provide user-friendly error messages
- Example:
  ```swift
  enum TelepesaError: LocalizedError {
      case networkError(NetworkError)
      case authenticationFailed
      case insufficientFunds
      case invalidCardDetails
      case serverMaintenance
      
      var errorDescription: String? {
          switch self {
          case .networkError(let networkError):
              return networkError.localizedDescription
          case .authenticationFailed:
              return "authentication_failed".localized
          case .insufficientFunds:
              return "insufficient_funds".localized
          case .invalidCardDetails:
              return "invalid_card_details".localized
          case .serverMaintenance:
              return "server_maintenance".localized
          }
      }
      
      var recoverySuggestion: String? {
          switch self {
          case .networkError:
              return "check_connection".localized
          case .authenticationFailed:
              return "try_login_again".localized
          case .insufficientFunds:
              return "add_funds".localized
          case .invalidCardDetails:
              return "check_card_details".localized
          case .serverMaintenance:
              return "try_again_later".localized
          }
      }
  }
  
  class ErrorHandler: ObservableObject {
      @Published var currentError: TelepesaError?
      @Published var showError = false
      
      func handle(_ error: Error) {
          let telepesaError = TelepesaError.from(error)
          
          DispatchQueue.main.async {
              self.currentError = telepesaError
              self.showError = true
          }
          
          // Log error for analytics
          Logger.shared.log(error: telepesaError)
      }
  }
  ```

### Logging Framework
- Implement structured logging
- Use os_log for system integration
- Add remote logging for production
- Example:
  ```swift
  import os.log
  
  class Logger {
      static let shared = Logger()
      
      private let subsystem = "com.maelcolium.telepesa"
      private let networkLog = OSLog(subsystem: "com.maelcolium.telepesa", category: "network")
      private let authLog = OSLog(subsystem: "com.maelcolium.telepesa", category: "auth")
      private let paymentLog = OSLog(subsystem: "com.maelcolium.telepesa", category: "payment")
      
      func log(
          _ message: String,
          category: LogCategory = .general,
          level: OSLogType = .default
      ) {
          let log = logForCategory(category)
          os_log("%{public}@", log: log, type: level, message)
          
          // Send to remote logging service in production
          if ProcessInfo.processInfo.environment["DEBUG"] == nil {
              sendToRemoteLogging(message, category: category, level: level)
          }
      }
      
      func log(error: Error, category: LogCategory = .general) {
          log("Error: \(error.localizedDescription)", category: category, level: .error)
      }
      
      private func logForCategory(_ category: LogCategory) -> OSLog {
          switch category {
          case .network: return networkLog
          case .auth: return authLog
          case .payment: return paymentLog
          case .general: return OSLog.default
          }
      }
      
      private func sendToRemoteLogging(
          _ message: String,
          category: LogCategory,
          level: OSLogType
      ) {
          // Implementation for remote logging service
      }
  }
  
  enum LogCategory {
      case general
      case network
      case auth
      case payment
  }
  ```

This comprehensive iOS Swift cursor rules file provides guidelines for building a modern, secure, and scalable iOS financial application that integrates seamlessly with your Spring Boot backend and maintains the same high standards as your other platform applications. 
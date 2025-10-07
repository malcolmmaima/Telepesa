# Telepesa Android App

A modern Android application for digital financial services, built with Clean Architecture principles and SOLID design patterns.

## Architecture

### Clean Architecture Layers

- **Presentation Layer**: Jetpack Compose UI with ViewModels
- **Domain Layer**: Business logic, entities, and use cases
- **Data Layer**: Repository implementations, data sources, and local storage

### SOLID Principles Implementation

- **Single Responsibility**: Each class has one reason to change
- **Open/Closed**: Open for extension, closed for modification
- **Liskov Substitution**: Derived classes are substitutable for base classes
- **Interface Segregation**: Clients depend only on interfaces they use
- **Dependency Inversion**: Depend on abstractions, not concretions

## Features

### Core Features
- **Authentication**: Secure login with biometric support
- **Account Management**: View balances and account details
- **Money Transfer**: Send and receive money instantly
- **Bill Payments**: Pay utilities and services
- **Transaction History**: Complete transaction tracking
- **QR Payments**: Scan and pay with QR codes
- **Card Management**: Digital card management
- **Savings**: Goal-based savings features
- **Notifications**: Real-time push notifications

### Security Features
- **Biometric Authentication**: Fingerprint and face recognition
- **Data Encryption**: Room database with SQLCipher
- **Secure Storage**: Encrypted local data storage
- **Network Security**: Certificate pinning and secure communication

## Project Structure

```
customer-app/
├── app/                          # Main application module
├── core/                         # Core modules
│   ├── common/                   # Common utilities and base classes
│   ├── domain/                   # Domain models and repository interfaces
│   ├── data/                     # Data layer implementations
│   ├── ui/                       # UI themes and base components
│   ├── network/                  # Network layer
│   ├── database/                 # Database layer
│   └── security/                 # Security utilities
├── feature/                      # Dynamic feature modules
│   ├── onboarding/               # App introduction screens
│   ├── auth/                     # Authentication flow
│   ├── home/                     # Home dashboard
│   ├── transfer/                 # Money transfer
│   ├── payments/                 # Bill payments
│   ├── profile/                  # User profile management
│   ├── notifications/            # Push notifications
│   ├── history/                  # Transaction history
│   ├── savings/                  # Savings features
│   └── cards/                    # Card management
└── libraries/                    # Shared libraries
    └── test/                     # Testing utilities
```

## Tech Stack

### Core Technologies
- **Kotlin**: 100% Kotlin codebase
- **Jetpack Compose**: Modern declarative UI
- **Material Design 3**: Latest design system
- **Hilt**: Dependency injection
- **Coroutines**: Asynchronous programming
- **Flow**: Reactive streams

### Architecture Components
- **Navigation Compose**: Type-safe navigation
- **ViewModel**: UI-related data holder
- **Room**: Local database with encryption
- **Retrofit**: HTTP client
- **Moshi**: JSON parsing
- **DataStore**: Preferences storage

### Testing
- **JUnit**: Unit testing framework
- **MockK**: Mocking library
- **Turbine**: Flow testing
- **Espresso**: UI testing

### Code Quality
- **Spotless**: Code formatting
- **Detekt**: Static analysis
- **Ktlint**: Kotlin linting

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17 or later
- Android SDK 34
- Minimum SDK 23

### Setup
1. Clone the repository
2. Open in Android Studio
3. Sync project with Gradle files
4. Configure API endpoints in `local.properties`:
   ```properties
   API_BASE_URL=http://your-api-endpoint.com/api/v1
   ```
5. Build and run the project

### Dynamic Feature Modules
The app uses Dynamic Feature Modules for:
- **On-demand loading**: Features are downloaded when needed
- **Reduced APK size**: Smaller initial download
- **Modular architecture**: Independent feature development
- **Better performance**: Faster app startup

## Development Guidelines

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Write comprehensive documentation
- Implement proper error handling

### Testing Strategy
- **Unit Tests**: Test business logic and ViewModels
- **Integration Tests**: Test repository implementations
- **UI Tests**: Test critical user flows
- **Target**: 80%+ code coverage

### Git Workflow
- Use feature branches for development
- Write descriptive commit messages
- Create pull requests for code review
- Ensure all tests pass before merging

## Security Considerations

- All sensitive data is encrypted at rest
- Network communication uses HTTPS with certificate pinning
- Biometric authentication for sensitive operations
- Secure key storage using Android Keystore
- Regular security audits and dependency updates

## Performance

- **Cold Start**: < 2 seconds
- **Memory Usage**: Optimized for low-end devices
- **Network**: Efficient API calls with caching
- **Database**: Indexed queries for fast data retrieval

## Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Generate test coverage report
./gradlew jacocoTestReport
```

## Build Variants

- **Debug**: Development build with logging
- **Release**: Production build with optimizations
- **Staging**: Testing build with staging API

## Internationalization

- Support for multiple languages
- RTL layout support
- Localized date and number formats
- Currency formatting per region

## Device Support

- **Minimum SDK**: 23 (Android 6.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: ARM64, ARMv7
- **Screen Density**: All densities supported

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

Copyright (c) 2025 Telepesa. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

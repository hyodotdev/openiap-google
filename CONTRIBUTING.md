# Contributing to OpenIAP Android

Thank you for your interest in contributing! We love your input and appreciate your efforts to make OpenIAP better.

## Quick Start

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Run tests (`./gradlew :openiap:test`)
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to your branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## Development Setup

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/openiap-google.git
cd openiap-google

# Open in Android Studio (recommended)
./scripts/open-android-studio.sh

# Or build from CLI
./gradlew :openiap:assemble

# Run unit tests for the library module
./gradlew :openiap:test

# (Optional) Install and run the Example app
./gradlew :Example:installDebug
adb shell am start -n dev.hyo.martie/.MainActivity
```

## Code Style

- Follow the official Kotlin Coding Conventions
- Use meaningful, descriptive names for types, functions, and variables
- Keep functions small and focused
- Add comments when they clarify intent (avoid redundant comments)

### Naming Conventions

- **OpenIap prefix for public models (Android)**
  - Prefix all public model types with `OpenIap`.
  - Examples: `OpenIapProduct`, `OpenIapPurchase`, `OpenIapActiveSubscription`, `OpenIapRequestPurchaseProps`, `OpenIapProductRequest`, `OpenIapReceiptValidationProps`, `OpenIapReceiptValidationResult`.
- Private/internal helper types do not need the prefix.
- When renaming existing types, provide a public typealias from the old name to the new name to preserve source compatibility and migrate usages incrementally when feasible.

## Testing

All new features must include unit tests (JUnit + coroutines test):

```kotlin
@Test
fun yourFeature_isCorrect() = kotlinx.coroutines.test.runTest {
    // Arrange
    // val module = FakeOpenIapModule()

    // Act
    // val result = store.yourMethod()

    // Assert
    // assertEquals(expected, result)
}
```

Run tests locally with:

```bash
./gradlew :openiap:test
```

## Pull Request Guidelines

### ✅ Do

- Write clear PR titles and descriptions
- Include tests for new features
- Update documentation when needed
- Keep changes focused and small

### ❌ Don't

- Mix unrelated changes in one PR
- Break existing tests
- Change code style without discussion
- Include commented-out or dead code

## Commit Messages

Keep them clear and concise:

- `Add purchase error recovery`
- `Fix subscription status check`
- `Update Google Play Billing integration`
- `Refactor transaction handling`

## Release Process (Maintainers Only)

When a PR is merged, maintainers handle releases using semantic versioning (major.minor.patch):

1. Bump version (e.g., `OPENIAP_VERSION` in `gradle.properties` or release property)
2. Update changelog (if applicable)
3. Tag and create a GitHub Release
4. Publish to Maven Central via CI (or `./gradlew publish` if configured)

Availability: artifacts appear on Maven Central shortly after the release propagates.

## Questions?

Feel free to:

- Open an issue for bugs or features
- Start a discussion for questions
- Tag @chan for urgent matters

## License

By contributing, you agree that your contributions will be licensed under the MIT License.


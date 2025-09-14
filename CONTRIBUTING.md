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

### Horizon Quickstart (Local)

The core library ships Play-only by default but supports Horizon if a provider is on the classpath.

- Provider selection is done via `OpenIapStore(context, store?, appId?)` or BuildConfig flags.
- Run Example targeting Horizon/auto:
  - VS Code: use launch config "Android: Run Example (Horizon/Auto)"
  - CLI: `./gradlew :Example:installDebug -PEXAMPLE_OPENIAP_STORE=auto`
  - CLI (force): `./gradlew :Example:installDebug -PEXAMPLE_OPENIAP_STORE=horizon -PEXAMPLE_HORIZON_APP_ID=YOUR_APP_ID`
  - VS Code (force): use launch config "Android: Run Example (Horizon Force)" — it will prompt for `HORIZON_APP_ID` via input box and pass `-PEXAMPLE_HORIZON_APP_ID` to Gradle.
- Notes:
  - Use a Quest device with Meta services; emulators are not supported.
  - If the provider is missing, the factory falls back to Play when using `auto`.

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

## Version Management

This repo uses a single source of truth for the library version via a `VERSION` file, and provides a helper script to keep related files in sync.

- Version source: `VERSION` (root)
- Gradle property: `OPENIAP_VERSION` in `gradle.properties` (kept in sync)
- Docs: dependency snippets in `README.md` (kept in sync)

Update the version using the provided script:

```bash
# Bump by type (three common modes)
./scripts/bump-version.sh patch  # for bug fixes
./scripts/bump-version.sh minor  # for new features
./scripts/bump-version.sh major  # for breaking

# Or set an explicit version
./scripts/bump-version.sh 1.2.3

# Optional flags
./scripts/bump-version.sh patch --no-readme   # skip README sync
./scripts/bump-version.sh patch --no-gradle   # skip Gradle sync
./scripts/bump-version.sh patch --commit      # auto-commit the changes
```

What the script does:

- Writes the new value to `VERSION`
- Updates `OPENIAP_VERSION` in `gradle.properties` (if present)
- Replaces version strings in `README.md` dependency snippets
- Optionally creates a commit when `--commit` is used

Note: Creating remote tags is optional here; keep the `VERSION` file and README in sync. Tagging can be done separately during release if needed.

## Release Process (Maintainers Only)

When a PR is merged, maintainers handle releases using semantic versioning (major.minor.patch):

1. Bump version via `scripts/bump-version <major|minor|patch|x.y.z>`
2. Ensure README snippets reflect the new version (script does this)
3. Update changelog (if applicable)
4. Optionally tag and create a GitHub Release
5. Publish to Maven Central via CI (or `./gradlew publish` if configured)

Availability: artifacts appear on Maven Central shortly after the release propagates.

## Questions?

Feel free to:

- Open an issue for bugs or features
- Start a discussion for questions
- Tag @chan for urgent matters

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

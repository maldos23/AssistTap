# CLAUDE.md

AssistTap: invisible Android app that opens the default voice assistant and finishes. Kotlin, minSdk 26,
target/compileSdk 35, AGP 9 (built-in Kotlin, no `kotlin-android` plugin), Gradle 9.8, JDK 17+.

## Commands

```sh
./gradlew ktlintCheck detekt lint            # static analysis (lint warnings are errors)
./gradlew ktlintFormat                       # auto-fix formatting
./gradlew testDebugUnitTest                  # JUnit 5 + MockK + Robolectric (SDK 34)
./gradlew verifyResolverCoverage             # >= 80 % line coverage gate on resolver logic
./gradlew check                              # all of the above
./gradlew assembleDebug assembleRelease      # release is unsigned unless KEYSTORE_* env vars are set
./gradlew connectedDebugAndroidTest          # needs a running emulator/device
```

## Conventions

- Everything in English: code, comments, commits, docs.
- Conventional Commits; update `CHANGELOG.md` under `Unreleased`.
- No runtime dependencies, no requested permissions, no network. Test-only dependencies go in `gradle/libs.versions.toml`.
- Keep `LaunchActivity` thin; put decisions in `AssistantIntentResolver` / `IntentCandidates` / `AssistantToggle`.
- `AssistantWatcherService` is opt-in; every feature must keep working when it is disabled.
- KDoc on every public class and function.
- Record non-obvious decisions in `docs/ARCHITECTURE.md`.

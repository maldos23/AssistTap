# Testing

Requirements: JDK 17+ and an Android SDK with platform 35 (`sdk.dir` in `local.properties` or `ANDROID_HOME`).

## Unit and Robolectric tests

Both live in `app/src/test` and run together on the JVM:

```sh
./gradlew testDebugUnitTest
```

- `AssistantIntentResolverTest` — JUnit 5 + MockK, pure resolver logic.
- `IntentCandidatesTest`, `LaunchActivityTest` — Robolectric (JUnit 4 via the vintage engine), SDK 34.

Run one class:

```sh
./gradlew testDebugUnitTest --tests "com.example.assisttap.LaunchActivityTest"
```

## Coverage

```sh
./gradlew createDebugUnitTestCoverageReport   # HTML: app/build/reports/coverage/test/debug/index.html
./gradlew verifyResolverCoverage              # fails below 80 % line coverage on the resolver logic
```

`./gradlew check` also runs the coverage gate.

## Instrumented smoke test

Start an emulator (API 30 and 34 are the CI targets) or connect a device, then:

```sh
./gradlew connectedDebugAndroidTest
```

## Static analysis

```sh
./gradlew ktlintCheck detekt lint
./gradlew ktlintFormat   # auto-fix formatting
```

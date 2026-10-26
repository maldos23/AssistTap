# Contributing

1. Fork and create a branch from `main`.
2. Keep the app tiny: no new runtime dependencies, no permissions, no network.
3. Before pushing, run:

   ```sh
   ./gradlew ktlintCheck detekt lint testDebugUnitTest verifyResolverCoverage
   ```

4. Use [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `test:`, `docs:`, `ci:`, `chore:`).
5. Add an entry under `Unreleased` in `CHANGELOG.md`.
6. Open a pull request against `main`; CI also runs the instrumented tests on API 30 and 34.

Device reports (head unit model, Android version, whether it works) are very welcome: add them to the
"Tested devices" table in `README.md`.

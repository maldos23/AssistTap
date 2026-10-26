# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2026-10-25

### Added

- Invisible launcher activity that opens the default voice assistant and finishes.
- Fallback chain: `ACTION_VOICE_COMMAND` → `ACTION_ASSIST` → Google app → "No assistant found" toast.
- Adaptive microphone launcher icon labelled "Assistant".
- Unit, Robolectric and instrumented tests with an 80 % coverage gate on the resolver logic.
- GitHub Actions pipeline for lint, tests, build, instrumented tests and signed releases.

[Unreleased]: https://github.com/maldos23/AssistTap/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/maldos23/AssistTap/releases/tag/v1.0.0

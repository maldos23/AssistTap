# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.1.1] - 2026-10-26

### Fixed

- With the service: presses were slow or misread when another app was busy, because the service read every
  window's content. It now tracks windows from events.
- With the service: a press up to 3 s after opening was ignored even if the assistant never appeared; now 1.5 s.
- Without the service: a press up to 10 s after opening went home even if the assistant had already closed; now 5 s.

## [1.1.0] - 2026-10-25

### Added

- Launching AssistTap while the assistant is open closes it instead of starting a second request.
- Optional accessibility service for exact detection: closes with Back and returns to the previous app; ignores a
  second press while the assistant is still appearing.
- Without the service, a launch within 10 s of opening the assistant closes it by going to the home screen.

## [1.0.0] - 2026-10-25

### Added

- Invisible launcher activity that opens the default voice assistant and finishes.
- Fallback chain: `ACTION_VOICE_COMMAND` → `ACTION_ASSIST` → Google app → "No assistant found" toast.
- Adaptive microphone launcher icon labelled "Assistant".
- Unit, Robolectric and instrumented tests with an 80 % coverage gate on the resolver logic.
- GitHub Actions pipeline for lint, tests, build, instrumented tests and signed releases.

[Unreleased]: https://github.com/maldos23/AssistTap/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/maldos23/AssistTap/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/maldos23/AssistTap/releases/tag/v1.0.0

# AssistTap

[![CI](https://github.com/maldos23/AssistTap/actions/workflows/ci.yml/badge.svg)](https://github.com/maldos23/AssistTap/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A tiny, invisible Android app that opens your default voice assistant (Google Assistant / Gemini) and closes itself.

## Why

Many car head units and steering-wheel button mappers can only **launch an installed app**; they cannot fire arbitrary
Android intents. AssistTap is an installable app (listed as **"Assistant"**) that wraps the assistant intent, so any
"open app" button becomes a voice-assistant button.

- No UI, no flicker, not shown in recents.
- No permissions, no network, no analytics, no runtime dependencies.
- Release APK is about 20 KB.

## Install (sideload)

1. Download `AssistTap-vX.Y.Z.apk` from the [latest release](https://github.com/maldos23/AssistTap/releases/latest).
2. Copy it to the head unit or phone (USB stick, `adb install`, or a browser download).
3. Open the APK with a file manager and allow "Install unknown apps" for that file manager when prompted.
4. Tap the new **Assistant** icon once: your voice assistant should open.

With `adb`:

```sh
adb install AssistTap-v1.0.0.apk
```

## Map a steering-wheel button

The exact menu names vary by head unit; the flow is the same everywhere.

1. Open your head unit's **steering-wheel key** settings (often *Settings → Car settings → Steering wheel keys*, or an
   app such as *SWC*, *Key Mapper*, or *Button Mapper*).
2. Pick the button you want to use and press it so the head unit learns it (some units call this "study" or "learn").
3. Set the button's action to **Launch app** / **Open application**.
4. Choose **Assistant** (AssistTap) from the app list.
5. Save, then press the button: the assistant should start listening.

On a phone, the same works with any button-remapping app that supports "launch app".

## Troubleshooting

| Symptom | Fix |
|---|---|
| Toast "No assistant found" | Install or enable the Google app, then set a default assistant in *Settings → Apps → Default apps → Digital assistant app*. |
| Gemini opens instead of Google Assistant | Expected: `ACTION_VOICE_COMMAND` opens whichever assistant is the device default. Change it in the default-apps settings. |
| Nothing happens on a head unit | Some head units block apps without visible UI. Open an issue with your unit model; see "Known caveats". |
| The app is missing from the head unit's app picker | Reboot the head unit after installing; some pickers cache the app list. |

## Known caveats

- On devices where Gemini replaced Google Assistant, `ACTION_VOICE_COMMAND` opens whichever assistant is set as default.
- Some head units block apps with no visible UI. If you hit this, report it; an optional 1-second splash behind a build
  flag is the planned workaround (not built yet, to keep the app invisible by default).
- Some OEMs restrict background activity starts. AssistTap starts the assistant from a foreground, user-initiated app
  start, so it should work, but it has only been verified on the devices listed below.

### Tested devices

| Device | Android | Result |
|---|---|---|
| Android Emulator (AVD) | 15 | Instrumented smoke test passes |

Please open a PR adding your device.

## Development

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), [docs/TESTING.md](docs/TESTING.md),
[docs/RELEASING.md](docs/RELEASING.md) and [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[MIT](LICENSE)

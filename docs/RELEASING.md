# Releasing

## One-time setup: signing secrets

1. Create a keystore (keep it out of the repository; `*.jks` and `*.keystore` are git-ignored):

   ```sh
   keytool -genkeypair -v -keystore release.jks -alias assisttap -keyalg RSA -keysize 4096 -validity 10000
   ```

2. Add these repository secrets (*Settings → Secrets and variables → Actions*):

   | Secret | Value |
   |---|---|
   | `KEYSTORE_BASE64` | `base64 -i release.jks` output |
   | `KEYSTORE_PASSWORD` | keystore password |
   | `KEY_ALIAS` | key alias (e.g. `assisttap`) |
   | `KEY_PASSWORD` | key password |

The build reads `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD` from the environment. Without
`KEYSTORE_FILE`, `assembleRelease` produces an unsigned APK.

## Release flow

1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Move the `Unreleased` entries in `CHANGELOG.md` under the new version and date.
3. Commit, then tag and push:

   ```sh
   git tag v1.0.0
   git push origin main v1.0.0
   ```

4. The `release` job in `.github/workflows/ci.yml` builds `assembleRelease`, signs it with the secrets and creates a
   GitHub Release with `AssistTap-v1.0.0.apk` attached.
5. Verify the release page has the APK and that `apksigner verify --print-certs` shows your certificate.

## Local signed build

```sh
KEYSTORE_FILE=$PWD/release.jks KEYSTORE_PASSWORD=... KEY_ALIAS=assisttap KEY_PASSWORD=... ./gradlew assembleRelease
```

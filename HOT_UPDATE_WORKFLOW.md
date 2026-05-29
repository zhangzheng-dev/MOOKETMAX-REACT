# Android Hot Update Workflow

This document records the current Android in-app update workflow for the MooketMax project.

## Goal

After the one-time backend upgrade completed on 2026-05-29, future Android update releases should not require:

- modifying backend version constants
- rebuilding the Spring Boot service
- restarting the backend service

Future releases only need a new APK upload, with optional update notes.

## Current Server Rules

The backend `AppController` now reads update packages from:

- `/tmp/mooket-hot-updates`

Recognized APK file name format:

- `mooket-max-<version>-v<versionCode>.apk`

Example:

- `mooket-max-1.0.3-v5.apk`

Optional update notes file:

- `mooket-max-<version>-v<versionCode>.txt`

Example:

- `mooket-max-1.0.3-v5.txt`

The backend automatically chooses the latest package by:

1. highest `versionCode`
2. latest file modified time if version codes are equal

## Release Steps

### 1. Update app version locally

Keep the mobile app version aligned before building:

- `mobile/src/config/env.ts`
- `mobile/android/app/build.gradle`
- `mobile/ios/MooketMobile.xcodeproj/project.pbxproj`

### 2. Build the Android release APK

From `E:\mooketmax\mobile\android`:

```powershell
.\gradlew.bat assembleRelease
```

Output APK:

- `E:\mooketmax\mobile\android\app\build\outputs\apk\release\app-release.apk`

### 3. Upload the hot update package

Use the helper script from the repo root:

```powershell
powershell -ExecutionPolicy Bypass -File E:\mooketmax\publish_hot_update.ps1 -Version 1.0.4 -VersionCode 6 -UpdateContent "1. 修复若干问题`n2. 优化体验"
```

This script will:

1. copy the local release APK to a versioned temporary filename
2. create `/tmp/mooket-hot-updates` if needed
3. upload the APK to the server
4. upload optional update notes text

## Important Notes

- The SSH config used by the helper scripts is under:
  - `C:\Users\zhangzheng\.ssh`
- SSH operations must use the existing SSH skill helper scripts.
- `MSYS_NO_PATHCONV=1` is required for upload commands in this Windows environment.
- This workflow is for Android APK in-app update only.
- iOS still does not support this APK-style update flow.

## Verification

Example version check:

```powershell
curl.exe -i "https://twms.malleeglobal.com/social/api/v1/app/version?versionCode=5&version=1.0.3"
```

Example partial download check:

```powershell
curl.exe -i -r 0-0 "https://twms.malleeglobal.com/social/api/v1/app/download/apk"
```

Expected result:

- version endpoint returns the newest uploaded `version` and `versionCode`
- download endpoint returns the newest uploaded APK filename in `Content-Disposition`

## One-Time History

The one-time backend upgrade enabling this no-restart workflow was completed on:

- `2026-05-29`

After that date, future Android hot-update releases should follow this document instead of editing backend version constants for every release.

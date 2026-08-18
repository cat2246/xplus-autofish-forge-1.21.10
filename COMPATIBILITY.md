# Compatibility and smoke verification

Verification date: 2026-08-18 (Asia/Kuala_Lumpur)

## Target and provenance

- Minecraft: 1.21.10
- Forge: 60.1.9
- Java: Microsoft OpenJDK 21.0.7 (the Minecraft `java-runtime-delta` runtime)
- OptiFine artifact inspected: `OptiFine-1.21.10_HD_U_J7_pre11.jar`
- OptiFine SHA-256: `BF845CFC6A387B0CC879512CAEFA86039FD5CA9E37AA6828A1967577AE96B6D7`
- Upstream: Wudji/XPlus-AutoFish commit `88fd0fc9858b57c8c9e6b26dbb6d47d3dfc0705a`
- Port version: `1.3.7-forge-mc1.21.10`

The live profile was inspected read-only. The exact ForgeOptiFine profile JSON, `TLauncherAdditional.json`, ForgeOptiFine client JAR, and OptiFine JAR were copied to the ignored `work/isolated-optifine/` directory. No game was launched with the live profile as its game directory.

## Forge development client (without OptiFine)

Command:

```text
./gradlew runClient --args=--quickPlaySingleplayer AutoFishVerification
```

The client used the isolated worktree `run/` directory and Java 21. The quick-play world did not exist, so the client reached initialization and stopped; no fishing behavior was exercised. A second `./gradlew runClient` launch produced a Minecraft window titled `Minecraft* Forge 1.21.10`; it was terminated by PID after the title-screen initialization log appeared.

Evidence in `run/logs/debug.log` and `run/logs/latest.log`:

- Forge 60.1.9 and Minecraft 1.21.10 initialized.
- `autofish` was discovered from the development source set.
- `ForgeModXPlusAutofish` was constructed.
- The Mixin platform registered the `autofish` container and both client mixin classes are present in the packaged metadata. No Mixin, missing-class, or event-bus validation failure was reported.
- The implementation source contains the required direct `RegisterKeyMappingsEvent.BUS.addListener(...)` registration.
- The only unrelated startup error was the unauthenticated development Realms feature-flag request; it did not prevent client initialization.

## Feature-surface status

Automated tests cover configuration defaults/constraints and persistence, draft save/cancel/reset semantics, key-press latching, scheduler timing, random delays, and translation-key parity. Source/package inspection covers all fourteen configuration fields, Forge event wiring, Mixin metadata, and required JAR entries.

The following require an in-world or interactive session and were **not exercised** in this smoke run: opening the screen with `V`, widget interaction for all fourteen settings, Reset/Cancel/Done interaction, persistence after a client restart, casting, bite reel-in, recast, sound/motion switching, persistent mode, break protection, multi-rod, auto-turn/reset-view, and ClearLag regex handling. Consequently, this document makes no claim that fishing behavior passed.

## OptiFine relaunch

The exact OptiFine JAR was copied into the isolated worktree `run/mods/` alongside the packaged Forge JAR and launched through the Forge 60.1.9 development client. OptiFine's transformation service was detected and loaded:

```text
OptiFineTransformationService.onLoad
OptiFine ZIP file: .../run/mods/OptiFine-1.21.10_HD_U_J7_pre11.jar
```

The launch was blocked before a usable title screen. The isolated log records repeated `java.io.IOException: Base resource not found: *.class` failures from `optifine.Patcher`, followed by `Failed to complete lifecycle event CONSTRUCT` and a broken Forge mod state. This is an exact compatibility limitation of this dev-launch reproduction; no OptiFine interaction or fishing check was performed, and no claim of OptiFine compatibility is made.

Logs: `run/logs/latest.log` and `run/logs/debug.log` (the latter includes the transformation-service stack trace). The isolated OptiFine copy and profile evidence remain under ignored `work/isolated-optifine/`.

## Build and artifact verification

Build/test command:

```text
./gradlew clean test build verifyJarMetadata
```

This completed successfully with Java 21. `verifyJarMetadata` confirmed Forge `mods.toml`, the Mixin configuration/refmap, both Mixin classes, GPL `LICENSE`, and the icon, and rejected NeoForge/Cloth Config metadata. The verified JAR was copied to `outputs/xplus-autofish-1.3.7-forge-mc1.21.10.jar`; the build and output copies have matching SHA-256 `5A9C740B9D95A23594CA2FCE39493C3D883E3B8876E5EC98D02E41EDCBA888DD`.

The source archive is generated from the committed source tree and excludes Gradle caches, build/run directories, logs, crash reports, IDE files, and generated SDD workspace artifacts.

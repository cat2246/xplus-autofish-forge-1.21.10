# Compatibility and smoke verification

Verification date: 2026-08-18 (Asia/Kuala_Lumpur)

## Target and provenance

- Minecraft 1.21.10
- Forge 60.1.9
- Java Microsoft OpenJDK 21.0.7 (`java-runtime-delta`)
- OptiFine `OptiFine-1.21.10_HD_U_J7_pre11.jar`, SHA-256 `BF845CFC6A387B0CC879512CAEFA86039FD5CA9E37AA6828A1967577AE96B6D7`
- Upstream commit `88fd0fc9858b57c8c9e6b26dbb6d47d3dfc0705a`
- Port version `1.3.7-forge-mc1.21.10`

The live ForgeOptiFine profile, launcher JSON, Forge libraries, and exact OptiFine artifact were inspected read-only. The production-style copy is under ignored `work/production-launch/`; its game directory is `work/production-launch/production-game`. No process used `C:\Users\limwi\AppData\Roaming\.minecraft` as its game directory, and no live-profile files were written.

## Forge-only smoke

Command: `./gradlew runClient` with Java 21. The client reached resource/title-screen initialization in the isolated worktree `run/`; it was then stopped by its isolated process. No world or fishing interaction was exercised.

Immutable evidence captured before later launches:

- `work/evidence/forge-clean-fix-round1-latest.log`
- `work/evidence/forge-clean-fix-round1-debug.log`

The debug log proves Forge 60.1.9/Minecraft 1.21.10 startup, discovery of `autofish`, Mixin platform registration, and construction of `com.wudji.xplusautofish.ForgeModXPlusAutofish`. It contains no Mixin-target, missing-class, or event-bus validation failure. The only startup error is the expected unauthenticated Realms feature-flag request caused by dummy/offline smoke credentials. Direct `RegisterKeyMappingsEvent.BUS.addListener(...)` registration is present in the implementation. `V` interaction, settings controls, persistence, and all in-world fishing behavior remain unverified.

## OptiFine production-profile route

The exact user artifact and profile setup were copied read-only into the isolated production configuration. The direct Java command used `net.minecraftforge.bootstrap.ForgeBootstrap`, the copied Forge/OptiFine libraries, isolated `--gameDir`, live assets read-only, and dummy offline identity arguments; no access token was used or recorded.

The earlier reobfuscated packaged-JAR attempt retained these failures:

- `work/production-launch/evidence/fix-round1-production.stdout.log`
- `work/production-launch/evidence/fix-round1-production.stderr.log`

Those logs show OptiFine loading and the actual Mixin failures `@Mixin target gzo was not found` and `@Mixin target dae was not found`. The previous report's attribution to `optifine.Patcher` as the cause of those Mixin failures was incorrect. The production-profile jar set exposes official `net.minecraft...` classes, so the fix changes the mixins to official-name targets and packages official-name classes with an empty runtime refmap rather than the absent obfuscated `gzo`/`dae` names.

The corrected official-name artifact was then tested in the same isolated configuration. The Mixin target warnings disappeared, but this manually reconstructed launcher route still failed before a usable title screen with OptiFine transformer `Base resource not found: ...class` messages and `IllegalStateException: Field fluid is not private and an instance field`:

- `work/production-launch/evidence/fix-round1-official-packaging.stdout.log`
- `work/production-launch/evidence/fix-round1-official-packaging.stderr.log`

This latter failure is retained as a limitation of the direct classpath reconstruction; it is not evidence that AutoFish fishing behavior works with OptiFine. No OptiFine interaction, configuration-screen, casting, or fishing check was exercised. The live profile baseline without AutoFish did reach OptiFine initialization, but that baseline does not establish mod compatibility.

## Build and deliverables

Final Java 21 command: `./gradlew clean test build verifyJarMetadata` — `BUILD SUCCESSFUL`. Tests cover configuration, persistence, screen-model behavior, key latching, scheduling, delays, translations, event wiring, and package metadata. The verified JAR contains Forge metadata, the Mixin configuration/refmap/classes, GPL `LICENSE`, and the icon, with no NeoForge or Cloth Config metadata.

The output JAR and source ZIP hashes are recorded in the Task 9 report. The source ZIP is generated from the committed source tree and excludes Gradle caches, build/run directories, logs, crash reports, IDE files, `work`, and generated SDD artifacts.

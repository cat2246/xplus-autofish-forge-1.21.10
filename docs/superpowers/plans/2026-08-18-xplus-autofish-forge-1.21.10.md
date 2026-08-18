# XPlus AutoFish Forge 1.21.10 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a feature-complete client-side Forge 60.1.9 port of XPlus AutoFish 1.3.7 for Minecraft 1.21.10 that starts and operates with OptiFine HD U J7 pre11.

**Architecture:** Import upstream commit `88fd0fc` from `Wudji/XPlus-AutoFish` as the gameplay baseline and combine it with the official Forge 60.1.9 MDK. Keep the upstream engine, monitors, scheduler, mixins, assets, translations, and JSON settings semantics; replace NeoForge lifecycle code and Cloth Config with Forge events and a native Minecraft screen.

**Tech Stack:** Java 21, Minecraft 1.21.10 official mappings, Forge 60.1.9, ForgeGradle 7.x, Sponge Mixin, Gson, JUnit Jupiter 5, Gradle Wrapper.

**Spec:** `docs/superpowers/specs/2026-08-18-xplus-autofish-forge-1.21.10-design.md`

## Global Constraints

- Primary runtime is exactly Minecraft 1.21.10, Forge 60.1.9, Java 21, and OptiFine 1.21.10 HD U J7 pre11.
- Preserve upstream commit `88fd0fc` behavior, assets, translations, authorship, and GPL-3.0 license.
- Produce a client-only mod with mod id `autofish` and no Cloth Config runtime dependency.
- Preserve the existing `autofish.config` JSON filename and field names.
- Do not alter the user's live Minecraft installation during build or verification.
- Copy only verified user-facing artifacts into `outputs/`.

## File Structure

The implementation root is `xplus-autofish-forge-1.21.10/`.

- `build.gradle`, `settings.gradle`, `gradle.properties`, `gradle/wrapper/*`: exact Forge 60.1.9 and Java 21 toolchain, tests, Mixin processing, and archive naming.
- `UPSTREAM.md`, `LICENSE`: source provenance, port attribution, and retained GPL-3.0 terms.
- `src/main/java/com/wudji/xplusautofish/ForgeModXPlusAutofish.java`: Forge lifecycle, event wiring, key mapping, and engine ownership only.
- `src/main/java/com/wudji/xplusautofish/XPlusAutofish.java`: retained fishing state machine and gameplay operations.
- `src/main/java/com/wudji/xplusautofish/config/Config.java`: settings values, copying, validation, and upstream defaults.
- `src/main/java/com/wudji/xplusautofish/config/ConfigManager.java`: synchronous startup load plus safe asynchronous persistence.
- `src/main/java/com/wudji/xplusautofish/gui/AutoFishConfigScreen.java`: native Minecraft configuration screen composition.
- `src/main/java/com/wudji/xplusautofish/gui/ConfigDraft.java`: cancel-safe editable settings snapshot independent of widgets.
- `src/main/java/com/wudji/xplusautofish/gui/OptionRow.java`: one native widget row with label, tooltip, reset, and value control.
- `src/main/java/com/wudji/xplusautofish/input/KeyPressLatch.java`: edge-triggered hotkey behavior so holding `V` does not reopen the screen every tick.
- `src/main/java/com/wudji/xplusautofish/scheduler/*`: retained action scheduling plus injectable time/random sources for deterministic tests.
- `src/main/java/com/wudji/xplusautofish/mointor/*`: retained upstream motion and sound detection.
- `src/main/java/com/wudji/xplusautofish/mixin/*`: updated callback owner references and null-safe startup behavior.
- `src/main/resources/META-INF/mods.toml`: Forge/client-only metadata and exact version ranges.
- `src/main/resources/autofish.mixins.json`: Java 21 Mixin configuration and refmap.
- `src/main/resources/assets/autofish/*`: unchanged icon and translations plus any labels needed by the native screen.
- `src/test/java/com/wudji/xplusautofish/config/*`: defaults, constraints, malformed input, and persistence tests.
- `src/test/java/com/wudji/xplusautofish/gui/ConfigDraftTest.java`: save/cancel/reset behavior.
- `src/test/java/com/wudji/xplusautofish/input/KeyPressLatchTest.java`: one-open-per-press behavior.
- `src/test/java/com/wudji/xplusautofish/scheduler/*`: deterministic timing and random-delay tests.
- `COMPATIBILITY.md`: exact installation, verification results, and limitations.

---

### Task 1: Import the licensed upstream snapshot and establish the Forge build

**Files:**
- Create: `xplus-autofish-forge-1.21.10/**`
- Create: `xplus-autofish-forge-1.21.10/UPSTREAM.md`
- Create: `xplus-autofish-forge-1.21.10/build.gradle`
- Create: `xplus-autofish-forge-1.21.10/settings.gradle`
- Create: `xplus-autofish-forge-1.21.10/gradle.properties`
- Create: `.gitignore`

**Interfaces:**
- Consumes: upstream Git commit `88fd0fc`; official Forge MDK artifact `forge-1.21.10-60.1.9-mdk.zip`.
- Produces: a Java 21 ForgeGradle project whose `compileJava`, `processResources`, and `test` tasks run and whose archive base name is `xplus-autofish`.

- [ ] **Step 1: Import upstream sources and record provenance**

Copy `src/main`, `LICENSE`, `gradlew`, `gradlew.bat`, and `gradle/wrapper` from upstream commit `88fd0fc`. Write `UPSTREAM.md` with this exact content:

```markdown
# Upstream source

This Forge 1.21.10 port is derived from XPlus AutoFish by Wudji:
https://github.com/Wudji/XPlus-AutoFish

Baseline commit: 88fd0fc9858b57c8c9e6b26dbb6d47d3dfc0705a
Baseline branch: neoforged-1.21
Upstream version: 1.3.7-neoforged for Minecraft 1.21.10

The upstream and this port are distributed under GPL-3.0. See LICENSE.
```

- [ ] **Step 2: Replace build metadata with the Forge 60.1.9 toolchain**

Use the official MDK settings and these required declarations:

```groovy
plugins {
    id 'java'
    id 'idea'
    id 'eclipse'
    id 'net.minecraftforge.gradle' version '[7.0.3,8)'
}

version = '1.3.7-forge-mc1.21.10'
group = 'com.wudji'
base { archivesName = 'xplus-autofish' }
java.toolchain.languageVersion = JavaLanguageVersion.of(21)

minecraft {
    mappings channel: 'official', version: '1.21.10'
    runs {
        configureEach {
            workingDir = layout.projectDirectory.dir('run')
            systemProperty 'eventbus.api.strictRuntimeChecks', 'true'
            systemProperty 'forge.enabledGameTestNamespaces', 'autofish'
        }
        register('client')
    }
}

repositories {
    minecraft.mavenizer(it)
    maven fg.forgeMaven
    maven fg.minecraftLibsMaven
    mavenCentral()
}

dependencies {
    implementation minecraft.dependency('net.minecraftforge:forge:1.21.10-60.1.9')
    annotationProcessor 'net.minecraftforge:eventbus-validator:7.0.1'
    testImplementation platform('org.junit:junit-bom:5.11.4')
    testImplementation 'org.junit.jupiter:junit-jupiter'
}

tasks.named('test') { useJUnitPlatform() }
tasks.withType(JavaCompile).configureEach { options.encoding = 'UTF-8' }
```

Set `rootProject.name = 'xplus-autofish-forge-1.21.10'` and retain the MDK Gradle performance properties.

- [ ] **Step 3: Compile the imported source against Forge to establish the expected failure**

Run: `./gradlew compileJava`

Expected: FAIL because `net.neoforged.*` and Cloth Config types remain in the imported source.

- [ ] **Step 4: Add repository ignore rules**

Ignore `work/`, `.gradle/`, all `build/` and `run*/` directories, IDE files, and local crash/log output. Do not ignore source resources or Gradle wrapper files.

- [ ] **Step 5: Verify Gradle resolves the exact toolchain**

Run: `./gradlew --version`

Expected: Gradle runs on Java 21 and reports the project without configuration errors.

- [ ] **Step 6: Commit the imported baseline and build definition**

```powershell
git add .gitignore xplus-autofish-forge-1.21.10
git commit -m "build: import XPlus AutoFish 1.21.10 baseline"
```

### Task 2: Make configuration defaults and validation deterministic

**Files:**
- Modify: `xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/config/Config.java`
- Create: `xplus-autofish-forge-1.21.10/src/test/java/com/wudji/xplusautofish/config/ConfigTest.java`

**Interfaces:**
- Consumes: upstream JSON field names and defaults.
- Produces: `Config.copy()`, `Config.copyFrom(Config)`, and `boolean enforceConstraints()` with ranges recast `500..5000`, random `0..75`, reel-in `1..2000`, turn duration `100..5000`, finite turn angle, and non-null regex.

- [ ] **Step 1: Write failing default and clamping tests**

```java
@Test void defaultsMatchUpstream137() {
    Config c = new Config();
    assertAll(
        () -> assertTrue(c.isAutofishEnabled()),
        () -> assertFalse(c.isMultiRod()),
        () -> assertTrue(c.isOpenWaterDetectEnabled()),
        () -> assertEquals(1500L, c.getRecastDelay()),
        () -> assertEquals(50L, c.getRandomDelay()),
        () -> assertEquals(1L, c.getReelInDelay()),
        () -> assertEquals(30.0f, c.getTurnAngle()),
        () -> assertEquals(500, c.getTurnDuration())
    );
}

@Test void constraintsClampEveryNativeScreenRange() {
    Config c = new Config();
    c.setRecastDelay(50);
    c.setRandomDelay(900);
    c.setReelInDelay(-1);
    c.setTurnDuration(99_999);
    c.setTurnAngle(Float.NaN);
    assertTrue(c.enforceConstraints());
    assertAll(
        () -> assertEquals(500L, c.getRecastDelay()),
        () -> assertEquals(75L, c.getRandomDelay()),
        () -> assertEquals(1L, c.getReelInDelay()),
        () -> assertEquals(5000, c.getTurnDuration()),
        () -> assertEquals(30.0f, c.getTurnAngle())
    );
}
```

- [ ] **Step 2: Run the tests and verify the new requirements fail**

Run: `./gradlew test --tests '*ConfigTest'`

Expected: FAIL because complete clamping and copy methods are absent.

- [ ] **Step 3: Implement copying and complete constraints**

Implement `copy()` by creating a new `Config` and calling `copyFrom(this)`. `copyFrom` must assign all fourteen upstream fields. Clamp numeric settings with `Math.clamp` or explicit bounds, restore `30.0f` for a non-finite turn angle, and replace a null regex with an empty string.

- [ ] **Step 4: Run configuration tests**

Run: `./gradlew test --tests '*ConfigTest'`

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/config/Config.java xplus-autofish-forge-1.21.10/src/test/java/com/wudji/xplusautofish/config/ConfigTest.java
git commit -m "test: define AutoFish configuration invariants"
```

### Task 3: Port configuration persistence without NeoForge or Commons IO

**Files:**
- Modify: `xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/config/ConfigManager.java`
- Create: `xplus-autofish-forge-1.21.10/src/test/java/com/wudji/xplusautofish/config/ConfigManagerTest.java`

**Interfaces:**
- Consumes: `Config.copy()`, `Config.copyFrom(Config)`, and a config directory `Path`.
- Produces: `ConfigManager(Path)`, `Config getConfig()`, `void readConfig()`, `void writeConfig()`, and `CompletableFuture<Void> writeConfigAsync()` using UTF-8 and `autofish.config`.

- [ ] **Step 1: Write failing persistence and recovery tests**

```java
@TempDir Path tempDir;

@Test void roundTripsExistingJsonFieldNames() {
    ConfigManager manager = new ConfigManager(tempDir);
    manager.getConfig().setRecastDelay(2222);
    manager.writeConfig();
    ConfigManager reloaded = new ConfigManager(tempDir);
    assertEquals(2222L, reloaded.getConfig().getRecastDelay());
}

@Test void malformedJsonFallsBackToDefaultsAndRewritesFile() throws Exception {
    Files.writeString(tempDir.resolve("autofish.config"), "{not-json", UTF_8);
    ConfigManager manager = new ConfigManager(tempDir);
    assertEquals(1500L, manager.getConfig().getRecastDelay());
    assertDoesNotThrow(() -> JsonParser.parseString(
        Files.readString(tempDir.resolve("autofish.config"), UTF_8)));
}
```

- [ ] **Step 2: Run and verify failure**

Run: `./gradlew test --tests '*ConfigManagerTest'`

Expected: FAIL because the manager still depends on NeoForge `FMLPaths`, Commons IO, and the mod entry point.

- [ ] **Step 3: Implement portable persistence**

Use `Files.createDirectories`, `Files.readString(..., UTF_8)`, and `Files.writeString` to a sibling temporary file followed by `Files.move(..., REPLACE_EXISTING, ATOMIC_MOVE)`, with a non-atomic retry when the file system rejects atomic moves. Constructor loading is synchronous. Use one daemon-thread executor for `writeConfigAsync()` and log failures through `LogUtils.getLogger()` without throwing into the game loop.

- [ ] **Step 4: Run persistence tests**

Run: `./gradlew test --tests '*ConfigManagerTest'`

Expected: PASS for creation, round-trip, clamping, malformed JSON, and null JSON.

- [ ] **Step 5: Commit**

```powershell
git add xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/config/ConfigManager.java xplus-autofish-forge-1.21.10/src/test/java/com/wudji/xplusautofish/config/ConfigManagerTest.java
git commit -m "feat: add safe standalone config persistence"
```

### Task 4: Port Forge lifecycle and edge-triggered input

**Files:**
- Delete: `xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/NeoForgedModXPlusAutofish.java`
- Create: `xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/ForgeModXPlusAutofish.java`
- Create: `xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/input/KeyPressLatch.java`
- Create: `xplus-autofish-forge-1.21.10/src/test/java/com/wudji/xplusautofish/input/KeyPressLatchTest.java`
- Modify: all Java references from `NeoForgedModXPlusAutofish` to `ForgeModXPlusAutofish`.

**Interfaces:**
- Consumes: Forge `FMLJavaModLoadingContext`, `RegisterKeyMappingsEvent`, and `TickEvent.ClientTickEvent.Post` buses.
- Produces: singleton `ForgeModXPlusAutofish`, `getConfigManager()`, `getConfig()`, `getScheduler()`, `getAutofish()`, packet callbacks, and `boolean KeyPressLatch.update(boolean down)`.

- [ ] **Step 1: Write the failing hotkey latch test**

```java
@Test void firesOnceUntilKeyIsReleased() {
    KeyPressLatch latch = new KeyPressLatch();
    assertFalse(latch.update(false));
    assertTrue(latch.update(true));
    assertFalse(latch.update(true));
    assertFalse(latch.update(false));
    assertTrue(latch.update(true));
}
```

- [ ] **Step 2: Run and verify failure**

Run: `./gradlew test --tests '*KeyPressLatchTest'`

Expected: FAIL because `KeyPressLatch` does not exist.

- [ ] **Step 3: Implement the latch**

```java
public final class KeyPressLatch {
    private boolean previouslyDown;

    public boolean update(boolean down) {
        boolean pressed = down && !previouslyDown;
        previouslyDown = down;
        return pressed;
    }
}
```

- [ ] **Step 4: Implement Forge event wiring**

The constructor must accept `FMLJavaModLoadingContext context`, obtain `context.getModBusGroup()`, register client setup with `FMLClientSetupEvent.getBus(modBusGroup)`, register the key with `RegisterKeyMappingsEvent.getBus(modBusGroup)`, and register post-ticks with `TickEvent.ClientTickEvent.Post.BUS`. Construct `ConfigManager(FMLPaths.CONFIGDIR.get())`, scheduler, and engine during client setup. Open the screen only when `KeyPressLatch.update(CONFIG_SCREEN_MAPPING.isDown())` returns true.

- [ ] **Step 5: Compile and run tests**

Run: `./gradlew test compileJava`

Expected: PASS with no `net.neoforged` imports remaining.

- [ ] **Step 6: Commit**

```powershell
git add -A xplus-autofish-forge-1.21.10/src
git commit -m "feat: wire AutoFish into Forge 60 events"
```

### Task 5: Replace Cloth Config with a cancel-safe native settings model

**Files:**
- Create: `xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/gui/ConfigDraft.java`
- Create: `xplus-autofish-forge-1.21.10/src/test/java/com/wudji/xplusautofish/gui/ConfigDraftTest.java`

**Interfaces:**
- Consumes: live `Config` plus upstream defaults.
- Produces: `ConfigDraft(Config)`, `Config values()`, `void reset()`, `void applyTo(Config)`, and `boolean differsFrom(Config)`.

- [ ] **Step 1: Write failing save, cancel, and reset tests**

```java
@Test void draftDoesNotMutateLiveConfigUntilApplied() {
    Config live = new Config();
    ConfigDraft draft = new ConfigDraft(live);
    draft.values().setAutofishEnabled(false);
    assertTrue(live.isAutofishEnabled());
    draft.applyTo(live);
    assertFalse(live.isAutofishEnabled());
}

@Test void resetRestoresEveryUpstreamDefault() {
    Config live = new Config();
    live.setRecastDelay(4000);
    ConfigDraft draft = new ConfigDraft(live);
    draft.reset();
    assertEquals(1500L, draft.values().getRecastDelay());
    assertFalse(draft.differsFrom(new Config()));
}
```

- [ ] **Step 2: Run and verify failure**

Run: `./gradlew test --tests '*ConfigDraftTest'`

Expected: FAIL because `ConfigDraft` does not exist.

- [ ] **Step 3: Implement the draft using deep copies**

Store one private editable copy. `values()` returns it, `reset()` replaces it with `new Config()`, `applyTo` enforces constraints then calls `target.copyFrom(values)`, and `differsFrom` compares all fourteen fields explicitly.

- [ ] **Step 4: Run draft and config tests**

Run: `./gradlew test --tests '*ConfigDraftTest' --tests '*ConfigTest'`

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/gui/ConfigDraft.java xplus-autofish-forge-1.21.10/src/test/java/com/wudji/xplusautofish/gui/ConfigDraftTest.java
git commit -m "test: define native config screen save semantics"
```

### Task 6: Build the complete native Minecraft configuration screen

**Files:**
- Replace: `xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/gui/AutoFishConfigScreen.java`
- Create: `xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/gui/OptionRow.java`
- Modify: `xplus-autofish-forge-1.21.10/src/main/resources/assets/autofish/lang/*.json`

**Interfaces:**
- Consumes: `ConfigDraft`, `ForgeModXPlusAutofish`, existing `options.autofish.*` translations.
- Produces: `static Screen buildScreen(ForgeModXPlusAutofish, Screen)` with all fourteen fields, Basic/Advanced grouping, scrolling, tooltips, reset, Done, and Cancel.

- [ ] **Step 1: Add a resource-parity test**

Add a JUnit test that parses `en_us.json` and asserts the presence of the title, Basic, Advanced, on/off, all fourteen option title keys, Done, Cancel, and Reset translation keys.

- [ ] **Step 2: Run and verify failure**

Run: `./gradlew test --tests '*TranslationParityTest'`

Expected: FAIL for any missing native-screen action key.

- [ ] **Step 3: Implement reusable native rows**

`OptionRow` must render a translatable label and tooltip beside exactly one control. Use cycle buttons for booleans, sliders for the bounded numeric settings, and an edit box for regex and turn angle. Expose narration text and keyboard focus through standard Minecraft widgets; do not add render mixins.

- [ ] **Step 4: Implement all settings and save semantics**

Basic: enable, multi-rod, open-water detection, break protection, persistent mode, auto-turn view, turn angle, and turn duration. Advanced: sound detection, forced multiplayer detection, recast delay, random percentage, reel-in delay, and ClearLag regex. Done applies the draft, calls `setDetection()` if the detection strategy changed, writes asynchronously, and returns to the parent. Cancel returns without mutation. Reset updates every visible widget from `new Config()`.

- [ ] **Step 5: Run tests and compile**

Run: `./gradlew test compileJava`

Expected: PASS and `rg -n 'clothconfig|me\.shedaniel' src` returns no matches.

- [ ] **Step 6: Commit**

```powershell
git add xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/gui xplus-autofish-forge-1.21.10/src/main/resources/assets/autofish/lang xplus-autofish-forge-1.21.10/src/test
git commit -m "feat: replace Cloth Config with native settings UI"
```

### Task 7: Verify scheduler and fishing-engine parity under 1.21.10 mappings

**Files:**
- Modify: `xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/scheduler/Action.java`
- Modify: `xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/scheduler/AutofishScheduler.java`
- Create: `xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/scheduler/RandomDelay.java`
- Modify as compile requires: `XPlusAutofish.java`, `mointor/*.java`
- Create: `xplus-autofish-forge-1.21.10/src/test/java/com/wudji/xplusautofish/scheduler/ActionTest.java`
- Create: `xplus-autofish-forge-1.21.10/src/test/java/com/wudji/xplusautofish/scheduler/RandomDelayTest.java`

**Interfaces:**
- Consumes: `LongSupplier nowMillis`, `DoubleSupplier random`, and the validated config.
- Produces: deterministic one-shot/repeating action timing and `RandomDelay.compute(long base, long percent, DoubleSupplier random)`.

- [ ] **Step 1: Write failing deterministic timing tests**

```java
@Test void oneShotCompletesOnlyAfterDeadline() {
    AtomicLong now = new AtomicLong(1000);
    Action action = new Action(ActionType.RECAST, 500, () -> {}, now::get);
    assertFalse(action.tryExecute());
    now.set(1500);
    assertTrue(action.tryExecute());
}

@Test void randomDelayStaysInsideConfiguredBounds() {
    assertEquals(750L, RandomDelay.compute(1000, 25, () -> 0.0));
    assertEquals(1250L, RandomDelay.compute(1000, 25, () -> 1.0));
}
```

- [ ] **Step 2: Run and verify failure**

Run: `./gradlew test --tests '*scheduler*'`

Expected: FAIL because time and randomness are hard-coded.

- [ ] **Step 3: Inject clocks/randomness without changing production behavior**

Default constructors must delegate to `Util::getMillis` and `Math::random`. Tests use explicit suppliers. Preserve action ordering, repeating action reset, rod switching, break protection, open-water checks including bubble columns, sound/motion detection selection, persistent mode, auto-turn, and the upstream reel/recast formulas.

- [ ] **Step 4: Compile against official 1.21.10 mappings**

Resolve only actual mapping/API changes reported by `compileJava`; do not refactor unrelated gameplay logic.

- [ ] **Step 5: Run all tests**

Run: `./gradlew test`

Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git add xplus-autofish-forge-1.21.10/src/main/java xplus-autofish-forge-1.21.10/src/test
git commit -m "test: preserve AutoFish scheduler and detection behavior"
```

### Task 8: Port Mixins and Forge metadata, then build the reobfuscated JAR

**Files:**
- Modify: `xplus-autofish-forge-1.21.10/src/main/java/com/wudji/xplusautofish/mixin/*.java`
- Replace: `xplus-autofish-forge-1.21.10/src/main/resources/META-INF/neoforge.mods.toml` with `META-INF/mods.toml`
- Modify: `xplus-autofish-forge-1.21.10/src/main/resources/autofish.mixins.json`
- Modify: `xplus-autofish-forge-1.21.10/build.gradle`

**Interfaces:**
- Consumes: `ForgeModXPlusAutofish` callback methods and official 1.21.10 method names.
- Produces: a Forge-discoverable client-only JAR with required Mixins and no NeoForge/Cloth dependency entries.

- [ ] **Step 1: Add a packaged-metadata verification test**

Create a Gradle verification task that opens the built JAR and fails unless it contains `META-INF/mods.toml`, `autofish.mixins.json`, both Mixin classes, the GPL license, and assets; it must also fail if it contains `neoforge.mods.toml` or text matching `cloth_config`.

- [ ] **Step 2: Run and verify failure**

Run: `./gradlew jar verifyJarMetadata`

Expected: FAIL while NeoForge metadata/references remain.

- [ ] **Step 3: Port callbacks and make startup null-safe**

Keep injections into `ClientPacketListener.handleSoundEvent`, `handleSetEntityMotion`, `handleSystemChat`, and `FishingHook.catchingFish`. Before every callback, obtain `ForgeModXPlusAutofish.getInstance()` and skip when it or the engine is not ready. Keep `defaultRequire: 1` so mapping drift fails visibly in development.

- [ ] **Step 4: Write exact Forge metadata**

Set `modLoader="javafml"`, `loaderVersion="[60,)"`, `clientSideOnly=true`, version `1.3.7-forge-mc1.21.10`, GPL-3.0 license, Wudji authorship plus port attribution, Forge dependency `[60.1.9,61)`, and Minecraft dependency `[1.21.10,1.21.11)`. Remove Cloth Config and NeoForge dependencies.

- [ ] **Step 5: Build and inspect the JAR**

Run: `./gradlew clean test build verifyJarMetadata`

Expected: PASS and produce `build/libs/xplus-autofish-1.3.7-forge-mc1.21.10.jar`.

- [ ] **Step 6: Commit**

```powershell
git add -A xplus-autofish-forge-1.21.10
git commit -m "build: package XPlus AutoFish for Forge 1.21.10"
```

### Task 9: Perform isolated Forge and OptiFine smoke verification

**Files:**
- Create: `xplus-autofish-forge-1.21.10/COMPATIBILITY.md`
- Create only after verification: `outputs/xplus-autofish-1.3.7-forge-mc1.21.10.jar`
- Create only after verification: `outputs/xplus-autofish-forge-1.21.10-source.zip`

**Interfaces:**
- Consumes: packaged port JAR, installed Forge 60.1.9 libraries, installed OptiFine J7 pre11 artifact, and an isolated game directory.
- Produces: launch evidence, compatibility notes, and final deliverables without modifying the live profile.

- [ ] **Step 1: Launch the Forge development client without OptiFine**

Run: `./gradlew runClient --args '--quickPlaySingleplayer AutoFishVerification'` when an isolated test world exists; otherwise launch to the title screen and inspect `run/logs/latest.log`.

Expected: Forge discovers `autofish`, all Mixins apply, the title screen loads, and no missing-class or event-bus validation error appears.

- [ ] **Step 2: Manually exercise the feature surface in the isolated client**

Verify one screen open per `V` press, all fourteen settings, Reset, Cancel, Done, config persistence after restart, casting, bite reel-in, recast, sound/motion detection switching, persistent mode, break protection, multi-rod, auto-turn/reset-view, and ClearLag regex handling. Record each result.

- [ ] **Step 3: Add OptiFine to the isolated test environment and relaunch**

Use the user's exact `OptiFine-1.21.10_HD_U_J7_pre11.jar` or the exact launcher transformer artifact in a copied isolated instance. Do not write to `C:\Users\limwi\AppData\Roaming\.minecraft`. Launch Forge 60.1.9 with the packaged port and inspect the isolated `latest.log` and any crash report.

- [ ] **Step 4: Fix only evidenced compatibility problems and rerun verification**

For a Mixin collision, narrow the injection while retaining its callback. For an initialization-order failure, keep callbacks no-op until the singleton is ready. For a rendering conflict, remove custom rendering code from the screen and use standard widgets. Repeat `clean test build verifyJarMetadata` and both smoke launches after every fix.

- [ ] **Step 5: Write compatibility notes with concrete results**

`COMPATIBILITY.md` must list the exact Java, Minecraft, Forge, OptiFine, upstream commit, build command, test command, tested features, log locations, and any observed limitation. Do not state that fishing behavior passed unless it was exercised in a world.

- [ ] **Step 6: Create final deliverables**

Copy the verified JAR into `outputs/`. Create a source ZIP containing the implementation root but excluding `.gradle`, `build`, `run`, logs, crash reports, and IDE files.

- [ ] **Step 7: Run final verification and commit**

Run: `./gradlew clean test build verifyJarMetadata`

Expected: PASS. Compare SHA-256 of the built JAR and `outputs/` JAR; hashes must match.

```powershell
git add xplus-autofish-forge-1.21.10/COMPATIBILITY.md outputs
git commit -m "docs: record Forge and OptiFine verification"
```

## Independent review gate

After implementation, dispatch GPT-5.6 Terra at medium reasoning to review the complete diff against the approved spec and upstream commit `88fd0fc`. The reviewer must check feature parity, Forge 60 event/API usage, Mixin safety, config persistence, native screen completeness, GPL attribution, artifact metadata, OptiFine evidence, and test sufficiency. Route every actionable finding back to GPT-5.6 Luna at high reasoning, then rerun the relevant tests and final build before delivery.

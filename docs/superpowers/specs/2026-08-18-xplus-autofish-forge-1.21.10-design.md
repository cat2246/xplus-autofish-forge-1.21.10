# XPlus AutoFish Forge 1.21.10 Port Design

## Objective

Produce a client-side XPlus AutoFish JAR for Minecraft 1.21.10 and Forge 60.1.9 that preserves the upstream mod's user-facing features and works in the user's current OptiFine J7 pre11 environment.

## Source and licensing

Use the official `Wudji/XPlus-AutoFish` 1.21.10 NeoForge source as the functional baseline. Preserve the upstream GPL-3.0 license, notices, authorship, assets, translations, and attribution. The delivered artifact will be clearly identified as a Forge 1.21.10 port.

## Architecture

- Retain the upstream fishing engine, schedulers, detection modes, mixins, translations, configuration model, and default settings.
- Replace the NeoForge mod entry point, lifecycle hooks, event registration, key mapping registration, and client tick integration with Forge 60.1.9 equivalents.
- Replace the unavailable Cloth Config Forge 1.21.10 dependency with an integrated configuration screen built from Minecraft's native client GUI components.
- Package the result as one client-only Forge JAR targeting Minecraft 1.21.10 and Java 21, with no Cloth Config runtime dependency.
- Avoid rendering hooks or unrelated mixins to reduce the OptiFine compatibility surface.

## Runtime flow

Forge initializes the client mod, loads the saved AutoFish configuration, registers the default `V` configuration hotkey, and attaches the client tick handler. The fishing monitor observes the same bobber motion, splash sound, and network signals used upstream. When a bite is detected, the existing scheduler reels in and recasts using the configured delays and enabled behavior flags.

The configuration screen exposes every upstream setting with equivalent defaults, validation, reset, save, and cancel behavior. Configuration is stored in the Minecraft instance's config directory. Missing, obsolete, malformed, or out-of-range values fall back to upstream defaults or are safely clamped; configuration errors must not prevent Minecraft from reaching the title screen.

The mod remains client-only and does not require server installation.

## Feature parity

The port must preserve all features exposed by the upstream 1.21.10 NeoForge build, including:

- automatic reel-in and recast;
- upstream detection modes and monitor behavior;
- configurable reel and recast timing;
- persistent behavior and all other upstream toggles;
- the configuration hotkey and complete configuration surface;
- existing translations, defaults, assets, and saved-setting semantics where technically compatible.

Visual implementation details of the configuration screen may use native Minecraft widgets, but every setting and action must remain available and understandable.

## Compatibility and failure handling

- Primary target: Minecraft 1.21.10, Forge 60.1.9, Java 21, and OptiFine 1.21.10 HD U J7 pre11.
- Mixin application failures, missing optional integration points, and configuration problems must be logged clearly.
- Compatibility handling must prefer disabling an affected optional behavior over crashing the client.
- The port will not claim compatibility with other Minecraft, Forge, or OptiFine versions unless separately verified.

## Verification

- Compile and package with the Forge 1.21.10 development toolchain.
- Add focused automated tests for configuration defaults, serialization, validation, migration, and scheduler behavior where feasible outside the full game runtime.
- Launch the Forge development client and verify title-screen startup, mod discovery, hotkey registration, configuration editing and persistence, reel/recast behavior, and each upstream detection mode.
- Test the packaged JAR in an isolated copy of the user's Forge 60.1.9 plus OptiFine J7 pre11 environment before touching the live profile.
- Inspect logs for loader errors, mixin failures, missing classes, duplicate registrations, and OptiFine conflicts.
- Conduct an independent code review using GPT-5.6 Terra at medium reasoning. Implementation and review fixes will use GPT-5.6 Luna at high reasoning.

## Deliverables

- Forge 1.21.10 source tree with build configuration and retained license notices.
- Packaged JAR named in the form `xplus-autofish-<version>-forge-mc1.21.10.jar`.
- A concise compatibility and installation note listing the exact verified environment and known limitations.

The live Minecraft installation will not be modified as part of the build. Installing the final JAR requires a separate explicit user request after verification.

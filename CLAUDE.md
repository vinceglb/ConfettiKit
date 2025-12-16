# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the library
./gradlew :confettikit:build

# Run all tests
./gradlew allTests

# Run JVM tests only
./gradlew :confettikit:jvmTest

# Run the sample desktop app
./gradlew :sample:composeApp:run

# Publish to Maven Local (for local testing)
./gradlew publishToMavenLocal
```

## Project Structure

This is a Kotlin Multiplatform library for confetti animations in Compose Multiplatform.

- **confettikit/** - The main library module
  - Targets: Android, iOS (arm64/x64/simulatorArm64), JVM, JS, WASM
  - Uses explicit API mode (`explicitApi()`)
- **sample/composeApp/** - Sample application demonstrating the library

## Architecture

### Core Components

The library follows a particle system architecture:

1. **Party** (`core/Party.kt`) - Configuration object defining how confetti should appear and behave (angle, spread, speed, colors, shapes, emission settings)

2. **PartySystem** (`core/PartySystem.kt`) - Manages the lifecycle of particles for a single Party configuration. Handles particle creation via emitters and updates particles each frame.

3. **Emitter** (`core/emitter/EmitterConfig.kt`) - Controls particle creation rate. Two modes:
   - `Emitter(duration).max(amount)` - Creates fixed number of particles over duration
   - `Emitter(duration).perSecond(amount)` - Creates particles at a steady rate

4. **ConfettiKit** (`compose/ConfettiKit.kt`) - The main Composable entry point. Uses a Canvas with `withInfiniteAnimationFrameMillis` for animation loop. Converts Party configs into PartySystems and renders particles.

### Rendering Flow

`ConfettiKit` composable → creates `PartySystem` per `Party` → `PartyEmitter` generates `Confetti` particles → `render()` updates particle physics each frame → particles drawn to Canvas with rotation/scale transforms

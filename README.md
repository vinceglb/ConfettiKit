<div align="center">
  <img src="https://github.com/user-attachments/assets/68aed8e6-7e8b-4d5d-925e-7b2641df65f8" alt="ConfettiKit for Kotlin Multiplatform and Compose Multiplatform" />

  <br>
 
  <h1>ConfettiKit</h1>
  <p>A lightweight Kotlin/Compose Multiplatform library to add vibrant, customizable confetti animations to your apps.</p>

  <div>
    <img src="https://img.shields.io/maven-central/v/io.github.vinceglb/filekit-core" alt="ConfettiKit Kotlin Maven Version" />
    <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg?logo=android" alt="Badge Android" />
		<img src="https://img.shields.io/badge/Platform-iOS-lightgrey.svg?logo=apple" alt="Badge iOS" />
		<img src="https://img.shields.io/badge/Platform-JVM-8A2BE2.svg?logo=openjdk" alt="Badge JVM" />
    <img src="https://img.shields.io/badge/Platform-WASM%20%2F%20JS-yellow.svg?logo=javascript" alt="Badge WASM / JS" />
  </div>

  <p align="center">
    <a href="#-installation">Installation</a> •
    <a href="#-creating-your-first-celebration">Quick start</a> •
    <a href="#-customizing-your-celebration">Documentation</a> •
    <a href="#-recipes">Recipes</a> •
    <a href="https://github.com/vinceglb/ConfettiKit/tree/main/sample">Sample project</a>
  </p>

  <br>
</div>

## 😎 Credits

> This library is based on the incredible work of [@DanielMartinus](https://github.com/DanielMartinus) on his Android [Konfetti](https://github.com/DanielMartinus/Konfetti) library and has been adapted for Kotlin Multiplatform and Compose Multiplatform.

## 📦 Installation

```kotlin
dependencies {
    implementation("io.github.vinceglb:confettikit:0.1.0")
}
```

## ✨ Creating Your First Celebration

The simplest way to get the party started is:

```kotlin
ConfettiKit(
    modifier = Modifier.fillMaxSize(),
    parties = listOf(
        Party(emitter = Emitter(duration = 5.seconds).perSecond(30))
    )
)
```

That's it! You've got confetti! 🎊

## 🎉 Customizing Your Celebration

A Party configuration object controls every aspect of your confetti animation. While you only need an Emitter to get started, understanding each property helps you create exactly the effect you want.

### 🚀 Motion and Direction

#### 🎯 Controlling the Launch

```kotlin
Party(
    angle = 90,    // Straight up
    spread = 45,   // 45-degree spread
    speed = 30f,   // Initial speed
    maxSpeed = 40f // Maximum speed
)
```

The direction and spread of your confetti is controlled by several properties:

**`angle`**
- Think of this as the direction your confetti launcher is pointing
- 0° points right, 90° points up, 180° points left, 270° points down
- Use convenient presets like Angle.TOP, Angle.RIGHT, Angle.BOTTOM, Angle.LEFT
- Example: angle = 45 launches confetti diagonally up and right

**`spread`**
- Controls how wide your confetti spray pattern is
- Think of it like adjusting a garden sprayer nozzle
- 360° creates a full circular burst
- 1° creates a focused line
- Example: spread = 90 creates a quarter-circle spray pattern

#### 🏃‍♂️ Velocity Control

```kotlin
Party(
    speed = 20f,      // Base speed
    maxSpeed = 30f,   // Maximum speed
    damping = 0.95f   // Speed decay
)
```

Control how fast your confetti moves and how it slows down:

**`speed`**
- The initial velocity of each confetti piece
- Higher values make confetti shoot out faster

**`maxSpeed`**
- Creates natural variation by randomly picking speeds
- Set to -1 to disable the upper limit
- Example: speed = 20f, maxSpeed = 30f means each piece will have a random speed between 20 and 30

**`damping`**
- Controls how quickly confetti slows down
- Values closer to 1 make confetti float longer
- Lower values make it slow down faster
- Example: damping = 0.98f creates a floating effect

### 🎨 Visual Customization

```kotlin
Party(
    colors = listOf(0xfce18a, 0xff726d, 0xf4306d),
    shapes = listOf(Shape.Square, Shape.Circle),
    size = listOf(Size.SMALL, Size.MEDIUM)
)
```

Customize how your confetti looks:

**`colors`**
- List of colors randomly chosen for each piece
- Use your brand colors or theme colors
- Example: colors = listOf(0xFF0000, 0x00FF00) for red and green confetti

**`shapes`**
- Define what shapes your confetti can be
- Example: shapes = listOf(Shape.Circle) for circular confetti only

**`sizes`**
- Control how big your confetti pieces are
- Mix different sizes for more dynamic effects
- Example: size = listOf(Size.SMALL) for subtle, delicate confetti

### ⚡ Animation Lifecycle 

```kotlin
Party(
    timeToLive = 3000,       // 3 seconds lifetime
    fadeOutEnabled = true,   // Fade out at end
    delay = 500,             // Start after 500ms
    rotation = Rotation.enabled()
)
```

Control the timing and behavior of your confetti:

**`timeToLive`**
- How long each piece of confetti exists (in milliseconds)
- Longer times create more overlapping particles
- Example: timeToLive = 5000 makes confetti last for 5 seconds

**`fadeOutEnabled`**
- When true, confetti fades out at the end of its life
- When false, it disappears instantly
- Example: fadeOutEnabled = false for sharp disappearance

**`position`**
- Where confetti spawns from
- Use Position.Absolute(x, y) for exact screen coordinates
- Use Position.Relative(x, y) for responsive positioning (0.0 to 1.0)
- Use Position.between() for random positions
- Example: position = Position.Relative(0.5, 0.0) spawns from top center

### 🌟 Emission Control 

The Emitter controls how many pieces of confetti are created and how often:

```kotlin
// Burst of 100 pieces over 100ms
Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)

// Continuous stream of 30 pieces per second for 5 seconds
Emitter(duration = 5, TimeUnit.SECONDS).perSecond(30)
```

## 📱 Using in Compose

ConfettiKit is designed to work seamlessly with Compose. You can use it as a Composable function in your UI:

```kotlin
ConfettiKit(
    modifier = Modifier.fillMaxSize(),
    parties = parties,
    onParticleSystemEnded = { system, activeSystems -> }
)
```

## 🧑‍🍳 Recipes

### 💥 Explode

Create a burst of confetti that explodes from a single point:

```kotlin
fun explode(): List<Party> {
    return listOf(
        Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100.milliseconds).max(100),
        )
    )
}
```

### 🎊 Parade

Create a parade of confetti that moves from one side of the screen to the other:

```kotlin
fun parade(): List<Party> {
    val party = Party(
        speed = 10f,
        maxSpeed = 30f,
        damping = 0.9f,
        angle = Angle.RIGHT - 45,
        spread = Spread.SMALL,
        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
        emitter = Emitter(duration = 5.seconds).perSecond(30),
        position = Position.Relative(0.0, 0.5)
    )

    return listOf(
        party,
        party.copy(
            angle = party.angle - 90, // flip angle from right to left
            position = Position.Relative(1.0, 0.5)
        ),
    )
}
```

### 🌧️ Rain

Create a gentle rain of confetti that falls from the top of the screen:

```kotlin
fun rain(): List<Party> {
    return listOf(
        Party(
            speed = 0f,
            maxSpeed = 15f,
            damping = 0.9f,
            angle = Angle.BOTTOM,
            spread = Spread.ROUND,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 3.5.seconds).perSecond(100),
            position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0))
        )
    )
}
```

## 🌱 Sample project

Check out the [sample project](https://github.com/vinceglb/ConfettiKit/tree/main/sample) for a complete example of how to use ConfettiKit in your app.

---

Made with ❤️ by Vince

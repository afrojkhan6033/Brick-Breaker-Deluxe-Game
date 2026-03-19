# 🧱 Brick Breaker Deluxe

A feature-rich, classic Brick Breaker arcade game built with Java Swing. Break bricks, collect power-ups, survive multiple levels, and chase the high score!

---

## 📸 Screenshots

> _Add your gameplay screenshots here_

---

## ✨ Features

- 🎮 **Multiple Game States** — Main menu, gameplay, pause, game over, and level complete screens
- 🌌 **Animated Star Background** — Dynamic parallax starfield for visual depth
- 🏗️ **4 Unique Brick Patterns** — Regular, Checkerboard, Pyramid, and Random layouts that cycle across levels
- 💪 **Multi-strength Bricks** — Bricks with varying hit points, color-coded by strength
- ⚡ **6 Power-Up Types**:
  | Power-Up | Effect |
  |---|---|
  | 🟢 Expand Paddle | Makes your paddle wider |
  | 🔴 Shrink Paddle | Makes your paddle narrower |
  | 🔵 Slow Ball | Reduces ball speed |
  | 🟡 Fast Ball | Increases ball speed |
  | 🩷 Extra Life | Grants an additional life |
  | 🔮 Multi Ball | Splits into multiple balls |
- 💥 **Particle Effects** — Color-matched explosion particles on brick destruction
- 📈 **Progressive Difficulty** — More bricks, faster ball speed, and harder patterns as levels increase
- 🏆 **Persistent High Score** — Saves and loads your best score via `highscore.dat`
- 🖥️ **Double Buffering** — Flicker-free rendering using an off-screen `BufferedImage`
- 🎨 **3D Brick Rendering** — Gradient fills with highlight and shadow edges for a polished look

---

## 🕹️ Controls

| Key | Action |
|---|---|
| `←` / `→` Arrow Keys | Move paddle left / right |
| `Enter` | Confirm menu selection / advance |
| `P` | Pause / Resume |
| `Esc` | Return to main menu |
| `↑` / `↓` Arrow Keys | Navigate menu options |

---

## 🗂️ Project Structure

```
brick-breaker-deluxe/
│   highscore.dat          # Persisted high score (auto-generated)
│   pom.xml                # Maven build file
│
└───src/
    └───main/
        └───java/
            └───com/mycompany/brick/
                    GamePlay.java       # Core game loop, rendering, input, power-ups, particles
                    MapGenerator.java   # Brick layout patterns and drawing
                    MyApp.java          # Entry point — sets up the JFrame window
```

---

## 🔧 Prerequisites

- **Java 15** or higher
- **Maven 3.6+**

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/brick-breaker-deluxe.git
cd brick-breaker-deluxe
```

### 2. Build the project

```bash
mvn clean package
```

### 3. Run the game

```bash
java -jar target/Brick-1.0-SNAPSHOT.jar
```

Or run directly from your IDE by executing `MyApp.java`.

---

## 🏗️ How It Works

### Game Loop
`GamePlay` extends `JPanel` and implements `KeyListener` and `ActionListener`. A `javax.swing.Timer` drives the game loop at ~8ms intervals, updating ball physics, power-up positions, particle lifetimes, and triggering repaints.

### Level Generation
`MapGenerator` accepts row count, column count, and current level to generate one of four brick patterns. Brick strength scales with level, and colors are mapped to strength (red → blue → green → gold → purple).

### Collision Detection
The ball checks for collisions against the paddle, the four walls, and each individual brick cell using `java.awt.Rectangle` intersection tests. Brick hits decrement that brick's strength; bricks at zero are removed and may drop a power-up.

### Persistence
High scores are serialized to `highscore.dat` in the working directory using Java's `ObjectOutputStream` / `ObjectInputStream`.

---

## 📦 Dependencies

| Dependency | Purpose |
|---|---|
| `spring-boot-starter` | Base Spring Boot starter (build scaffold) |
| Java AWT / Swing | All rendering and UI |

> **Note:** The game logic relies entirely on Java AWT/Swing. The Spring Boot parent POM is included as a build scaffold but is not used at runtime.

---

## 🛠️ Potential Improvements

- [ ] Implement the **High Scores screen** (menu option currently placeholder)
- [ ] Add **sound effects** and background music
- [ ] Introduce **boss levels** with moving bricks
- [ ] Add a **mouse/touch control** mode
- [ ] Package as a native executable with GraalVM

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---


<!-- Afroj khan
    https://github.com/afrojkhan6033
     -->
## 🙌 Contributing

Pull requests are welcome! For major changes, please open an issue first to discuss what you'd like to change.

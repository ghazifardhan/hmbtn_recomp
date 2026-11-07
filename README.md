# Harvest Moon: Back to Nature - Static Recompilation (Android Port)

Native Android port of **Harvest Moon: Back to Nature (PS1 / SLUS-01115)** powered by Ahead-of-Time (AOT) static binary recompilation (`psxrecomp`).

---

## ⚠️ Legal Disclaimer / Educational Notice

> **THIS REPOSITORY IS STRICTLY FOR RESEARCH AND EDUCATIONAL PURPOSES ONLY.**

1. **No Copyrighted Game Assets or ROMs Distributed:**
   - This repository **does NOT contain any copyrighted game ROMs, ISOs, disc images (`.bin`, `.cue`, `.iso`), sound files, textures, or video cutscenes (`.STR`)**.
   - This repository **does NOT contain proprietary Sony PlayStation BIOS files**. The Android build only includes an open-source cleanroom implementation (`openbios.bin`).
   - All game logic in this repository is represented as static recompiled C source code for research into legacy MIPS R3000A architecture translation to modern ARM64 systems.

2. **Strictly Anti-Piracy:**
   - This project is **NOT created for piracy, commercial distribution, or intellectual property infringement**.
   - To build and run this application, **users must legally own an original retail copy of *Harvest Moon: Back to Nature* for the Sony PlayStation** and dump their own disc image.

3. **Trademarks & Copyrights:**
   - *Harvest Moon*, *Bokujō Monogatari*, and all related names and trademarks are property of **Marvelous Inc.**, **Victor Interactive Software**, and/or their respective licensors.
   - *PlayStation* and *PS1* are registered trademarks of **Sony Interactive Entertainment Inc.**
   - This independent research project is **not affiliated with, authorized, maintained, sponsored, or endorsed** by Marvelous, Victor Interactive, Sony, or any of their affiliates.

---

## 🚀 Features

- **Native ARM64 Execution**: Game code runs directly on CPU via native C compilation (`-O3`), achieving a rock-solid **60 FPS**.
- **Virtual Touch Gamepad**: Custom touch controller overlay with responsive 8-way D-Pad, PSX face buttons (✕, ◯, ▢, △), shoulder buttons (L1/L2/R1/R2), Start/Select, and haptic vibration feedback.
- **Orientation Modes**:
  - **Landscape (Wide)**: Fullscreen wide layout with left/right thumb controls.
  - **Portrait (Tall)**: Classical handheld style — 4:3 game screen on top (1080×810), dedicated gamepad at the bottom (1080×1650).
  - **Auto-Rotate**: Sensor-based dynamic switching.
- **Quick Configuration Menu (⚙)**: On-the-fly toggling of screen orientation, button opacity (0% to 85%), and haptic vibration feedback.

---

## 🛠️ Build Requirements

- **Android SDK** (API Level 34 / Android 14 recommended)
- **Android NDK** (version `26.3.11579264` or compatible)
- **JDK 17+**
- **CMake 3.22+** & **Ninja**

---

## 📦 How to Build and Run

### 1. Clone Repository
```bash
git clone --recurse-submodules <repo-url>
cd hmbtn_recomp
```

### 2. Configure Environment
Copy the environment template and adjust paths to your local Android SDK/NDK:
```bash
cp android-env.sh.example android-env.sh
chmod +x android-env.sh
source ./android-env.sh
```

### 3. Provide Your Own Legally Dumped Game Disc
Place your legally dumped game disc image into `android/app/src/main/assets/`:
- `Harvest Moon - Back to Nature.cue`
- `Harvest Moon - Back to Nature.bin`

### 4. Compile Release APK
```bash
cd android
./gradlew assembleRelease
```
The output APK will be generated at:
```
android/app/build/outputs/apk/release/app-release.apk
```

### 5. Install to Device
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

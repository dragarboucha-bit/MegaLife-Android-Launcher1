# Mega Life Roleplay — Official Android SA-MP Launcher (`mega-life.top:7777`)

This repository contains the complete, production-ready Android Studio + NDK (C++/Java) source code for the **Mega Life Roleplay** launcher, built on the `ALYN-SA-MP-FIX` architecture.

## Key Features Implemented
- **100% Locked Server Connection:** Both the Java layer (`ServerConfig.java`, `GameFileManager.java`) and the native C++ layer (`main.h`, `settings.cpp`, `netgame.cpp`) are hardcoded exclusively to **`mega-life.top:7777`**. All other server lists and third-party server inputs are stripped.
- **Live SA-MP UDP Query Protocol (`SampQueryClient.java`):** Queries `mega-life.top:7777` using authentic SA-MP `'i'` UDP packets every 10 seconds to display live `ONLINE`/`OFFLINE` status, active players, maximum player slots, and latency (ms).
- **Official Discord Integration:** Dedicated Discord card and button opening **`https://discord.gg/nrfgsHCbZ`**.
- **Built-in GTA SA Cache & Custom TXD/DFF Auto-Downloader (`GameFileManager.java`):** Supports one-click downloading and safe ZIP extraction of your GTA SA base files and custom Mega Life `.txd`/`.dff` model packs directly into app-scoped storage (`Android/data/com.megalife.samp/files/`).
- **GitHub Actions Automatic APK Builder (`.github/workflows/build-apk.yml`):** Every push to the `main` branch automatically compiles both `Release` and `Debug` APKs in the cloud using JDK 17, Android SDK 34, and NDK r25c.

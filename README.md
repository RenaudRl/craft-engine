# CraftEngine

![Java Version](https://img.shields.io/badge/Java-21-orange)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Target](https://img.shields.io/badge/Target-Folia%20/%20Paper-blue)

**CraftEngine** is a next-generation, high-performance plugin for custom content implementation, engineered specifically for the **BTC Studio** infrastructure. This fork is optimized for native, blazingly fast integration with **Paper** and **Folia**.

> [!WARNING]
> **PLATFORM COMPATIBILITY NOTICE**
> This fork is **STRICTLY** for Paper 1.21.11+ and Folia 1.21.11+. Legacy compatibility layers have been removed to maximize performance. If you are not running modern Paper/Folia, this plugin **will not function**.

---

## 🚀 Key Features in Detail

### ⚡ Concurrency & Threading (Folia Native)
- **Hardcoded Folia Scheduler**: Deeply integrated `FoliaExecutor` ensures that all tasks (Global & Region-synced) are handled correctly without the overhead of dynamic platform detection.
- **Zero-Overhead Logic**: Slashed unnecessary logic checks for non-Folia platforms, resulting in faster tick-to-task execution.

### 🛠️ Core Optimisations & Debloating
- **Java 21 Native**: Leveraging the latest JVM optimizations for maximum throughput and memory efficiency.
- **Legacy Cleanup**: Removed legacy compatibility code for older versions, focusing exclusively on 1.21.11+.
- **Adventure Native**: Full integration with Kyori Adventure for modern text handling.

### 🌍 Deployment & Startup
- **Streamlined Loading**: Faster startup times through optimized resource discovery and reduced library dependencies.
- **Plug & Play**: Automatic threading context detection for both Paper and Folia environments.

---

## ⚙️ Configuration

CraftEngine is primarily tuned via its configuration files.

### Key Settings
| Key | Default | Description |
|-----|---------|-------------|
| `config_version` | `61` | Internal configuration version. |
| `latest_supported_version` | `1.21.11` | Target Minecraft version. |
| `lang_version` | `44` | Language file version. |

---

## 🛠 Building & Deployment

Requires **Java 21**.

```bash
# Clean and compile the project
./gradlew clean build
```

The artifact will be generated under `/target` folder.

---

## 🤝 Credits & Inspiration
This project draws inspiration from the broader Minecraft development community:
- **[Paper](https://github.com/PaperMC/Paper)** - High-performance Minecraft server.
- **[LuckPerms](https://github.com/LuckPerms/LuckPerms)** - Permission management.
- **[Fabric](https://github.com/FabricMC/fabric)** - Modding toolchain.
- **[packetevents](https://github.com/retrooper/packetevents)** - Packet manipulation.
- **[DataFixerUpper](https://github.com/Mojang/DataFixerUpper)** - Data migration.
- **[ViaVersion](https://github.com/ViaVersion/ViaVersion)** - Protocol translation.

### Core Dependencies
- **[cloud-minecraft](https://github.com/Incendo/cloud-minecraft)** - Command framework.
- **[adventure](https://github.com/KyoriPowered/adventure)** - Text API.
- **[byte-buddy](https://github.com/raphw/byte-buddy)** - Runtime code generation.

---

## 🌍 How to Contribute

### 🔌 New Features & Bug Fixes
If your PR is about a bug fix, it will most likely get merged. If you want to submit a new feature, please make sure to contact us in advance on [Discord](https://discord.com/invite/WVKdaUPR3S).

### 🌍 Translations
1. Clone this repository.
2. Create a new language file in: `/common-files/src/main/resources/translations`
3. Submit a **pull request** to **dev** branch for review.

---

## 📜 License
- **Custom BTC-CORE Patches**: Proprietary to **BTC Studio**.
- **Upstream Source**: GPLv3 license applies to original CraftEngine components.

---

## CraftEngine API

```kotlin
repositories {
    maven("https://repo.momirealms.net/releases/")
}
```
```kotlin
dependencies {
    compileOnly("net.momirealms:craft-engine-core:0.0.66.4")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.66.4")
}
```

---
**Fork maintained by BTCSTUDIO**
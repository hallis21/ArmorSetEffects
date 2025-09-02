# ArmorSetEffects V2

A Minecraft Paper plugin that grants players special effects and bonuses when wearing complete armor sets.

This plugin allows server administrators to define custom armor sets in JSON configuration files. When a player equips all pieces of a defined armor set, they automatically receive the configured effects and bonuses. The plugin supports multiple armor sets with priority systems to handle conflicts when players wear overlapping sets.

## 🚀 Features

- **Modern Architecture**: Built with Kotlin for Paper 1.21.8
- **Flexible Configuration**: JSON-based configuration with hot reloading
- **Priority System**: Armor sets can have different priorities for conflicts
- **Advanced Effects**: Permanent effects, temporary effects, and item-based abilities
- **Adventure Integration**: Rich text formatting with MiniMessage
- **Performance Optimized**: Async operations and efficient event handling
- **Comprehensive Commands**: List and admin commands
- **Migration Support**: Automatic migration from V1 configurations

## 📦 Installation

1. Download the latest `ArmorSetEffectsV2.jar` from releases
2. Place it in your Paper server's `plugins/` folder
3. Start your server - configuration files will be generated automatically
4. Configure your armor sets in `plugins/ArmorSetEffects/sets/`

## 🔧 Building

```bash
./gradlew build fatJar
```

The plugin JAR will be available in `build/libs/`

## 📚 Documentation

- **[Migration Plan](MIGRATION_PLAN.md)** - Complete V1 to V2 migration guide
- **Configuration**: JSON-based with example sets included
- **Commands**: `/armor-list`, `/armor-reload`

## 🔄 V1 to V2 Migration

The legacy V1 Java implementation is preserved in the `LegacyImp` branch. V2 includes automatic configuration migration when upgrading.

## 🏗️ Development

- **Language**: Kotlin 1.9.22
- **Target**: Paper API 1.21.8
- **Build System**: Gradle with Kotlin DSL
- **Dependencies**: Adventure, kotlinx.serialization

## 📖 Legacy V1

The original Java implementation is available in the `LegacyImp` branch for reference and compatibility.

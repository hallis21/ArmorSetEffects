# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ArmorSetEffects V2 is a modern Minecraft Paper plugin written in Kotlin that grants players special effects when wearing complete armor sets. This is a complete rewrite from the original Java V1 implementation.

## Key Technologies

- **Language**: Kotlin 1.9.22 targeting JVM 21
- **Target Platform**: Paper API 1.21.8 (Minecraft)
- **Build System**: Gradle with Kotlin DSL
- **Configuration**: JSON-based with kotlinx.serialization
- **Text System**: Adventure API with MiniMessage support

## Essential Commands

### Building and Development
```bash
# Build plugin JAR with all dependencies
./gradlew build fatJar

# Run tests
./gradlew test

# Apply code formatting (Spotless with ktlint)
./gradlew spotlessApply

# Check code formatting
./gradlew spotlessCheck

# Set up development environment (downloads Paper server, builds plugin)
./gradlew setupDev

# Copy built plugin to test server
./gradlew copyToTestServer

# Start test server (accepts EULA automatically)
./start-server.sh
```

### Testing
- Test files are in `src/test/kotlin/`
- Uses JUnit 5 with MockK for mocking
- MockBukkit for Paper API testing

## Architecture Overview

### Core Components

1. **ArmorSetsPlugin** (`src/main/kotlin/com/hallis21/armorsets/ArmorSetsPlugin.kt`)
   - Main plugin class extending JavaPlugin
   - Initializes all services and managers
   - Handles plugin lifecycle (enable/disable/reload)

2. **Service Layer** (`src/main/kotlin/com/hallis21/armorsets/services/`)
   - `ArmorSetManager`: Loads and manages armor set definitions from JSON/YAML files
   - `EffectManager`: Handles applying/removing effects to players

3. **Configuration** (`src/main/kotlin/com/hallis21/armorsets/config/`)
   - `ConfigManager`: Manages plugin configuration and V1 migration
   - JSON-based configuration with automatic migration from V1

4. **Data Models** (`src/main/kotlin/com/hallis21/armorsets/models/`)
   - `ArmorSet`: Main data class representing an armor set
   - `ArmorPiece`: Represents individual armor pieces
   - `Effects`: Various effect types (permanent, temporary, item-based)

5. **Commands** (`src/main/kotlin/com/hallis21/armorsets/commands/`)
   - `ArmorCommandManager`: Registers and manages all commands
   - Subcommands: list, reload, GUI commands

6. **Event System** (`src/main/kotlin/com/hallis21/armorsets/events/` & `listeners/`)
   - `ArmorEquipEvent`: Custom event for armor changes
   - `ArmorEquipListener`: Listens for armor changes and applies effects

## Configuration Structure

- Main config: `plugins/ArmorSetEffects/config.json`
- Armor sets: `plugins/ArmorSetEffects/sets/` (JSON files)
- Supports both single armor set files and files containing multiple sets
- Automatic V1 configuration migration

## Important Development Notes

### Code Style
- Uses ktlint formatting (enforced via Spotless)
- Kotlin naming conventions
- Uses kotlinx.serialization for JSON/config handling

### Plugin Integration
- Supports PlaceholderAPI (soft dependency)
- Uses Adventure API for rich text formatting
- Permission system: `armorseteffects.*` namespace

### Testing Strategy
- Unit tests for core logic
- MockBukkit for Paper API integration tests
- Test configuration files in test resources

### Performance Considerations
- Async configuration loading where possible
- Efficient armor change detection
- Priority-based armor set resolution
- Effect manager handles cleanup on disable

### Migration Support
- Automatic detection and migration of V1 configurations
- Preserves user data during upgrades
- Maintains backward compatibility for existing server setups

## Common Tasks

### Adding New Commands
1. Create subcommand class in `commands/subcommands/`
2. Implement `ArmorSubCommand` interface
3. Register in `ArmorCommandManager`
4. Add permission to `build.gradle.kts` bukkit configuration

### Adding New Effect Types
1. Create effect data class in `models/Effects.kt`
2. Update `EffectManager` to handle the new effect type
3. Update JSON serialization if needed
4. Add example to default configuration

### Testing Changes
1. Use `./gradlew setupDev` for full environment setup
2. Use `./gradlew copyToTestServer` for quick plugin updates
3. Start test server: `./start-server.sh` (auto-accepts EULA, optimized JVM settings)
4. Test server location: `test-server/` directory

### Development Server Features
- The `start-server.sh` script automatically:
  - Accepts Minecraft EULA
  - Creates developer-friendly server.properties (creative mode, peaceful difficulty)
  - Uses optimized JVM settings for development
  - Runs in nogui mode for better performance
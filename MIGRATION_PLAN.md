# ArmorSetEffects V2 - Complete Migration & Development Plan

## Project Overview

Complete rewrite of ArmorSetEffects plugin using Kotlin, Paper API 1.21.8, and modern development practices.

## Development Environment Setup

### Required Software

1. **Java Development Kit (JDK) 21**

   - Download from: https://adoptium.net/ or https://www.oracle.com/java/
   - Required for Minecraft 1.21.8

2. **Visual Studio Code**

   - Download from: https://code.visualstudio.com/

3. **Gradle 8.5+**

   - Will be handled by Gradle Wrapper in project
   - No manual installation needed

4. **Git**

   - For version control
   - Download from: https://git-scm.com/

5. **Paper Test Server**
   - Download Paper 1.21.8 from: https://papermc.io/downloads/paper

### VS Code Extensions (Required)

1. **Kotlin Language** - Official Kotlin support
2. **Extension Pack for Java** - Red Hat's Java development pack
3. **Gradle for Java** - Gradle support
4. **Debugger for Java** - Java debugging support

### VS Code Extensions (Recommended)

1. **GitLens** - Enhanced Git integration
2. **Error Lens** - Inline error display
3. **TODO Highlight** - Highlight TODO comments
4. **YAML** - For YAML configuration files
5. **JSON Tools** - JSON formatting and validation
6. **Markdown All in One** - Documentation support
7. **Material Icon Theme** - Better file icons
8. **GitHub Copilot** - AI assistance (optional)

## Project Structure

```
ArmorSetEffectsV2/
├── MIGRATION_PLAN.md           # This document
├── README.md                    # Project documentation
├── build.gradle.kts             # Build configuration
├── settings.gradle.kts          # Project settings
├── gradle.properties            # Gradle properties
├── gradlew                      # Gradle wrapper (Unix)
├── gradlew.bat                  # Gradle wrapper (Windows)
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── libs.versions.toml           # Version catalog
├── .vscode/                     # VS Code configuration
│   ├── settings.json
│   ├── launch.json              # Debug configurations
│   └── tasks.json               # Build tasks
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/hallis21/armorsets/
│   │   │       ├── ArmorSetsPlugin.kt
│   │   │       ├── api/
│   │   │       ├── commands/
│   │   │       ├── config/
│   │   │       ├── effects/
│   │   │       ├── listeners/
│   │   │       ├── models/
│   │   │       ├── services/
│   │   │       └── utils/
│   │   └── resources/
│   │       ├── plugin.yml
│   │       ├── config.yml
│   │       └── sets/
│   │           └── examples/
│   └── test/
│       └── kotlin/
└── test-server/                 # Local test server
    └── plugins/
```

## Development Phases

### Phase 1: Initial Setup (Day 1)

**Success Criteria:**

- [ ] Project folder created with proper structure
- [ ] Gradle build system configured with Kotlin DSL
- [ ] Paper API 1.21.8 dependency configured
- [ ] Basic plugin loads successfully on test server
- [ ] VS Code properly configured for Kotlin development

**Tasks:**

1. Create ArmorSetEffectsV2 folder
2. Initialize Gradle project with Kotlin DSL
3. Configure dependencies (Paper API, Kotlin, Adventure)
4. Create minimal plugin class that loads
5. Set up VS Code workspace settings
6. Create test server setup script

### Phase 2: Core Models & Architecture (Day 2-3)

**Success Criteria:**

- [ ] All v1 models converted to Kotlin data classes
- [ ] Proper separation of concerns
- [ ] Type-safe configuration system
- [ ] Unit tests for models

**Tasks:**

1. Create Kotlin data classes:
   - ArmorSet
   - ArmorPiece
   - PermanentEffect
   - ItemEffect
   - EffectTrigger
2. Implement configuration loader with validation
3. Create service layer architecture
4. Add dependency injection setup

### Phase 3: Event System & Listeners (Day 4-5)

**Success Criteria:**

- [ ] Armor equip detection working
- [ ] Effect application/removal working
- [ ] No memory leaks
- [ ] Better performance than v1

**Tasks:**

1. Port ArmorEquipEvent system to Paper API
2. Implement effect manager
3. Add player state tracking
4. Optimize event handling
5. Add metrics collection

### Phase 4: Modern Features (Day 6-7)

**Success Criteria:**

- [ ] Adventure components fully integrated
- [ ] Commands with tab completion
- [ ] GUI for armor sets
- [ ] PlaceholderAPI support

**Tasks:**

1. Migrate to Adventure text components
2. Implement Brigadier command system
3. Create chest GUI for viewing sets
4. Add PlaceholderAPI integration
5. Implement permission system

### Phase 5: Bug Fixes & Improvements (Day 8-9)

**Success Criteria:**

- [ ] Priority system works correctly
- [ ] Metadata matching is reliable
- [ ] No conflicting sets issues
- [ ] Proper reload without data loss

**Tasks:**

1. Fix metadata priority bug
2. Fix interchangeable slots conflict
3. Implement proper reload mechanism
4. Add comprehensive error handling
5. Improve logging system

### Phase 6: Testing & Optimization (Day 10)

**Success Criteria:**

- [ ] All unit tests pass
- [ ] Integration tests pass
- [ ] Performance benchmarks improved
- [ ] No memory leaks detected

**Tasks:**

1. Write comprehensive unit tests
2. Create integration tests
3. Performance profiling
4. Memory leak detection
5. Load testing with multiple players

### Phase 7: Documentation & Release (Day 11-12)

**Success Criteria:**

- [ ] Complete API documentation
- [ ] User guide created
- [ ] Migration guide from v1
- [ ] CI/CD pipeline working

**Tasks:**

1. Write API documentation
2. Create user wiki
3. Make video tutorials
4. Set up GitHub Actions
5. Prepare release package

## Technical Specifications

### Dependencies

- Paper API: 1.21.8-R0.1-SNAPSHOT
- Kotlin: 1.9.22
- Adventure: 4.16.0
- Shadow Plugin: 8.1.1
- JUnit: 5.10.1
- MockBukkit: 3.80.0

### Key Improvements Over V1

1. **Performance**: 50% reduction in event processing time
2. **Memory**: 30% less memory usage
3. **Features**: GUI, PlaceholderAPI, better commands
4. **Code Quality**: Type-safe, null-safe, testable
5. **Maintainability**: Modular architecture, clean code

### Configuration Format

Support both YAML and JSON with schema validation:

```yaml
# config.yml
armor-sets:
  enabled: true
  debug: false
  gui:
    enabled: true
    title: "Armor Sets"
  effects:
    stack-similar: false
    show-particles: true
```

### API Design

Public API for other plugins:

```kotlin
interface ArmorSetsAPI {
    fun getActiveSet(player: Player): ArmorSet?
    fun hasSet(player: Player, setName: String): Boolean
    fun applySet(player: Player, set: ArmorSet)
    fun removeSet(player: Player)
    fun registerCustomEffect(effect: CustomEffect)
}
```

## Quick Start Guide

### 1. Install Prerequisites

```bash
# Install JDK 21
# Windows: Download installer from Adoptium
# macOS: brew install openjdk@21
# Linux: sudo apt install openjdk-21-jdk

# Verify installation
java -version  # Should show version 21
```

### 2. Clone and Setup Project

```bash
# Clone the repository
git clone <repository-url>
cd ArmorSetEffectsV2

# Initialize Gradle wrapper
./gradlew wrapper --gradle-version=8.5

# Build the project
./gradlew build
```

### 3. Configure VS Code

```bash
# Open in VS Code
code .

# Install extensions via command palette (Ctrl+Shift+P)
# Search for "Extensions: Install Extensions"
# Install all required extensions listed above
```

### 4. Run Test Server

```bash
# Download Paper server
./gradlew downloadPaperServer

# Build and copy plugin
./gradlew shadowJar
cp build/libs/ArmorSetEffectsV2-*.jar test-server/plugins/

# Start test server
cd test-server
java -jar paper-1.21.8.jar nogui
```

### 5. Development Workflow

```bash
# Build plugin
./gradlew build

# Run tests
./gradlew test

# Hot reload in test server
./gradlew copyToTestServer
# Then in server console: /reload confirm

# Format code
./gradlew spotlessApply
```

## VS Code Configuration Files

### .vscode/settings.json

```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic",
  "kotlin.compiler.jvm.target": "21",
  "files.exclude": {
    "**/.gradle": true,
    "**/build": true
  },
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.organizeImports": true
  }
}
```

### .vscode/tasks.json

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Build Plugin",
      "type": "shell",
      "command": "./gradlew shadowJar",
      "group": {
        "kind": "build",
        "isDefault": true
      }
    },
    {
      "label": "Run Tests",
      "type": "shell",
      "command": "./gradlew test",
      "group": "test"
    },
    {
      "label": "Deploy to Test Server",
      "type": "shell",
      "command": "./gradlew shadowJar && cp build/libs/*.jar test-server/plugins/",
      "group": "build"
    }
  ]
}
```

### .vscode/launch.json

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug Tests",
      "request": "launch",
      "mainClass": "",
      "projectName": "ArmorSetEffectsV2",
      "args": "",
      "console": "internalConsole"
    }
  ]
}
```

## Migration Notes from V1

### Key Changes

1. **Language**: Java → Kotlin
2. **Build System**: Maven → Gradle
3. **API**: Spigot 1.17.1 → Paper 1.21.8
4. **Text System**: ChatColor → Adventure Components
5. **JSON Library**: Gson → kotlinx.serialization or Jackson

### Known V1 Bugs to Fix

1. **Metadata Priority Bug**: When multiple sets share armor slots but one requires metadata
2. **Interchangeable Slots Conflict**: Uncertain which set applies when slots overlap
3. **Reload Issues**: Player states lost during config reload
4. **Performance**: Redundant checks and inefficient caching

### Backward Compatibility

- Configuration files will be automatically migrated
- Player data will be preserved during upgrade
- Commands will maintain same syntax where possible

## Project Timeline

| Week | Focus Area         | Deliverables                              |
| ---- | ------------------ | ----------------------------------------- |
| 1    | Setup & Models     | Working Gradle project, core data classes |
| 2    | Core Functionality | Event system, effect management           |
| 3    | Modern Features    | Adventure API, commands, GUI              |
| 4    | Polish & Release   | Bug fixes, tests, documentation           |

## Success Metrics

### Feature Completeness

- [ ] 100% V1 feature parity
- [ ] GUI for armor set management
- [ ] PlaceholderAPI integration
- [ ] Advanced configuration options

### Code Quality

- [ ] 80%+ test coverage
- [ ] Zero critical security issues
- [ ] Full API documentation
- [ ] Clean code principles followed

---

**Last Updated**: $(date)
**Version**: 2.0.0-SNAPSHOT
**Author**: hallis21

# Release Guide

This document outlines the process for creating releases of ArmorSetEffects.

## Prerequisites

Before creating a release, ensure you have:

- [x] GitHub CLI (`gh`) installed and authenticated
- [x] Clean working directory (all changes committed)
- [x] All tests passing (`./gradlew test`)
- [x] Code formatting applied (`./gradlew spotlessApply`)
- [x] Updated documentation (README, CHANGELOG, etc.)

## Release Process

### 1. Pre-Release Checklist

- [ ] **Test thoroughly** on a test server with Paper 1.21.8
- [ ] **Review all changes** since the last release
- [ ] **Update version** in `build.gradle.kts` (remove `-SNAPSHOT`)
- [ ] **Update README.md** if needed with new features/changes
- [ ] **Run full test suite**: `./gradlew clean test`
- [ ] **Check code formatting**: `./gradlew spotlessCheck`
- [ ] **Build and test JAR**: `./gradlew clean fatJar`

### 2. Version Management

**Current version format:** `MAJOR.MINOR.PATCH[-SNAPSHOT]`

**Version bumping rules:**
- **MAJOR**: Breaking changes, incompatible API changes
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes, backward compatible

**Example version progression:**
```
2.0.0-SNAPSHOT → 2.0.0 (release) → 2.0.1-SNAPSHOT (next dev)
```

### 3. Create Release

1. **Update version in `build.gradle.kts`**:
   ```kotlin
   version = "2.0.0"  // Remove -SNAPSHOT
   ```

2. **Commit version change**:
   ```bash
   git add build.gradle.kts
   git commit -m "Release version 2.0.0"
   ```

3. **Run the release script**:
   ```bash
   ./release.sh
   ```

The script will:
- ✅ Build the plugin JAR
- ✅ Create and push a git tag
- ✅ Create GitHub release with changelog
- ✅ Upload the JAR as a release asset

### 4. Post-Release

1. **Bump to next development version**:
   ```kotlin
   version = "2.0.1-SNAPSHOT"  // Next patch version
   ```

2. **Commit and push**:
   ```bash
   git add build.gradle.kts
   git commit -m "Bump version to next development cycle"
   git push origin master
   ```

## Manual Release (Alternative)

If you prefer manual control or the script fails:

### Build the JAR
```bash
./gradlew clean fatJar
```

### Create and push tag
```bash
git tag -a v2.0.0 -m "Release version 2.0.0"
git push origin v2.0.0
```

### Create GitHub release
```bash
gh release create v2.0.0 build/libs/ArmorSetEffects-2.0.0.jar \
  --title "ArmorSetEffects v2.0.0" \
  --notes "Release notes here..." \
  --latest
```

## Hotfix Releases

For urgent bug fixes:

1. Create hotfix branch from the release tag:
   ```bash
   git checkout -b hotfix/2.0.1 v2.0.0
   ```

2. Make the fix and test thoroughly

3. Update version to patch release (e.g., `2.0.1`)

4. Follow normal release process

5. Merge hotfix back to main:
   ```bash
   git checkout master
   git merge hotfix/2.0.1
   git branch -d hotfix/2.0.1
   ```

## Release Notes Template

When creating releases, use this template for consistent release notes:

```markdown
## ArmorSetEffects vX.Y.Z

### ✨ New Features
- Feature descriptions

### 🐛 Bug Fixes  
- Bug fix descriptions

### 🔄 Changes
- Breaking changes or modifications

### 📚 Documentation
- Documentation updates

### 🏗️ Internal
- Development/build improvements

---
**Installation:**
1. Download `ArmorSetEffects-X.Y.Z.jar` below
2. Place it in your Paper server's `plugins/` folder  
3. Restart your server

**Requirements:**
- Paper 1.21.8 or later
- Java 21
```

## Troubleshooting

### Common Issues

**"Working directory not clean"**
- Commit or stash all changes before releasing

**"Tag already exists"**  
- Check if the version was already released: `git tag -l`
- Use a different version number

**"JAR file not found"**
- Ensure build completed successfully: `./gradlew clean fatJar`
- Check the `build/libs/` directory

**"Not authenticated with GitHub"**
- Run `gh auth login` and follow the prompts

**Build fails**
- Run tests: `./gradlew test`
- Check formatting: `./gradlew spotlessCheck`
- Fix any issues before releasing

### Getting Help

- Check the [GitHub repository](https://github.com/hallis21/armorseteffects) for issues
- Review build logs in the Actions tab
- Ensure all dependencies are properly configured
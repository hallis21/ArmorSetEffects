#!/bin/bash
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 ArmorSetEffects Release Script${NC}"
echo "=================================="

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo -e "${RED}❌ Error: Not in a git repository${NC}"
    exit 1
fi

# Check if working directory is clean
if [[ -n $(git status --porcelain) ]]; then
    echo -e "${RED}❌ Error: Working directory is not clean. Please commit or stash changes.${NC}"
    git status --short
    exit 1
fi

# Check if gh CLI is installed
if ! command -v gh &> /dev/null; then
    echo -e "${RED}❌ Error: GitHub CLI (gh) is not installed. Please install it from https://cli.github.com${NC}"
    exit 1
fi

# Check if user is logged into gh
if ! gh auth status &> /dev/null; then
    echo -e "${RED}❌ Error: Not logged into GitHub CLI. Run 'gh auth login' first.${NC}"
    exit 1
fi

# Extract version from build.gradle.kts
VERSION=$(grep 'version = ' build.gradle.kts | sed 's/version = "//' | sed 's/"//' | tr -d ' ')
echo -e "${BLUE}📋 Current version: ${VERSION}${NC}"

# Check if version contains SNAPSHOT
if [[ $VERSION == *"SNAPSHOT"* ]]; then
    echo -e "${RED}❌ Error: Version contains SNAPSHOT. Please update version in build.gradle.kts to a release version.${NC}"
    echo "   Current: $VERSION"
    echo "   Example: 2.0.0"
    exit 1
fi

# Check if tag already exists
if git rev-parse "v$VERSION" >/dev/null 2>&1; then
    echo -e "${RED}❌ Error: Tag v$VERSION already exists${NC}"
    exit 1
fi

echo -e "${YELLOW}🔨 Building plugin...${NC}"
./gradlew clean fatJar

# Check if JAR was created
JAR_FILE="build/libs/ArmorSetEffectsV2-${VERSION}.jar"
if [[ ! -f "$JAR_FILE" ]]; then
    echo -e "${RED}❌ Error: JAR file not found at $JAR_FILE${NC}"
    echo "Available files:"
    ls -la build/libs/
    exit 1
fi

echo -e "${GREEN}✅ Build successful: $JAR_FILE${NC}"

# Generate changelog from recent commits
echo -e "${YELLOW}📝 Generating changelog...${NC}"
CHANGELOG=$(git log --oneline --pretty=format:"- %s" HEAD...$(git describe --tags --abbrev=0 2>/dev/null || echo "HEAD~10") 2>/dev/null || git log --oneline --pretty=format:"- %s" -10)

# Create git tag
echo -e "${YELLOW}🏷️  Creating git tag v$VERSION...${NC}"
git tag -a "v$VERSION" -m "Release version $VERSION"

# Push tag to remote
echo -e "${YELLOW}📤 Pushing tag to remote...${NC}"
git push origin "v$VERSION"

# Create GitHub release
echo -e "${YELLOW}🎉 Creating GitHub release...${NC}"
RELEASE_NOTES="## ArmorSetEffects v$VERSION

### Changes
$CHANGELOG

---
**Installation:**
1. Download \`ArmorSetEffectsV2-${VERSION}.jar\` below
2. Place it in your Paper server's \`plugins/\` folder
3. Restart your server

**Requirements:**
- Paper 1.21.8 or later
- Java 21

For configuration help, see the [README](https://github.com/hallis21/armorseteffects/blob/master/README.md)."

gh release create "v$VERSION" "$JAR_FILE" \
    --title "ArmorSetEffects v$VERSION" \
    --notes "$RELEASE_NOTES" \
    --latest

echo -e "${GREEN}🎊 Release v$VERSION created successfully!${NC}"
echo -e "${BLUE}📋 Next steps:${NC}"
echo "   1. Update version in build.gradle.kts to next development version (e.g., ${VERSION%.*}.$((${VERSION##*.}+1))-SNAPSHOT)"
echo "   2. Commit the version bump: git commit -am 'Bump version to next development cycle'"
echo "   3. Push changes: git push origin master"
echo ""
echo -e "${GREEN}✨ Release URL: $(gh release view "v$VERSION" --web --json url --jq '.url')${NC}"
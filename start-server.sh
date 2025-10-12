#!/bin/bash

# ArmorSetEffects V2 Test Server Startup Script
# This script accepts the EULA and starts the Paper server

cd "$(dirname "$0")/test-server"

echo "Starting ArmorSetEffects V2 test server..."

# Accept EULA if not already accepted
if [ ! -f "eula.txt" ] || ! grep -q "eula=true" eula.txt; then
    echo "Accepting Minecraft EULA..."
    echo "# By changing the setting below to TRUE you are indicating your agreement to our EULA (https://aka.ms/MinecraftEULA)." > eula.txt
    echo "# $(date)" >> eula.txt
    echo "eula=true" >> eula.txt
fi

# Check if server JAR exists
if [ ! -f "paper-1.21.8.jar" ]; then
    echo "Error: paper-1.21.8.jar not found!"
    echo "Please run './gradlew setupDev' first to download the server."
    exit 1
fi

# Create server.properties with developer-friendly settings
if [ ! -f "server.properties" ]; then
    echo "Creating developer-friendly server.properties..."
    cat > server.properties << EOF
# ArmorSetEffects V2 Test Server Properties
server-name=ArmorSetEffects Test Server
server-port=25565
gamemode=creative
difficulty=peaceful
max-players=20
online-mode=false
white-list=false
spawn-protection=0
enable-command-block=true
motd=ArmorSetEffects V2 Test Server
EOF
fi

echo "Starting Paper server..."
echo "Press Ctrl+C to stop the server"
echo ""

# Start the server with appropriate JVM options
java -Xms1G -Xmx2G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -jar paper-1.21.8.jar --nogui
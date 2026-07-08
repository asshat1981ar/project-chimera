#!/usr/bin/env bash
# Termux + Shizuku (rish) Build & Deploy Script for Project Chimera
# Run this script directly on the Termux host (outside PRoot)

set -euo pipefail

# ANSI color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'
NC_BOLD='\033[1m'

# Paths
DEBIAN_ROOT="/data/data/com.termux/files/usr/var/lib/proot-distro/containers/debian/rootfs"
PROJECT_DIR_NAME="project-chimera"
GUEST_PROJECT_PATH="/home/dev/${PROJECT_DIR_NAME}"
HOST_PROJECT_PATH="${DEBIAN_ROOT}${GUEST_PROJECT_PATH}"
RISH_PATH="/data/data/com.termux/files/usr/bin/rish"

# Header
echo -e "${CYAN}====================================================${NC}"
echo -e "${NC_BOLD}   Project Chimera - Termux/Shizuku Auto-Deploy     ${NC}"
echo -e "${CYAN}====================================================${NC}"

# 1. Prerequisite Checks
echo -e "${BLUE}[*] Checking prerequisites...${NC}"

if [ ! -f "$RISH_PATH" ]; then
    echo -e "${RED}[ERROR] rish executable not found at $RISH_PATH${NC}"
    echo -e "${YELLOW}Please ensure you have configured Shizuku to 'Use Shizuku in terminal apps'${NC}"
    echo -e "${YELLOW}See details: https://shizuku.rikka.app/guide/setup/${NC}"
    exit 1
fi

# Test Shizuku/rish connection
echo -e "${BLUE}[*] Checking Shizuku service status...${NC}"
if ! "$RISH_PATH" -c "echo 'Shizuku OK'" >/dev/null 2>&1; then
    echo -e "${RED}[ERROR] Shizuku is not running or Termux has not been authorized!${NC}"
    echo -e "${YELLOW}Please ensure:${NC}"
    echo -e "${YELLOW}  1. The Shizuku app is running (check Wireless Debugging / ADB connection)${NC}"
    echo -e "${YELLOW}  2. You have run the 'rish' setup inside Termux${NC}"
    echo -e "${YELLOW}  3. Shizuku has granted terminal access authorization${NC}"
    exit 1
else
    echo -e "${GREEN}[✔] Shizuku service is connected and authorized!${NC}"
fi

# 2. Select Build Variant
echo -e "\n${NC_BOLD}Select Build Variant:${NC}"
echo "  1) mockDebug   (Mock offline AI, debug signature) [Default]"
echo "  2) devDebug    (Dev cloud AI, debug signature)"
echo "  3) prodDebug   (Prod cloud AI, debug signature)"
echo "  4) prodRelease (Prod cloud AI, release signature)"
echo "  5) Custom Gradle Task"
read -rp "Enter choice [1-5, default 1]: " variant_choice
variant_choice="${variant_choice:-1}"

FLAVOR="mock"
BUILD_TYPE="debug"
GRADLE_TASK="assembleMockDebug"

case "$variant_choice" in
    1)
        FLAVOR="mock"
        BUILD_TYPE="debug"
        GRADLE_TASK="assembleMockDebug"
        ;;
    2)
        FLAVOR="dev"
        BUILD_TYPE="debug"
        GRADLE_TASK="assembleDevDebug"
        ;;
    3)
        FLAVOR="prod"
        BUILD_TYPE="debug"
        GRADLE_TASK="assembleProdDebug"
        ;;
    4)
        FLAVOR="prod"
        BUILD_TYPE="release"
        GRADLE_TASK="assembleProdRelease"
        ;;
    5)
        read -rp "Enter Gradle task name (e.g. assembleDevRelease): " custom_task
        GRADLE_TASK="$custom_task"
        # Try to infer details
        if [[ "$GRADLE_TASK" =~ [Aa]ssemble(.*)([Dd]ebug|[Rr]elease) ]]; then
            FLAVOR=$(echo "${BASH_REMATCH[1]}" | tr '[:upper:]' '[:lower:]')
            BUILD_TYPE=$(echo "${BASH_REMATCH[2]}" | tr '[:upper:]' '[:lower:]')
        else
            FLAVOR=""
            BUILD_TYPE=""
        fi
        ;;
    *)
        echo -e "${RED}[ERROR] Invalid choice. Exiting.${NC}"
        exit 1
        ;;
esac

# Resolve final package ID
BASE_PACKAGE="com.chimera.ashes"
if [ -n "$FLAVOR" ] && [ "$FLAVOR" = "mock" ]; then
    if [ "$BUILD_TYPE" = "debug" ]; then
        PACKAGE_ID="${BASE_PACKAGE}.mock.debug"
    else
        PACKAGE_ID="${BASE_PACKAGE}.mock"
    fi
else
    if [ "$BUILD_TYPE" = "debug" ]; then
        PACKAGE_ID="${BASE_PACKAGE}.debug"
    else
        PACKAGE_ID="${BASE_PACKAGE}"
    fi
fi

echo -e "\n${BLUE}[*] Target Package: ${GREEN}${PACKAGE_ID}${NC}"
echo -e "${BLUE}[*] Gradle Task:    ${GREEN}${GRADLE_TASK}${NC}"

# 3. Compile inside Debian PRoot Container
echo -e "\n${BLUE}[*] Compiling APK inside Debian PRoot container...${NC}"

# Run the compilation command inside debian container
# Note: we pass --shared-tmp and run Gradle with JAVA_HOME set to the guest JDK-17
if ! proot-distro login debian --shared-tmp -- /bin/bash -c "
    export JAVA_HOME=/home/dev/.local/jdk-17
    export PATH=\$JAVA_HOME/bin:\$PATH
    cd $GUEST_PROJECT_PATH
    ./gradlew $GRADLE_TASK --console=plain
"; then
    echo -e "${RED}[ERROR] Compilation failed! Check build logs above.${NC}"
    exit 1
fi

echo -e "${GREEN}[✔] Compilation Succeeded!${NC}"

# 4. Find the built APK
APK_DIR="${HOST_PROJECT_PATH}/app/build/outputs/apk"
echo -e "${BLUE}[*] Searching for built APK in $APK_DIR...${NC}"

APK_PATH=""
if [ -n "$FLAVOR" ] && [ -n "$BUILD_TYPE" ]; then
    POTENTIAL_APK="${APK_DIR}/${FLAVOR}/${BUILD_TYPE}/app-${FLAVOR}-${BUILD_TYPE}.apk"
    if [ -f "$POTENTIAL_APK" ]; then
        APK_PATH="$POTENTIAL_APK"
    fi
fi

if [ -z "$APK_PATH" ]; then
    # Fallback search
    APK_PATH=$(find "$APK_DIR" -name "*.apk" -type f | head -n 1)
fi

if [ -z "$APK_PATH" ] || [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}[ERROR] Could not find any built APK in $APK_DIR!${NC}"
    exit 1
fi

echo -e "${GREEN}[✔] Found APK: ${NC_BOLD}$(basename "$APK_PATH")${NC} at $APK_PATH"

# 5. Push APK to /data/local/tmp and Install via Shizuku
TEMP_DEVICE_PATH="/data/local/tmp/chimera_temp.apk"
echo -e "${BLUE}[*] Transferring APK to device (/data/local/tmp)...${NC}"

# Use piped redirection to bypass Termux private folder restriction for 'shell' user
if ! "$RISH_PATH" -c "cat > '$TEMP_DEVICE_PATH'" < "$APK_PATH"; then
    echo -e "${RED}[ERROR] Failed to transfer APK to device!${NC}"
    exit 1
fi

echo -e "${BLUE}[*] Installing APK using Shizuku...${NC}"
if ! "$RISH_PATH" -c "pm install -t -r '$TEMP_DEVICE_PATH'"; then
    echo -e "${RED}[ERROR] Installation failed!${NC}"
    # Cleanup anyway
    "$RISH_PATH" -c "rm -f '$TEMP_DEVICE_PATH'" >/dev/null 2>&1 || true
    exit 1
fi

# Cleanup
echo -e "${BLUE}[*] Cleaning up temporary files...${NC}"
"$RISH_PATH" -c "rm -f '$TEMP_DEVICE_PATH'"

echo -e "${GREEN}[✔] Installation completed successfully!${NC}"

# 6. Launch Application
echo -e "${BLUE}[*] Launching application (${PACKAGE_ID})...${NC}"
LAUNCH_CMD="am start -n ${PACKAGE_ID}/com.chimera.ui.MainActivity"
if ! "$RISH_PATH" -c "$LAUNCH_CMD" >/dev/null; then
    echo -e "${YELLOW}[WARNING] Could not start application automatically.${NC}"
    echo -e "${YELLOW}Please start it manually from your launcher.${NC}"
else
    echo -e "${GREEN}[✔] Application launched!${NC}"
fi

echo -e "${CYAN}====================================================${NC}"
echo -e "${GREEN}${NC_BOLD}               Deployment Complete!                 ${NC_RESET}"
echo -e "${CYAN}====================================================${NC}"

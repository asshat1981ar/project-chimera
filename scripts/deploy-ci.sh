#!/usr/bin/env bash
# Termux + Shizuku (rish) + GitHub CI/CD APK Downloader and Installer
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

# Paths & Settings
RISH_PATH="/data/data/com.termux/files/usr/bin/rish"
REPO="asshat1981ar/project-chimera"
WORKFLOW="android.yml"
TMP_DOWNLOAD_DIR="/data/data/com.termux/files/home/.chimera_ci_tmp"

# Header
echo -e "${CYAN}====================================================${NC}"
echo -e "${NC_BOLD}  Project Chimera - GitHub Actions & Shizuku Deploy ${NC}"
echo -e "${CYAN}====================================================${NC}"

# 1. Prerequisite Checks
echo -e "${BLUE}[*] Checking prerequisites...${NC}"

# Check gh CLI
if ! command -v gh >/dev/null 2>&1; then
    echo -e "${RED}[ERROR] GitHub CLI ('gh') is not installed in Termux.${NC}"
    echo -e "${YELLOW}Please run: pkg install gh${NC}"
    exit 1
fi

# Check gh Auth
if ! gh auth status >/dev/null 2>&1; then
    echo -e "${RED}[ERROR] GitHub CLI is not authenticated.${NC}"
    echo -e "${YELLOW}Please run: gh auth login${NC}"
    exit 1
fi

# Check rish
if [ ! -f "$RISH_PATH" ]; then
    echo -e "${RED}[ERROR] rish executable not found at $RISH_PATH${NC}"
    echo -e "${YELLOW}Please ensure you have configured Shizuku to 'Use Shizuku in terminal apps'${NC}"
    exit 1
fi

# Test Shizuku/rish connection
echo -e "${BLUE}[*] Checking Shizuku service status...${NC}"
if ! "$RISH_PATH" -c "echo 'Shizuku OK'" >/dev/null 2>&1; then
    echo -e "${RED}[ERROR] Shizuku is not running or Termux has not been authorized!${NC}"
    echo -e "${YELLOW}Please start Shizuku and grant terminal permission.${NC}"
    exit 1
else
    echo -e "${GREEN}[✔] Shizuku service is connected!${NC}"
fi

# 2. Select Artifact Type
echo -e "\n${NC_BOLD}Select APK Type to Download:${NC}"
echo "  1) debug   (mockDebug - Offline AI, mock data) [Default]"
echo "  2) beta    (prodBeta - Cloud AI, production staging)"
echo "  3) demo    (mockDemo - Offline AI, demo mode)"
echo "  4) release (prodRelease - Signed Production Release)"
read -rp "Enter choice [1-4, default 1]: " choice
choice="${choice:-1}"

case "$choice" in
    1)
        ARTIFACT_NAME="chimera-debug-apk"
        PACKAGE_ID="com.chimera.ashes.mock.debug"
        ;;
    2)
        ARTIFACT_NAME="chimera-beta-apk"
        PACKAGE_ID="com.chimera.ashes.beta"
        ;;
    3)
        ARTIFACT_NAME="chimera-demo-apk"
        PACKAGE_ID="com.chimera.ashes.mock.demo"
        ;;
    4)
        ARTIFACT_NAME="chimera-release-apk"
        PACKAGE_ID="com.chimera.ashes"
        ;;
    *)
        echo -e "${RED}[ERROR] Invalid choice. Exiting.${NC}"
        exit 1
        ;;
esac

# 3. Retrieve Latest Runs
echo -e "\n${BLUE}[*] Fetching recent CI/CD runs for workflow: $WORKFLOW...${NC}"
# Get runs and print them cleanly to the user
gh run list --repo "$REPO" --workflow "$WORKFLOW" --limit 5

echo -e "\n${NC_BOLD}Select GitHub Actions Run ID to download:${NC}"
echo -e "Press ${GREEN}[Enter]${NC} to download from the latest run listed above."
read -rp "Run ID (or empty for latest): " run_id

if [ -z "$run_id" ]; then
    # Grab the database ID of the first (latest) run
    run_id=$(gh run list --repo "$REPO" --workflow "$WORKFLOW" --limit 1 --json databaseId --jq '.[0].databaseId')
    if [ -z "$run_id" ] || [ "$run_id" = "null" ]; then
        echo -e "${RED}[ERROR] No runs found for workflow $WORKFLOW.${NC}"
        exit 1
    fi
    echo -e "${BLUE}[*] Selecting latest run ID: ${GREEN}${run_id}${NC}"
fi

# 4. Download the Artifact
rm -rf "$TMP_DOWNLOAD_DIR"
mkdir -p "$TMP_DOWNLOAD_DIR"

echo -e "\n${BLUE}[*] Downloading artifact '${ARTIFACT_NAME}' from run ${run_id}...${NC}"
if ! gh run download "$run_id" --repo "$REPO" --name "$ARTIFACT_NAME" --dir "$TMP_DOWNLOAD_DIR"; then
    echo -e "${RED}[ERROR] Failed to download artifact. Make sure the run succeeded and artifact has not expired.${NC}"
    rm -rf "$TMP_DOWNLOAD_DIR"
    exit 1
fi

# Locate APK
APK_PATH=$(find "$TMP_DOWNLOAD_DIR" -name "*.apk" -type f | head -n 1)
if [ -z "$APK_PATH" ] || [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}[ERROR] No APK file found in downloaded artifact!${NC}"
    rm -rf "$TMP_DOWNLOAD_DIR"
    exit 1
fi

echo -e "${GREEN}[✔] Downloaded APK: ${NC_BOLD}$(basename "$APK_PATH")${NC}"

# 5. Push to Device & Install via Shizuku
TEMP_DEVICE_PATH="/data/local/tmp/chimera_ci_temp.apk"
echo -e "${BLUE}[*] Transferring APK to device (/data/local/tmp)...${NC}"

# Pipe stdin to bypass folder read permissions for the shell user
if ! "$RISH_PATH" -c "cat > '$TEMP_DEVICE_PATH'" < "$APK_PATH"; then
    echo -e "${RED}[ERROR] Failed to transfer APK to device!${NC}"
    rm -rf "$TMP_DOWNLOAD_DIR"
    exit 1
fi

echo -e "${BLUE}[*] Installing APK via Shizuku...${NC}"
if ! "$RISH_PATH" -c "pm install -t -r '$TEMP_DEVICE_PATH'"; then
    echo -e "${RED}[ERROR] Installation failed!${NC}"
    "$RISH_PATH" -c "rm -f '$TEMP_DEVICE_PATH'" >/dev/null 2>&1 || true
    rm -rf "$TMP_DOWNLOAD_DIR"
    exit 1
fi

# Cleanup
echo -e "${BLUE}[*] Cleaning up temporary files...${NC}"
"$RISH_PATH" -c "rm -f '$TEMP_DEVICE_PATH'"
rm -rf "$TMP_DOWNLOAD_DIR"

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

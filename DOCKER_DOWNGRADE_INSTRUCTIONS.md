# Docker Desktop Downgrade Instructions - Mac Silicon

## Problem
Docker Desktop 29.1.2 (v4.54.0) has API compatibility issues with TestContainers, returning malformed JSON from `/info` endpoint.

## Solution
Downgrade to Docker Desktop 4.53.1 (last known stable version for TestContainers)

## Step-by-Step Instructions

### 1. Download Docker Desktop 4.53.1 for Apple Silicon
```bash
# Download Docker Desktop 4.53.1 for ARM64 (Apple Silicon)
curl -L -o ~/Downloads/Docker-4.53.1.dmg \
  "https://desktop.docker.com/mac/main/arm64/175267/Docker.dmg"
```

**Manual Download:** [Docker Desktop 4.53.1 Release Notes](https://docs.docker.com/desktop/release-notes/#4531)

### 2. Quit Current Docker Desktop
```bash
# Quit Docker Desktop completely
osascript -e 'quit app "Docker"'

# Wait a few seconds for Docker to fully stop
sleep 5

# Verify Docker processes are stopped
ps aux | grep -i docker | grep -v grep
```

### 3. Backup Your Docker Data (Optional but Recommended)
```bash
# Backup containers and images
docker save $(docker images -q) -o ~/Downloads/docker-images-backup.tar

# Or use Docker Desktop's built-in export
# Settings → Troubleshoot → Clean / Purge data → Export
```

### 4. Uninstall Current Docker Desktop
```bash
# Remove Docker.app
sudo rm -rf /Applications/Docker.app

# Remove CLI tools
sudo rm -f /usr/local/bin/docker
sudo rm -f /usr/local/bin/docker-compose
sudo rm -f /usr/local/bin/docker-credential-desktop
sudo rm -f /usr/local/bin/docker-credential-ecr-login
sudo rm -f /usr/local/bin/docker-credential-osxkeychain
sudo rm -f /usr/local/bin/kubectl

# Optional: Clean install - removes ALL Docker data
# WARNING: This deletes all containers, images, and volumes
# rm -rf ~/Library/Group\ Containers/group.com.docker
# rm -rf ~/Library/Containers/com.docker.docker
# rm -rf ~/Library/Application\ Support/Docker\ Desktop
# rm -rf ~/.docker
```

### 5. Install Docker Desktop 4.53.1
```bash
# Mount the DMG
hdiutil attach ~/Downloads/Docker-4.53.1.dmg

# Copy to Applications
sudo cp -R /Volumes/Docker/Docker.app /Applications/

# Unmount the DMG
hdiutil detach /Volumes/Docker

# Start Docker Desktop
open -a Docker
```

### 6. Configure Docker Desktop
1. Wait for Docker Desktop to start (you'll see the whale icon in menu bar)
2. Accept the terms and conditions if prompted
3. Go to **Settings** (gear icon)
4. **General** tab:
   - ✅ Enable "Use Docker Compose V2"
   - ❌ **UNCHECK** "Automatically check for updates"
5. **Resources** tab: Adjust CPU, Memory, Disk as needed
6. Click **Apply & restart**

### 7. Verify Installation
```bash
# Check Docker version
docker --version
# Should show: Docker version 27.x.x (not 29.x.x)

# Check Docker Desktop version
# Click Docker menu bar icon → About Docker Desktop
# Should show: Docker Desktop 4.53.1

# Verify Docker is working
docker ps
docker info
```

### 8. Test with TestContainers
```bash
# Return to your project
cd /Users/alokkulkarni/Documents/Development/TestContainers/beneficiaries

# Run integration test
mvn test -Dtest=BeneficiariesApplicationIntegrationTest

# Should now work without Docker API errors
```

## Alternative: Use Colima Instead

If downgrading doesn't work, consider using Colima as an alternative:

```bash
# Install Colima
brew install colima

# Start Colima
colima start --arch aarch64 --vm-type=vz --vz-rosetta

# Colima is compatible with TestContainers
# Update ~/.testcontainers.properties:
# docker.host=unix://${HOME}/.colima/default/docker.sock
```

## Troubleshooting

### Docker Desktop won't start after downgrade
```bash
# Reset Docker Desktop
rm -rf ~/Library/Group\ Containers/group.com.docker
rm -rf ~/Library/Containers/com.docker.docker

# Reinstall
open -a Docker
```

### Permission errors
```bash
# Fix permissions
sudo chown -R $(whoami) ~/.docker
sudo chown -R $(whomai) ~/Library/Containers/com.docker.docker
```

### TestContainers still fails
```bash
# Clear TestContainers cache
rm -rf ~/.testcontainers.properties

# Restart terminal to clear environment variables
exec zsh

# Try running tests again
```

## Important Notes

1. **Disable Auto-Updates**: Critical to prevent automatic upgrade back to 29.x
2. **Known Working Versions**: 4.53.1, 4.52.x, 4.51.x all work with TestContainers
3. **Docker Engine Version**: 4.53.1 uses Docker Engine 27.x (not 29.x)
4. **Backup First**: Always backup important containers/images before downgrading

## Links

- [Docker Desktop Release Notes](https://docs.docker.com/desktop/release-notes/)
- [Docker Desktop for Mac](https://docs.docker.com/desktop/install/mac-install/)
- [TestContainers Documentation](https://java.testcontainers.org/)

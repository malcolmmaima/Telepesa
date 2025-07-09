#!/bin/bash

# Telepesa Shared Libraries Build Script
# Builds and installs all shared library modules

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_status "Building Telepesa Shared Libraries..."

# Array of shared library modules
MODULES=(
    "common-exceptions"
    "common-models" 
    "security-utils"
)

# Build each module
for module in "${MODULES[@]}"; do
    if [[ -d "$module" ]]; then
        print_status "Building $module..."
        
        cd "$module"
        
        if mvn clean install -DskipTests; then
            print_success "$module built and installed successfully"
        else
            print_error "Failed to build $module"
            exit 1
        fi
        
        cd ..
    else
        print_error "Module directory $module not found"
        exit 1
    fi
done

print_success "All shared libraries built and installed successfully!" 
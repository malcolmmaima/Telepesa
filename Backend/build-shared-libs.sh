#!/bin/bash

# Telepesa Shared Libraries Build Script
# This script builds and installs all shared libraries required by microservices

set -e  # Exit on any error

echo "🏗️  Building Telepesa Shared Libraries..."
echo "========================================"

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SHARED_LIBS_DIR="$SCRIPT_DIR/shared-libraries"

# Check if shared-libraries directory exists
if [ ! -d "$SHARED_LIBS_DIR" ]; then
    echo "❌ Error: shared-libraries directory not found at $SHARED_LIBS_DIR"
    exit 1
fi

cd "$SHARED_LIBS_DIR"

# Array of libraries in dependency order
LIBRARIES=("common-exceptions" "common-models" "security-utils")

echo "📦 Building libraries in dependency order..."

for lib in "${LIBRARIES[@]}"; do
    if [ -d "$lib" ]; then
        echo ""
        echo "🔨 Building $lib..."
        echo "------------------------"
        cd "$lib"
        
        # Clean and install
        mvn clean install -DskipTests -q
        
        if [ $? -eq 0 ]; then
            echo "✅ $lib built successfully"
        else
            echo "❌ Failed to build $lib"
            exit 1
        fi
        
        cd ..
    else
        echo "⚠️  Warning: Directory $lib not found, skipping..."
    fi
done

echo ""
echo "🎉 All shared libraries built successfully!"
echo "========================================"
echo "✅ common-exceptions: Built and installed"
echo "✅ common-models: Built and installed" 
echo "✅ security-utils: Built and installed"
echo ""
echo "You can now build and run the microservices:"
echo "  cd user-service && mvn spring-boot:run"
echo "  cd account-service && mvn spring-boot:run"
echo "" 
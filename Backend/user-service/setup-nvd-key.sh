#!/bin/bash

# Setup script for NVD API Key
# This script helps configure the NVD API key for local development

echo "🔑 NVD API Key Setup for Telepesa Banking Application"
echo "=================================================="

# Check if API key is provided as argument
if [ $# -eq 0 ]; then
    echo "Usage: ./setup-nvd-key.sh <your-nvd-api-key>"
    echo ""
    echo "To get your NVD API key:"
    echo "1. Visit: https://nvd.nist.gov/developers/request-an-api-key"
    echo "2. Request an API key (it's free)"
    echo "3. Run this script with your key: ./setup-nvd-key.sh YOUR_KEY_HERE"
    echo ""
    echo "Alternatively, you can set it manually:"
    echo "export NVD_API_KEY=your-key-here"
    exit 1
fi

API_KEY=$1

# Add to .bashrc/.zshrc for persistence
if [[ "$SHELL" == *"zsh"* ]]; then
    SHELL_RC="$HOME/.zshrc"
elif [[ "$SHELL" == *"bash"* ]]; then
    SHELL_RC="$HOME/.bashrc"
else
    SHELL_RC="$HOME/.profile"
fi

echo "Setting up NVD API key..."

# Check if already exists in shell config
if grep -q "NVD_API_KEY" "$SHELL_RC"; then
    echo "⚠️  NVD_API_KEY already exists in $SHELL_RC"
    echo "Please manually update it or remove the existing line first."
else
    echo "export NVD_API_KEY=$API_KEY" >> "$SHELL_RC"
    echo "✅ Added NVD_API_KEY to $SHELL_RC"
fi

# Set for current session
export NVD_API_KEY=$API_KEY
echo "✅ Set NVD_API_KEY for current session"

# Test the setup
echo ""
echo "🧪 Testing NVD API key setup..."
if [ -n "$NVD_API_KEY" ]; then
    echo "✅ NVD_API_KEY is set: ${NVD_API_KEY:0:8}..."
    
    # Test with a simple dependency check
    echo "🔍 Running test dependency check..."
    mvn org.owasp:dependency-check-maven:check \
        -DnvdApiKey=$NVD_API_KEY \
        -DfailBuildOnCVSS=10 \
        -DcveValidForHours=1 \
        -DfailOnError=false \
        -DautoUpdate=false \
        -Dformats=TEXT \
        -DsuppressionFile=dependency-check-suppressions.xml > /dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        echo "✅ NVD API key is working correctly!"
    else
        echo "⚠️  There might be an issue with the API key. Please check it's valid."
    fi
else
    echo "❌ Failed to set NVD_API_KEY"
fi

echo ""
echo "🔄 To apply changes to your shell, run: source $SHELL_RC"
echo "💡 Or start a new terminal session"
echo ""
echo "🚀 You can now run dependency checks with enhanced NVD data:"
echo "   mvn org.owasp:dependency-check-maven:check" 
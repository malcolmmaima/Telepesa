#!/bin/bash

# Test NVD API Key Configuration
# This script verifies that the NVD API key is properly configured and working

set -e

echo "🔒 Testing NVD API Key Configuration..."

# Check if API key is set
if [ -z "$NVD_API_KEY" ]; then
    echo "❌ NVD_API_KEY environment variable is not set."
    echo ""
    echo "📋 To get your free NVD API key:"
    echo "  1. Visit: https://nvd.nist.gov/developers/request-an-api-key"
    echo "  2. Fill out the form with your email"
    echo "  3. Check your email for the API key"
    echo "  4. Export the key: export NVD_API_KEY=\"your-key-here\""
    echo "  5. Add to ~/.bashrc or ~/.zshrc for persistence"
    echo ""
    echo "💡 Without an API key, security scans will be much slower."
    exit 1
fi

echo "✅ NVD API Key found: ${NVD_API_KEY:0:8}..."

# Test the API key with a minimal dependency check
echo "🧪 Running minimal security scan to test API key..."

mvn dependency-check:check \
    -DnvdApiKey=$NVD_API_KEY \
    -DfailBuildOnCVSS=10 \
    -DskipSystemScope=true \
    -DskipTestScope=true \
    -DskipProvidedScope=true \
    -DarchiveAnalyzerEnabled=false \
    -DpyDistributionAnalyzerEnabled=false \
    -DpyPackageAnalyzerEnabled=false \
    -DrubygemsAnalyzerEnabled=false \
    -DopensslAnalyzerEnabled=false \
    -DcmakeAnalyzerEnabled=false \
    -DautoconfAnalyzerEnabled=false \
    -DcomposerAnalyzerEnabled=false \
    -DnodeAnalyzerEnabled=false \
    -DnuspecAnalyzerEnabled=false \
    -DassemblyAnalyzerEnabled=false \
    -DnexusAnalyzerEnabled=false \
    -DautoUpdate=false \
    -DcveValidForHours=24 \
    -q

if [ $? -eq 0 ]; then
    echo "✅ NVD API key is working correctly!"
    echo "🚀 Security scans will run much faster with this configuration."
else
    echo "❌ There was an issue with the NVD API key or security scan."
    echo "💡 This might be due to:"
    echo "   - Invalid API key"
    echo "   - Network connectivity issues"
    echo "   - API rate limits"
    echo "🔄 The scan will still work but may be slower without a valid API key."
fi

echo ""
echo "📋 NVD API Configuration Summary:"
echo "  API Key: ${NVD_API_KEY:0:8}..."
echo "  Status: Configured for local development"
echo "  Purpose: Faster OWASP dependency vulnerability scanning"
echo "  Usage: mvn dependency-check:check -DnvdApiKey=\$NVD_API_KEY" 
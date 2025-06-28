#!/bin/bash

# Setup Environment Variables for Telepesa Backend
# This script helps developers set up their local environment

set -e

echo "🔧 Setting up Telepesa Backend Environment..."

# Check if .env.example exists
if [ ! -f ".env.example" ]; then
    echo "❌ .env.example not found. Creating template..."
    exit 1
fi

# Check if .env.local already exists
if [ -f ".env.local" ]; then
    echo "⚠️  .env.local already exists. Do you want to overwrite it? (y/N)"
    read -r response
    if [[ ! "$response" =~ ^[Yy]$ ]]; then
        echo "ℹ️  Keeping existing .env.local file"
        echo "💡 Edit .env.local manually to add new variables"
        exit 0
    fi
fi

# Copy template to actual env file
cp .env.example .env.local
echo "✅ Created .env.local from template"

# Make sure .env.local is in .gitignore
if ! grep -q ".env.local" .gitignore 2>/dev/null; then
    echo ".env.local" >> .gitignore
    echo "✅ Added .env.local to .gitignore"
else
    echo "✅ .env.local already in .gitignore"
fi

# Check for other .env patterns in .gitignore
if ! grep -q "^\.env$" .gitignore 2>/dev/null; then
    echo ".env" >> .gitignore
    echo "✅ Added .env to .gitignore"
fi

echo ""
echo "🎉 Environment setup complete!"
echo ""
echo "📋 Next steps:"
echo "1. Edit .env.local with your actual API keys and values"
echo "2. Get your NVD API key: https://nvd.nist.gov/developers/request-an-api-key"
echo "3. Update JWT_SECRET with a strong, unique value"
echo "4. Configure database credentials if using PostgreSQL"
echo ""
echo "⚠️  IMPORTANT:"
echo "- NEVER commit .env.local to version control"
echo "- Keep your API keys secure and private"
echo "- Use different keys for different environments"
echo ""
echo "📝 To load environment variables:"
echo "   source .env.local"
echo "   # or"
echo "   export \$(cat .env.local | xargs)"

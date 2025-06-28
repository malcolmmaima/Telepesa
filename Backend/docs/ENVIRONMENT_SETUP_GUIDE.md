# Environment Setup Guide for Telepesa

## 🔧 Local Development Environment Configuration

### Quick Start
```bash
# 1. Setup environment files
cd Backend
./scripts/setup-env.sh

# 2. Edit with your actual values
nano .env.local

# 3. Start development
cd transaction-service
./scripts/setup-local-dev.sh
```

## 📁 Environment File Structure

### Files Created
```
Backend/
├── .env.example          # ✅ Template (safe to commit)
├── .env.local           # ❌ Your actual values (NEVER commit)
├── scripts/
│   └── setup-env.sh     # ✅ Setup script
└── transaction-service/
    └── scripts/
        ├── load-env.sh       # ✅ Load environment variables
        ├── setup-local-dev.sh # ✅ Complete setup with env loading
        └── fix-nvd-issues.sh  # ✅ Fix OWASP parsing issues
```

### Security Features
- ✅ **API keys in .env files** (not hardcoded)
- ✅ **All .env files in .gitignore**
- ✅ **Template file (.env.example)** for team reference
- ✅ **Automated environment loading** in scripts
- ✅ **Security validation** and warnings

## 🔑 API Keys Management

### Required API Keys
1. **NVD API Key** - For faster OWASP security scans
   - Get at: https://nvd.nist.gov/developers/request-an-api-key
   - Used in: `NVD_API_KEY` environment variable

2. **JWT Secret** - For local development authentication
   - Generate: Strong, unique secret for local development
   - Used in: `JWT_SECRET` environment variable

### Environment Variables
```bash
# Security & API Keys
NVD_API_KEY=your-nvd-api-key-here
JWT_SECRET=your-super-secret-jwt-key
JWT_EXPIRATION=86400000

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=telepesa
DB_USERNAME=telepesa
DB_PASSWORD=your-db-password

# Spring Boot Configuration
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8081

# Development Settings
DEBUG_ENABLED=true
LOGGING_LEVEL_TELEPESA=DEBUG
```

## 🛠️ Usage Instructions

### Initial Setup
```bash
# 1. Navigate to Backend directory
cd Backend

# 2. Run the setup script
./scripts/setup-env.sh

# 3. Edit the environment file
nano .env.local
# Add your actual API keys and values

# 4. Verify the setup
source .env.local
echo "NVD API Key: ${NVD_API_KEY:0:8}..."
```

### Transaction Service Development
```bash
# Navigate to transaction service
cd Backend/transaction-service

# Run complete setup (includes environment loading)
./scripts/setup-local-dev.sh

# Or load environment manually
source scripts/load-env.sh

# Run with environment variables
mvn spring-boot:run -Dspring.profiles.active=local
```

### Fix OWASP Issues
```bash
# If you encounter OWASP parsing errors
cd Backend/transaction-service
./scripts/fix-nvd-issues.sh
```

## 🔒 Security Best Practices

### DO's ✅
- ✅ Use `.env.local` for actual values
- ✅ Keep `.env.example` updated with new variables
- ✅ Add all `.env*` files to `.gitignore`
- ✅ Use strong, unique secrets for each environment
- ✅ Get your own API keys (don't share)

### DON'Ts ❌
- ❌ Never commit `.env.local` or `.env` files
- ❌ Never hardcode API keys in source code
- ❌ Never share API keys in documentation
- ❌ Never use production keys in development

## 🐛 Troubleshooting

### Environment File Not Loading
```bash
# Check if file exists
ls -la Backend/.env.local

# Check file format (no spaces around =)
cat Backend/.env.local

# Manually source the file
source Backend/.env.local
```

### API Key Issues
```bash
# Verify NVD API key is set
echo "NVD API Key: ${NVD_API_KEY:0:8}..."

# Test API key
cd Backend/transaction-service
./scripts/test-nvd-api.sh
```

### OWASP Parsing Errors
```bash
# Run the fix script
cd Backend/transaction-service
./scripts/fix-nvd-issues.sh

# Manual fix
rm -rf ~/.m2/repository/org/owasp/dependency-check-data
mvn dependency-check:check -DfailOnError=false
```

## 📚 Related Documentation

- [Environment Security Rules](../Rules/environment-security-rules.md)
- [NVD API Setup Guide](NVD_API_SETUP.md)
- [OWASP Parsing Fix](OWASP_PARSING_FIX.md)
- [Transaction Service README](../transaction-service/README.md)

---

**Remember**: Your environment files contain sensitive information. Keep them secure and never commit them to version control! 🔐

# NVD API Key Setup Guide

## 🔒 Why You Need an NVD API Key

The OWASP Dependency Check plugin uses the National Vulnerability Database (NVD) to identify security vulnerabilities in your project dependencies. Without an API key, the plugin must download the entire vulnerability database, which can take 30+ minutes and often fails due to rate limiting.

**With an NVD API Key:**
- ⚡ **10x faster** security scans (2-3 minutes vs 30+ minutes)  
- 🔄 **Reliable downloads** without rate limiting
- 📊 **Complete vulnerability data** access
- 🚀 **Optimized CI/CD pipelines**

## 🆓 Getting Your Free NVD API Key

### Step 1: Request Your API Key
1. **Visit**: https://nvd.nist.gov/developers/request-an-api-key
2. **Fill out the form** with:
   - Your email address
   - Organization (can be personal)
   - Intended use (e.g., "Open source security scanning")
3. **Submit** the request

### Step 2: Check Your Email
- You'll receive your API key via email **within minutes**
- The email will contain your unique API key string
- **Save this key securely** - you'll need it for all projects

### Step 3: Configure Your Environment

#### Permanent Setup (Recommended)
Add to your shell profile (`~/.bashrc`, `~/.zshrc`, or `~/.profile`):
```bash
# Add this line to your shell profile
export NVD_API_KEY="your-api-key-here"

# Reload your profile
source ~/.zshrc  # or ~/.bashrc
```

## 🛠️ Using Your API Key

### For Any Telepesa Service
```bash
# Set your API key
export NVD_API_KEY="your-api-key-here"

# Run security scan (now 10x faster!)
mvn dependency-check:check -DnvdApiKey=$NVD_API_KEY
```

## 🔐 Security Best Practices

### ✅ DO:
- **Store in environment variables** or secure secret management
- **Add to CI/CD secrets** for automated pipelines  
- **Keep your API key private** and secure

### ❌ DON'T:
- **Never commit API keys** to version control
- **Don't share API keys** in documentation or code
- **Don't hardcode keys** in scripts or configuration files

## 📊 Performance Comparison

**Without API Key:** 30+ minutes, frequent failures  
**With API Key:** 2-3 minutes, reliable ✅

---

**Remember**: Your NVD API key is personal and should be kept secure. Never share it publicly! 🔐

## 🐛 Troubleshooting Common Issues

### CVSS v4.0 Parsing Errors
If you encounter errors like "Cannot construct instance of CvssV4Data$ModifiedCiaType", this is due to parsing issues with new CVSS v4.0 data format.

**Quick Fix:**
```bash
cd Backend/transaction-service
./scripts/fix-nvd-issues.sh
```

**What the fix does:**
- Updates to latest OWASP plugin version (11.1.0)
- Clears corrupted NVD cache data
- Adds better error handling configuration
- Disables problematic analyzers

### Manual Cache Clearing
```bash
# Clear NVD cache
rm -rf ~/.m2/repository/org/owasp/dependency-check-data

# Clear OWASP plugin cache
rm -rf ~/.m2/repository/org/owasp

# Run with safer configuration
mvn dependency-check:check -DfailOnError=false
```

### Plugin Version Issues
Always use the latest OWASP Dependency Check plugin version:
```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>11.1.0</version>
    <!-- Latest version with CVSS v4.0 support -->
</plugin>
```

### API Rate Limiting
If you encounter rate limiting even with an API key:
- Increase delay: `-DnvdApiDelay=4000`
- Reduce results per page: `-DnvdApiResultsPerPage=1000`
- Increase retry count: `-DnvdMaxRetryCount=5`


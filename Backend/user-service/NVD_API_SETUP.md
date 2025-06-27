# NVD API Key Setup Guide

This guide helps you set up the National Vulnerability Database (NVD) API key for enhanced security scanning in the Telepesa Banking Application.

## 🔑 Why You Need an NVD API Key

The NVD API key provides:
- **Faster scans**: Direct access to vulnerability data
- **More reliable builds**: Avoids rate limiting issues
- **Latest data**: Access to the most recent vulnerability information
- **Banking compliance**: Ensures comprehensive security scanning for financial applications

## 📝 Getting Your API Key

1. **Visit the NVD website**: https://nvd.nist.gov/developers/request-an-api-key
2. **Request an API key** (it's completely free)
3. **Wait for email confirmation** (usually takes a few minutes to hours)
4. **Copy your API key** from the email

## 🛠️ Setup Instructions

### Option 1: Automated Setup (Recommended)

```bash
# Navigate to the user-service directory
cd Backend/user-service

# Run the setup script with your API key
./setup-nvd-key.sh YOUR_NVD_API_KEY_HERE
```

### Option 2: Manual Setup

#### For Local Development:

```bash
# Add to your shell configuration
echo 'export NVD_API_KEY=your-api-key-here' >> ~/.zshrc  # for zsh
# OR
echo 'export NVD_API_KEY=your-api-key-here' >> ~/.bashrc # for bash

# Apply changes
source ~/.zshrc  # or ~/.bashrc
```

#### For GitHub Actions (CI/CD):

1. **Go to your GitHub repository**
2. **Navigate to**: Settings → Secrets and variables → Actions
3. **Click "New repository secret"**
4. **Name**: `NVD_API_KEY`
5. **Value**: Your actual API key
6. **Click "Add secret"**

## 🧪 Testing Your Setup

### Test Locally:
```bash
# Check if the key is set
echo $NVD_API_KEY

# Run a test scan
mvn org.owasp:dependency-check-maven:check
```

### Expected Output:
```
[INFO] Using NVD API key for enhanced vulnerability data
[INFO] Checking for updates
[INFO] updating nvd cve data
```

## 🚀 Usage

### With API Key (Enhanced Mode):
- **Faster scans**: Direct API access
- **Real-time data**: Latest vulnerability information  
- **Higher rate limits**: No throttling issues
- **Better reliability**: Consistent CI/CD builds

### Without API Key (Fallback Mode):
- **Local data only**: Uses cached vulnerability database
- **Rate limited**: May encounter 403 errors in CI/CD
- **Older data**: May miss recent vulnerabilities

## ⚙️ Configuration Details

The OWASP Dependency Check plugin will automatically:

1. **Detect API key**: Checks for `NVD_API_KEY` environment variable
2. **Enable enhanced mode**: Uses API if key is available
3. **Fallback gracefully**: Uses local data if no key is found
4. **Optimize settings**: Adjusts retry counts and delays based on API availability

### Key Configuration Parameters:

| Parameter | With API Key | Without API Key |
|-----------|--------------|-----------------|
| `autoUpdate` | `true` | `false` |
| `cveValidForHours` | `4` | `24` |
| `nvdMaxRetryCount` | `3` | `3` |
| `nvdDelay` | `2000ms` | `1000ms` |

## 🔐 Security Considerations

- **Keep your API key secure**: Never commit it to version control
- **Use environment variables**: Store in `.zshrc`, `.bashrc`, or CI secrets
- **Rotate periodically**: Consider getting a new key every 6-12 months
- **Monitor usage**: Check for any unusual API usage patterns

## 🐛 Troubleshooting

### Common Issues:

#### 1. "No plugin found for prefix 'spring-boot'"
```bash
# Make sure you're in the correct directory
cd Backend/user-service
mvn spring-boot:run
```

#### 2. "NVD Returned Status Code: 403"
- Your API key might be invalid or expired
- Check rate limits (free tier: 50 requests per 30 seconds)
- Verify the key is correctly set: `echo $NVD_API_KEY`

#### 3. "Fatal exception(s) analyzing"
- This is often normal - check if the scan actually completed
- Look for "BUILD SUCCESS" in the logs
- Review the generated report in `target/dependency-check-report.html`

### Debug Commands:

```bash
# Check environment variable
echo "NVD_API_KEY is set: ${NVD_API_KEY:+YES}"

# Run with debug output
mvn org.owasp:dependency-check-maven:check -X

# Test API key validity
curl -H "apiKey: $NVD_API_KEY" "https://services.nvd.nist.gov/rest/json/cves/2.0?resultsPerPage=1"
```

## 📊 Expected Improvements

With the API key configured, you should see:

- ✅ **Build time**: 2-3x faster security scans
- ✅ **Reliability**: 99%+ successful CI/CD builds  
- ✅ **Data freshness**: Real-time vulnerability detection
- ✅ **Compliance**: Enhanced security for banking applications

## 📞 Support

If you encounter issues:

1. **Check the logs**: Look for specific error messages
2. **Verify API key**: Test with the debug commands above  
3. **Review rate limits**: Ensure you're not exceeding API quotas
4. **Check NVD status**: Visit https://nvd.nist.gov/ for service status

---

**Happy secure coding! 🔐** 
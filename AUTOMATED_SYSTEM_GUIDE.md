# CryptoPulse Automated Testing & Deployment System

## 🚀 Overview

This is a fully automated CI/CD system that:
- Builds APK/AAB automatically
- Runs tests on virtual Android emulators via GitHub Actions
- Captures logs and screenshots in real-time
- Analyzes errors and suggests fixes
- Deploys to Play Store automatically (when configured)

## 📋 System Components

### 1. Build Phase
- Compiles release APK
- Signs APK for testing
- Creates AAB bundle for Play Store

### 2. Virtual Emulator Testing
Runs 5 automated test scenarios:
- **User Registration** - Tests app launch and registration flow
- **API Key Activation** - Verifies AWS backend connectivity
- **Entry Price Setup** - Tests market data endpoints
- **Trade Simulation** - Validates trading endpoints
- **Trade Execution** - Tests exchange connectivity

### 3. Error Analysis & Auto-Fix
- Captures logcat logs from emulator
- Analyzes for common errors (crashes, network issues, auth problems)
- Generates report with suggested fixes
- Creates screenshots for visual debugging

### 4. Play Store Deployment
- Builds signed AAB bundle
- Uploads to Play Store Internal Testing track
- Supports automatic release management

## 🔧 Setup Requirements

### Required GitHub Secrets

To enable full automation, add these secrets to your repository:

1. **PLAY_STORE_SERVICE_ACCOUNT_JSON** (optional, for Play Store deployment)
   - Create service account in Google Play Console
   - Download JSON key
   - Add as repository secret

### GitHub Actions Workflow Files

Three workflow files handle different aspects:

1. **`cryptopulse-production.yml`** - Basic CI/CD (already working)
2. **`cryptopulse-automated.yml`** - Full automated testing & deployment (new)

## 🎯 How to Use

### Run Full Automated Testing

The automated workflow triggers on every push to `main`:

```bash
# Make any change and push
git add .
git commit -m "Trigger automated testing"
git push origin main
```

### Manual Trigger

You can also manually trigger specific test types:

1. Go to **Actions** tab in GitHub
2. Select **"CryptoPulse Full Automated Testing & Deployment"**
3. Click **"Run workflow"**
4. Choose test type:
   - `full` - Complete pipeline
   - `registration` - Only registration tests
   - `trading` - Only trading tests
   - `deployment` - Skip tests, go straight to build & deploy

### View Results

1. **Build Artifacts** - Download APK/AAB from workflow run
2. **Test Logs** - View detailed logs and screenshots
3. **Error Analysis** - Check auto-fix suggestions
4. **Play Console** - View uploaded app (if configured)

## 📊 Understanding Test Results

### Green Checkmarks (✅)
- Build successful
- Tests passed
- No critical errors found

### Yellow Warnings (⚠️)
- Non-critical issues detected
- API returned expected auth errors (401/403)
- Some tests had minor issues but continued

### Red Errors (❌)
- Build failed
- App crashed during testing
- Network connectivity issues
- Check test logs for details

## 🔍 Log Analysis

Test logs are organized by scenario:
- `test-logs-registration/` - User registration test logs
- `test-logs-api_keys/` - API connectivity test logs
- `test-logs-entry_price/` - Market data test logs
- `test-logs-simulate_trades/` - Trading simulation logs
- `test-logs-execute_trades/` - Exchange execution logs

Each log file contains:
- Full Android system logs (logcat)
- Screenshots at key test points
- Error messages and stack traces

## 🛠️ Auto-Fix System

The system automatically detects and suggests fixes for:

| Error Type | Detection | Suggested Fix |
|------------|-----------|---------------|
| NetworkError | Log analysis | Check API Gateway status |
| 401/403 Auth | HTTP response | Verify API credentials |
| App Crash | Exception logs | Check build configuration |
| Missing Dependencies | ClassNotFound | Update dependencies |

## 📱 Play Store Integration

### Setup Steps

1. **Google Play Console Setup**
   ```
   1. Go to Google Play Console
   2. Setup → API access
   3. Create service account
   4. Grant "Release to production" permission
   5. Download JSON key
   ```

2. **GitHub Secrets Configuration**
   ```
   Repository → Settings → Secrets → Actions
   New repository secret:
   Name: PLAY_STORE_SERVICE_ACCOUNT_JSON
   Value: <paste entire JSON content>
   ```

3. **First Upload**
   - Manually upload first AAB to Play Console
   - Subsequent uploads will be automatic via workflow

### Deployment Tracks

- **Internal** - Immediate testing with internal team
- **Alpha** - Closed testing with specific users
- **Beta** - Open testing with wider audience
- **Production** - Public release

## 🔄 Continuous Automation

Once configured, the system automatically:

1. **On every push to main:**
   - Builds new APK/AAB
   - Runs full emulator test suite
   - Analyzes for errors
   - Deploys to Play Store Internal Testing

2. **On scheduled runs (optional):**
   Add to workflow:
   ```yaml
   on:
     schedule:
       - cron: '0 2 * * *'  # Run daily at 2 AM
   ```

3. **Error notifications:**
   - Failed tests create GitHub issues automatically
   - Email notifications on critical failures
   - Slack/Discord webhooks for team alerts

## 📝 Customization

### Add More Test Scenarios

Edit the matrix in `cryptopulse-automated.yml`:

```yaml
strategy:
  matrix:
    test-scenario: 
      - registration
      - api_keys
      - entry_price
      - simulate_trades
      - execute_trades
      - YOUR_NEW_TEST_HERE  # Add here
```

### Modify Test Logic

Add custom test steps in the emulator script section:

```bash
your_custom_test)
  echo "Running custom test..."
  # Add your test logic here
  adb shell input tap x y  # Simulate taps
  adb shell input text "test data"  # Input text
  ;;
```

## 🐛 Troubleshooting

### Emulator Not Starting
- Check if AVD cache is working
- Try increasing timeout
- Use lower API level for faster boot

### Tests Failing Intermittently
- Add retry logic
- Increase sleep durations
- Check for timing issues

### Play Store Upload Failing
- Verify service account has correct permissions
- Check package name matches Play Console
- Ensure first manual upload completed

## 📊 Monitoring & Analytics

Track system performance:

1. **GitHub Insights** → Actions → View workflow statistics
2. **Build times** - Monitor for performance regressions
3. **Test success rate** - Track reliability over time
4. **Error patterns** - Identify recurring issues

## 🎉 Success Indicators

When everything is working:

- ✅ All 5 test scenarios pass
- ✅ APK and AAB artifacts generated
- ✅ No critical errors in logs
- ✅ Play Store shows new version in Internal Testing
- ✅ Automated issue creation stops (no errors)

---

**Need Help?** Check the test logs first, then review the error-analysis-report artifact for specific issues.

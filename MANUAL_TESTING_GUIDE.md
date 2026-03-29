# Manual Testing Guide - Interactive Browser Emulator

## 🎯 What You Get

This workflow allows you to **manually use and test your CryptoPulse Android app** directly from GitHub via a browser-based emulator - **no Android Studio needed!**

## 🚀 How to Use

### Option 1: Browser-Based Emulator (Recommended) ⭐

**Step 1: Trigger the Workflow**

Automatically runs on every push to `main`, or manually:
```bash
git commit --allow-empty -m "Trigger manual testing"
git push origin main
```

Or manually:
1. Go to **Actions** → **"Manual Testing - Interactive Browser Emulator"**
2. Click **"Run workflow"**
3. Wait ~5 minutes for build

**Step 2: Click the Link**

After the workflow completes, you'll see output like:
```
═══════════════════════════════════════════════════
  🎮 INTERACTIVE EMULATOR READY
═══════════════════════════════════════════════════

  Click this link to use the app in your browser:
  https://appetize.io/app/xxxxx?device=pixel4...

  You can:
  • Click, tap, swipe like a real phone
  • Type with your keyboard
  • Test all features manually
  • Session lasts ~10 minutes
═══════════════════════════════════════════════════
```

**Step 3: Test the App**

The link opens a **fully functional Android emulator** in your browser:
- 📱 **Pixel 4 device** running Android 12
- 🖱️ **Click/tap** to interact with buttons
- ⌨️ **Type** with your keyboard in text fields
- 👆 **Swipe** to scroll through screens
- ⏱️ **10-minute session** per link

### What You Can Test:

| Feature | How to Test |
|---------|-------------|
| **User Registration** | Click through registration flow, enter email, request OTP |
| **API Key Activation** | Navigate to settings, test API connectivity |
| **Set Entry Price** | Go to trading screen, set price values |
| **Simulate Trades** | Use paper trading features |
| **Execute Real Trades** | Test exchange connectivity |
| **Navigation** | Click all menu items, verify screens load |
| **UI/UX** | Check layouts, buttons, text readability |

---

## 📱 Option 2: Download APK (For Real Device)

If you prefer testing on your actual Android phone:

1. Go to the workflow run in GitHub Actions
2. Scroll to **Artifacts** section
3. Download `manual-testing-apk`
4. Transfer to your Android device
5. Install and use normally

**Advantages:**
- ✅ Native performance (faster than browser)
- ✅ All device features (camera, sensors, etc.)
- ✅ No time limits
- ✅ More realistic testing

---

## 🔧 Option 3: VNC-Based Emulator (Advanced)

For developers who want direct emulator control:

1. Go to **Actions** → **"Manual Testing - Interactive Browser Emulator"**
2. Click **"Run workflow"** (dropdown appears)
3. Select **vnc-emulator** job only
4. Provides VNC access to raw Android emulator

**Note:** Requires VNC client (like RealVNC, TightVNC) and additional setup for public access.

---

## 🎮 Testing Checklist

Use this checklist while testing in the browser emulator:

### Registration & Authentication
- [ ] App launches without crashing
- [ ] Registration screen loads
- [ ] Can enter email address
- [ ] OTP request button works
- [ ] Error messages display correctly

### API Integration
- [ ] API key input screen accessible
- [ ] Connectivity to AWS backend works
- [ ] No network timeout errors

### Trading Features
- [ ] Entry price can be set
- [ ] Trade simulation runs
- [ ] Portfolio screen loads
- [ ] Market data displays

### UI/UX
- [ ] All buttons are clickable
- [ ] Text is readable
- [ ] Navigation works smoothly
- [ ] No visual glitches

### Performance
- [ ] App responds quickly to taps
- [ ] No long loading delays
- [ ] Screens transition smoothly

---

## ⚠️ Important Notes

### Appetize.io Limitations (Free Tier)
- **100 minutes/month** of usage
- **10-minute session** per app launch
- **Pixel 4 emulator** (can't change device)
- **Android 12** only

### What to Do If Link Doesn't Work:
1. **Re-run the workflow** - Sessions expire after 10 minutes
2. **Check workflow logs** - May show if upload failed
3. **Create Appetize account** - For more than 100 min/month
4. **Use APK download** - Install on your own device instead

### For Heavy Testing:
If you need more than 100 minutes/month:
- Create free account at [Appetize.io](https://appetize.io)
- Get API key
- Add `APPETIZE_API_KEY` secret to GitHub
- Workflow will use your account

---

## 🐛 Reporting Issues

Found a bug while testing? 

1. **Take screenshot** in the browser emulator
2. **Note the steps** you took before the bug
3. **Check workflow logs** for error details
4. **Create GitHub issue** with:
   - Screenshot
   - Steps to reproduce
   - Expected vs actual behavior
   - Workflow run link

---

## 🔄 Quick Re-Test

Need to test again after making code changes?

```bash
# Make your changes
git add .
git commit -m "Fix: [describe your fix]"
git push origin main

# New emulator link will be generated automatically!
```

---

## 📊 Comparison of Options

| Feature | Browser Emulator | APK Download | VNC Emulator |
|---------|-----------------|--------------|--------------|
| **Setup** | None needed | USB + Android device | VNC client needed |
| **Speed** | Good | Best (native) | Slow |
| **Interaction** | Mouse/keyboard | Touch gestures | Full control |
| **Time Limit** | 10 min/session | Unlimited | 20 min (configurable) |
| **Cost** | Free (100 min/mo) | Free | Free |
| **Best For** | Quick tests | Deep testing | Development |

---

## 🎯 Recommended Workflow

1. **Quick Check** → Use browser emulator (5-10 min test)
2. **Deep Testing** → Download APK, test on real device (30+ min)
3. **Bug Fix Verification** → Browser emulator (quick re-test)
4. **Final Validation** → Real device before release

---

**Ready to test?** Push any change to trigger the workflow and get your interactive emulator link!

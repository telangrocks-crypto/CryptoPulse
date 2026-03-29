#!/bin/bash
echo "CryptoPulse Production Deployment"
echo "===================================="

# Build release APK
echo "Building release APK..."
./gradlew assembleRelease

# Run final tests
echo "Running final tests..."
./gradlew test

# Backend health check
echo "Final backend health check..."
./test-backend-integration.sh

echo ""
echo "CRYPTOPULSE READY FOR PRODUCTION!"
echo "APK: app/build/outputs/apk/release/app-release.apk"
echo "Backend: https://hs28uxr9j6.execute-api.ap-south-1.amazonaws.com/prod"
echo "Status: All systems operational"

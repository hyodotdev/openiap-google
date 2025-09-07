#!/bin/bash

# OpenIAP GMS - Open in Android Studio
# This script opens the project in Android Studio

echo "🚀 Opening OpenIAP GMS in Android Studio..."

# Check if Android Studio is installed
ANDROID_STUDIO_PATH="/Applications/Android Studio.app"
if [ ! -d "$ANDROID_STUDIO_PATH" ]; then
    echo "❌ Android Studio not found at: $ANDROID_STUDIO_PATH"
    echo "Please install Android Studio or update the path in this script"
    exit 1
fi

# Open the project in Android Studio
open -a "Android Studio" .

echo "✅ Project opened in Android Studio"
echo ""
echo "📖 Quick Start Guide:"
echo "  1. Wait for Gradle sync to complete"
echo "  2. Select 'sample' configuration from the run dropdown"
echo "  3. Connect an Android device or start an emulator"
echo "  4. Click the Run button (▶️) to launch the example app"
echo ""
echo "🔧 Available Gradle Tasks:"
echo "  • Build Library: ./gradlew :openiap:build"
echo "  • Build Sample: ./gradlew :sample:assembleDebug"
echo "  • Install Sample: ./gradlew :sample:installDebug"
echo "  • Run Tests: ./gradlew :openiap:test"
echo ""
echo "📱 Test with Google Play Console:"
echo "  1. Upload a signed APK to internal testing"
echo "  2. Configure in-app products"
echo "  3. Add test accounts"
echo "  4. Install signed APK on test device"
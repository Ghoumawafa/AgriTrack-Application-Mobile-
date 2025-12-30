# AgriTrack Testing Guide

## Overview
This document provides comprehensive testing instructions for the AgriTrack mobile application.

## Prerequisites
- Android Studio installed
- Android device or emulator (API 24+)
- Firebase project configured
- ESP32 device (optional, for IoT testing)

## Unit Testing

### Running Unit Tests
```bash
./gradlew test
```

### Key Test Areas
1. **Database Operations** (Room)
   - Irrigation CRUD operations
   - Data persistence
   - Query performance

2. **Data Validation**
   - Input validation in forms
   - Email format validation
   - Password strength validation

3. **Utilities**
   - StorageHelper operations
   - Date conversions
   - Firebase data mapping

## Integration Testing

### Running Integration Tests
```bash
./gradlew connectedAndroidTest
```

### Test Scenarios

#### 1. User Authentication Flow
- [ ] Launch app → Splash screen appears
- [ ] Navigate to Login screen
- [ ] Enter invalid credentials → Error message displayed
- [ ] Enter valid credentials → Navigate to Home screen
- [ ] Logout → Return to Login screen

#### 2. Irrigation Management
- [ ] Navigate to Irrigation module
- [ ] Add new irrigation zone
- [ ] Configure hardware (pump pin, sensor pin)
- [ ] Toggle manual/auto mode
- [ ] Verify Firebase sync
- [ ] Delete irrigation zone

#### 3. Firebase Integration
- [ ] Create irrigation zone → Verify Firebase node created
- [ ] Update mode → Verify Firebase update
- [ ] ESP32 changes data → Verify app reflects changes
- [ ] Network disconnection → App continues with local data
- [ ] Network reconnection → Data syncs automatically

## Manual Testing Checklist

### UI/UX Testing
- [ ] All buttons are clickable and responsive
- [ ] Loading indicators appear during async operations
- [ ] Error messages are clear and actionable
- [ ] Navigation flows smoothly between screens
- [ ] Back button behavior is correct
- [ ] App handles orientation changes gracefully

### Performance Testing
- [ ] App launches within 3 seconds
- [ ] List scrolling is smooth (60 FPS)
- [ ] No memory leaks during extended use
- [ ] Database queries complete quickly (<100ms)
- [ ] Firebase operations have proper timeouts

### Security Testing
- [ ] Passwords are not visible in logs
- [ ] SharedPreferences data is secure
- [ ] Firebase rules prevent unauthorized access
- [ ] ProGuard obfuscates release builds

## ESP32 Integration Testing

### Setup
1. Configure ESP32 with WiFi credentials
2. Update Firebase URL in ESP32 code
3. Upload code to ESP32
4. Connect sensors and pump

### Test Cases
- [ ] ESP32 connects to WiFi successfully
- [ ] ESP32 reads Firebase data every 5 seconds
- [ ] Manual mode: App controls pump via Firebase
- [ ] Auto mode: ESP32 controls pump based on sensor
- [ ] Network failure: ESP32 handles gracefully
- [ ] Firebase update: ESP32 reflects changes within 5s

## Regression Testing

After each major change, verify:
- [ ] Login/Signup still works
- [ ] All navigation links functional
- [ ] Database operations successful
- [ ] Firebase sync operational
- [ ] No new crashes or ANRs

## Bug Reporting

When reporting bugs, include:
1. Device model and Android version
2. Steps to reproduce
3. Expected vs actual behavior
4. Screenshots/logs if applicable
5. Firebase/ESP32 logs (for IoT issues)

## Continuous Integration

### GitHub Actions (Recommended)
```yaml
# .github/workflows/android.yml
name: Android CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build with Gradle
        run: ./gradlew build
      - name: Run tests
        run: ./gradlew test
```

## Performance Benchmarks

### Target Metrics
- App launch time: < 3 seconds
- Screen transition: < 300ms
- Database query: < 100ms
- Firebase sync: < 2 seconds
- Memory usage: < 150MB

## Known Issues
1. Room database requires migration when schema changes
2. Firebase listeners must be detached in onDestroy()
3. ESP32 requires stable WiFi connection
4. ProGuard may break reflection-based code

## Next Steps
1. Implement automated UI tests with Espresso
2. Add Firebase Test Lab integration
3. Set up crash reporting (Firebase Crashlytics)
4. Implement analytics tracking


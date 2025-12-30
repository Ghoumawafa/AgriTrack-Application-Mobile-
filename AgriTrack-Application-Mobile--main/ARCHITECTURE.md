# AgriTrack Architecture Documentation

## System Overview

AgriTrack is a mobile IoT application for smart irrigation management, consisting of three main components:

1. **Android Mobile App** - User interface and local data management
2. **Firebase Realtime Database** - Cloud synchronization and real-time updates
3. **ESP32 IoT Device** - Hardware control and sensor monitoring

## Architecture Diagram

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│   Android App   │◄───────►│    Firebase      │◄───────►│   ESP32 Device  │
│   (Mobile UI)   │         │  Realtime DB     │         │  (IoT Hardware) │
└─────────────────┘         └──────────────────┘         └─────────────────┘
        │                            │                            │
        │                            │                            │
    Room DB                    /irrigation/zones           Sensors & Pumps
   (Local Cache)              (Cloud Sync)                (Physical Control)
```

## Mobile App Architecture

### Layer Structure

```
┌──────────────────────────────────────────┐
│           Presentation Layer             │
│  (Activities, Adapters, UI Components)   │
└──────────────────────────────────────────┘
                    │
┌──────────────────────────────────────────┐
│            Business Logic                │
│   (Managers, Helpers, Utilities)         │
└──────────────────────────────────────────┘
                    │
┌──────────────────────────────────────────┐
│            Data Layer                    │
│  (Room Database, Firebase, Models)       │
└──────────────────────────────────────────┘
```

### Key Components

#### 1. Activities
- **SplashActivity**: App entry point, handles navigation to Login/Home
- **LoginActivity**: User authentication with validation
- **AccueilActivity**: Main dashboard with module navigation
- **IrrigationActivity**: IoT irrigation management interface
- **ProfileActivity**: User profile and settings

#### 2. Data Models
- **Irrigation**: Entity with Room annotations and Firebase sync fields
  - Local fields: id, terrainName, irrigationDate, waterQuantity
  - IoT fields: remoteKey, mode, manualState, sensorValue, pumpPin, sensorPin

#### 3. Database (Room)
- **AppDatabase**: Singleton database instance
- **IrrigationDao**: Data access object with CRUD operations
- **DateConverter**: Type converter for Date objects
- **Indexes**: Optimized queries on terrainName, remoteKey, irrigationDate

#### 4. Firebase Integration
- **Path Structure**: `/irrigation/zones/{zoneKey}`
- **Real-time Sync**: ChildEventListener for bidirectional updates
- **Offline Support**: Room database serves as local cache

## Data Flow

### 1. User Creates Irrigation Zone (App → Firebase → ESP32)

```
User Input → IrrigationActivity → Room DB (local save)
                                      ↓
                              Firebase.push() → /irrigation/zones/{key}
                                      ↓
                              ESP32 polls Firebase → Reads zone config
                                      ↓
                              ESP32 configures hardware pins
```

### 2. ESP32 Updates Sensor Data (ESP32 → Firebase → App)

```
ESP32 reads sensor → Firebase.update() → /irrigation/zones/{key}/sensorValue
                                              ↓
                                    ChildEventListener triggered
                                              ↓
                                    syncZoneFromSnapshot()
                                              ↓
                                    Room DB updated → UI refreshed
```

### 3. User Toggles Manual Mode (App → Firebase → ESP32)

```
User toggles switch → writeModeToRemote()
                            ↓
                    Firebase.setValue("mode", "manual")
                            ↓
                    ESP32 polls → Reads new mode
                            ↓
                    ESP32 switches control logic
```

## Firebase Database Structure

```json
{
  "irrigation": {
    "zones": {
      "{zoneKey}": {
        "mode": "auto|manual|scheduled",
        "manualState": true|false,
        "sensorValue": 1234.5,
        "threshold": 1500,
        "hardware": {
          "pumpMotor": {
            "pin": 26,
            "enabled": true
          },
          "soilSensor": {
            "pin": 34,
            "enabled": true
          }
        }
      }
    }
  }
}
```

## ESP32 Control Logic

### Polling Cycle (Every 5 seconds)
1. Check WiFi connection
2. HTTP GET `/irrigation/zones.json`
3. Parse JSON response
4. For each zone:
   - Validate pin configuration
   - Apply mode logic:
     - **Manual**: Use `manualState` (ON/OFF)
     - **Auto**: Read sensor, compare to threshold
     - **Scheduled**: (Future feature)
5. Update GPIO pins accordingly

### Error Handling
- WiFi disconnection → Retry with exponential backoff
- HTTP failures → Log and continue with last known state
- Invalid pin → Ignore and log warning
- JSON parse error → Skip update cycle

## Security Considerations

### Mobile App
- ProGuard obfuscation for release builds
- No hardcoded credentials
- Secure SharedPreferences for user data
- Input validation on all forms

### Firebase
- Authentication required for read/write
- Validation rules for data structure
- Rate limiting to prevent abuse
- Indexes for query performance

### ESP32
- WiFi credentials in separate config file
- HTTPS for Firebase communication (production)
- Pin validation to prevent hardware damage
- Watchdog timer for crash recovery

## Performance Optimizations

### Mobile App
1. **Database Indexes**: Fast queries on frequently accessed fields
2. **Background Threads**: All DB operations off main thread
3. **Firebase Listeners**: Properly attached/detached to prevent leaks
4. **RecyclerView**: ViewHolder pattern for smooth scrolling

### ESP32
1. **Polling Interval**: 5 seconds balances responsiveness and power
2. **JSON Buffer**: 8KB static allocation for predictable memory
3. **Pin Validation**: Prevents invalid GPIO operations
4. **Connection Pooling**: Reuse HTTP connections

## Scalability

### Current Limitations
- Single user per app instance
- Max 10 zones per ESP32 (configurable)
- 5-second update latency

### Future Enhancements
- Multi-user support with Firebase Auth
- Multiple ESP32 devices per user
- WebSocket for sub-second updates
- Cloud Functions for complex automation
- Machine learning for predictive irrigation

## Deployment

### Mobile App
1. Build release APK: `./gradlew assembleRelease`
2. Sign with keystore
3. Upload to Google Play Console
4. Enable ProGuard for code protection

### ESP32
1. Configure WiFi credentials
2. Update Firebase URL
3. Upload via Arduino IDE or PlatformIO
4. Monitor serial output for debugging

### Firebase
1. Deploy security rules: `firebase deploy --only database`
2. Configure indexes for performance
3. Set up monitoring and alerts
4. Enable backup and recovery

## Monitoring & Debugging

### Mobile App
- Android Studio Logcat for runtime logs
- Firebase Crashlytics for crash reports
- Android Profiler for performance analysis

### ESP32
- Serial monitor for real-time logs
- Firebase console for data inspection
- Multimeter for hardware debugging

### Firebase
- Firebase Console for data visualization
- Usage metrics and quotas
- Security rules simulator


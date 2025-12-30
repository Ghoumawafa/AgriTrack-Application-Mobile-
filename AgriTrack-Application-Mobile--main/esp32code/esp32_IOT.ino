/*
  ESP32 IoT example for AgriTrack Realtime DB (per-zone)
  - Polls /irrigation/zones.json
  - Expects object where each child is a zone:
    { "<zoneKey>": { "mode":"auto"/"manual", "manualState":true, "sensorValue":..., "hardware": { "pumpMotor": { "pin":26, "enabled":true }, "soilSensor": { "pin":34, "enabled":true } }, "threshold":1500 }, ... }
  - Validates pins and applies per-zone actions.
  NOTE: Use secure auth + WiFiClientSecure + proper Firebase library for production.
*/

#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

const char* ssid = "YOUR_SSID";
const char* password = "YOUR_PASSWORD";

// Realtime DB endpoints (no auth in this example). Ensure trailing .json for REST API.
const char* DB_BASE = "https://agritrack-48076-default-rtdb.firebaseio.com";
const char* ZONES_ENDPOINT = "/irrigation/zones.json";

unsigned long lastCheck = 0;
const unsigned long CHECK_INTERVAL_MS = 5000; // 5s
unsigned long lastWiFiCheck = 0;
const unsigned long WIFI_CHECK_INTERVAL_MS = 30000; // Check WiFi every 30s
int consecutiveFailures = 0;
const int MAX_CONSECUTIVE_FAILURES = 5;

// allowed pins set (simple validation)
const int allowedPins[] = {25, 26, 27, 32, 33, 34}; // 34 is input-only
bool pinAllowed(int pin) {
  for (int i = 0; i < (int)(sizeof(allowedPins)/sizeof(allowedPins[0])); ++i) if (allowedPins[i] == pin) return true;
  return false;
}

// track configured pins to avoid reconfiguring repeatedly
struct ZoneConfig {
  String key;
  int pumpPin;
  bool pumpEnabled;
  int sensorPin;
  bool sensorEnabled;
  String mode;
  bool manualState;
  int threshold;
  unsigned long lastAction;
};
#define MAX_ZONES 8
ZoneConfig zones[MAX_ZONES];
int zoneCount = 0;

void safeConfigureZonePins(ZoneConfig &z) {
  if (z.sensorEnabled && pinAllowed(z.sensorPin)) {
    pinMode(z.sensorPin, INPUT);
  }
  if (z.pumpEnabled && pinAllowed(z.pumpPin)) {
    if (z.pumpPin >= 34 && z.pumpPin <= 39) {
      // input-only: do not set as output
      Serial.printf("Pump pin %d is input-only; disabling pump\n", z.pumpPin);
      z.pumpEnabled = false;
    } else {
      pinMode(z.pumpPin, OUTPUT);
      // ensure default off
      digitalWrite(z.pumpPin, LOW);
    }
  } else {
    if (pinAllowed(z.pumpPin) && !(z.pumpPin >= 34 && z.pumpPin <= 39)) {
      digitalWrite(z.pumpPin, LOW);
      pinMode(z.pumpPin, OUTPUT);
    }
  }
}

// WiFi reconnection helper
void ensureWiFiConnected() {
  if (WiFi.status() == WL_CONNECTED) {
    consecutiveFailures = 0;
    return;
  }

  Serial.println("WiFi disconnected, attempting reconnection...");
  WiFi.disconnect();
  delay(100);
  WiFi.begin(ssid, password);

  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - start < 10000) {
    delay(200);
    Serial.print(".");
  }
  Serial.println();

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("WiFi reconnected successfully");
    consecutiveFailures = 0;
  } else {
    Serial.println("WiFi reconnection failed");
    consecutiveFailures++;
  }
}

String httpGet(const String& url) {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi not connected, skipping HTTP request");
    return "";
  }

  HTTPClient http;
  http.setTimeout(5000); // 5 second timeout
  http.begin(url);
  int code = http.GET();
  String payload = "";

  if (code == HTTP_CODE_OK) {
    payload = http.getString();
    consecutiveFailures = 0; // Reset on success
  } else {
    Serial.printf("HTTP GET failed, code=%d url=%s\n", code, url.c_str());
    consecutiveFailures++;

    // If too many failures, try WiFi reconnection
    if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
      Serial.println("Too many consecutive failures, forcing WiFi reconnect");
      ensureWiFiConnected();
    }
  }

  http.end();
  return payload;
}

void applyZoneState(ZoneConfig &z, JsonObject zoneObj) {
  // update configuration
  if (zoneObj.containsKey("mode")) z.mode = String((const char*)zoneObj["mode"]);
  if (zoneObj.containsKey("manualState")) z.manualState = zoneObj["manualState"].as<bool>();
  if (zoneObj.containsKey("threshold")) z.threshold = zoneObj["threshold"].as<int>();

  if (zoneObj.containsKey("sensorValue")) {
    // not used to decide, but could be stored in DB
  }

  if (zoneObj.containsKey("hardware")) {
    JsonObject hw = zoneObj["hardware"].as<JsonObject>();
    if (hw.containsKey("pumpMotor")) {
      JsonObject pm = hw["pumpMotor"].as<JsonObject>();
      if (pm.containsKey("pin")) {
        int pin = pm["pin"].as<int>();
        if (pinAllowed(pin)) z.pumpPin = pin;
      }
      if (pm.containsKey("enabled")) z.pumpEnabled = pm["enabled"].as<bool>();
    }
    if (hw.containsKey("soilSensor")) {
      JsonObject ss = hw["soilSensor"].as<JsonObject>();
      if (ss.containsKey("pin")) {
        int pin = ss["pin"].as<int>();
        if (pinAllowed(pin) || (pin >= 34 && pin <= 39)) z.sensorPin = pin;
      }
      if (ss.containsKey("enabled")) z.sensorEnabled = ss["enabled"].as<bool>();
    }
  }

  safeConfigureZonePins(z);

  // Decision
  if (z.mode == "manual") {
    if (z.manualState && z.pumpEnabled && pinAllowed(z.pumpPin) && !(z.pumpPin >= 34 && z.pumpPin <= 39)) {
      digitalWrite(z.pumpPin, HIGH);
      z.lastAction = millis();
    } else {
      if (pinAllowed(z.pumpPin) && !(z.pumpPin >= 34 && z.pumpPin <= 39)) digitalWrite(z.pumpPin, LOW);
    }
  } else { // auto
    if (z.sensorEnabled && pinAllowed(z.sensorPin)) {
      int sensor = analogRead(z.sensorPin);
      if (sensor < z.threshold) {
        if (z.pumpEnabled && pinAllowed(z.pumpPin) && !(z.pumpPin >= 34 && z.pumpPin <= 39)) {
          digitalWrite(z.pumpPin, HIGH);
        }
      } else {
        if (pinAllowed(z.pumpPin) && !(z.pumpPin >= 34 && z.pumpPin <= 39)) digitalWrite(z.pumpPin, LOW);
      }
    } else {
      // cannot decide; keep pump off
      if (pinAllowed(z.pumpPin) && !(z.pumpPin >= 34 && z.pumpPin <= 39)) digitalWrite(z.pumpPin, LOW);
    }
  }
}

void parseZonesPayload(const String &payload) {
  StaticJsonDocument<8192> doc;
  DeserializationError err = deserializeJson(doc, payload);
  if (err) {
    Serial.println("Failed to parse zones JSON");
    return;
  }
  JsonObject root = doc.as<JsonObject>();
  zoneCount = 0;
  // iterate keys
  for (JsonPair kv : root) {
    if (zoneCount >= MAX_ZONES) break;
    String key = kv.key().c_str();
    JsonObject zoneObj = kv.value().as<JsonObject>();
    ZoneConfig z;
    z.key = key;
    z.pumpPin = 26;
    z.pumpEnabled = false;
    z.sensorPin = 34;
    z.sensorEnabled = false;
    z.mode = "auto";
    z.manualState = false;
    z.threshold = 1500;
    z.lastAction = 0;
    // apply values from payload
    applyZoneState(z, zoneObj);
    zones[zoneCount++] = z;
  }
}

void setup() {
  Serial.begin(115200);
  delay(100);

  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - start < 20000) {
    delay(200);
    Serial.print(".");
  }
  Serial.println();

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("WiFi connected");
  } else {
    Serial.println("WiFi not connected - continuing offline");
  }
}

void loop() {
  // Periodic WiFi check
  if (millis() - lastWiFiCheck > WIFI_CHECK_INTERVAL_MS) {
    lastWiFiCheck = millis();
    ensureWiFiConnected();
  }

  if (millis() - lastCheck < CHECK_INTERVAL_MS) {
    delay(50);
    return;
  }
  lastCheck = millis();

  // Only attempt Firebase operations if WiFi is connected
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("Skipping Firebase check - WiFi not connected");
    delay(1000);
    return;
  }

  String url = String(DB_BASE) + String(ZONES_ENDPOINT);
  String payload = httpGet(url);

  if (payload.length() == 0) {
    Serial.println("Empty payload received from Firebase");
    return;
  }

  parseZonesPayload(payload);

  // apply per-zone logic
  for (int i = 0; i < zoneCount; ++i) {
    applyZoneState(zones[i], JsonObject()); // second arg unused; applyZoneState already populated
  }
}

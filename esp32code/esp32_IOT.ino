#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>
#include <ArduinoJson.h>

// ---------------- USER CONFIG ----------------
static const char* WIFI_SSID = "jqsser";
static const char* WIFI_PASS = "00001111";

// Firebase
static const char* DB_BASE = "https://agritrack-48076-default-rtdb.firebaseio.com";
static const char* FIREBASE_AUTH = "MDIMJUvH7ZZsputknLwJdCvcgsbRJbf0BTGnEQqq"; // keep if you use DB secret/token
static const char* ZONE1_ENDPOINT = "/irrigation/zone1.json";

// Pins (as requested lists)
static const int ACTUATOR_PINS[] = {17, 18, 19, 21, 22, 23, 25};
static const int SENSOR_PINS[]   = {12, 13, 14, 15, 16};

// We use ONE actuator + ONE tactile button
static const int ACTUATOR_PIN = ACTUATOR_PINS[0]; // GPIO17
static const int BUTTON_PIN   = 27;              // dedicated tactile button (not in SENSOR_PINS)

// Polling
static const unsigned long POLL_MS = 1500;

// Button debounce
static const unsigned long DEBOUNCE_MS = 50;

// ---------------- State ----------------
struct Zone1State {
  bool enabled = true;
  bool ledOn = false;
};

static Zone1State zone1;
static unsigned long lastPollAt = 0;

static int btnRawLast = HIGH;
static int btnStable = HIGH;
static unsigned long btnLastChangeAt = 0;

// ---------------- Helpers ----------------
static void ensureWiFi() {
  if (WiFi.status() == WL_CONNECTED) return;

  Serial.printf("WiFi connect to %s\n", WIFI_SSID);
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASS);

  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - start < 15000) {
    delay(250);
    Serial.print(".");
  }
  Serial.println();

  if (WiFi.status() == WL_CONNECTED) {
    Serial.print("WiFi OK IP=");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("WiFi FAIL");
  }
}

static String withAuth(const String& url) {
  if (strlen(FIREBASE_AUTH) == 0) return url;
  String u = url;
  u += (url.indexOf('?') >= 0) ? "&auth=" : "?auth=";
  u += FIREBASE_AUTH;
  return u;
}

static String httpGet(const String& url) {
  if (WiFi.status() != WL_CONNECTED) return "";

  WiFiClientSecure client;
  client.setInsecure();
  HTTPClient http;
  http.setTimeout(8000);

  const String full = withAuth(url);
  http.begin(client, full);

  Serial.printf("GET %s\n", full.c_str());
  const int code = http.GET();
  String payload = (code > 0) ? http.getString() : "";

  Serial.printf("GET %s (%d), len=%d\n", (code == 200 ? "OK" : "ERR"), code, payload.length());
  http.end();
  return payload;
}

static bool httpPatchJson(const String& url, const String& jsonBody) {
  if (WiFi.status() != WL_CONNECTED) return false;

  WiFiClientSecure client;
  client.setInsecure();
  HTTPClient http;
  http.setTimeout(8000);

  const String full = withAuth(url);
  http.begin(client, full);
  http.addHeader("Content-Type", "application/json");

  const int code = http.sendRequest("PATCH", (uint8_t*)jsonBody.c_str(), jsonBody.length());
  Serial.printf("PATCH %s code=%d body=%s\n", full.c_str(), code, jsonBody.c_str());

  http.end();
  return code >= 200 && code < 300;
}

static void applyActuator() {
  const bool on = zone1.enabled && zone1.ledOn;
  digitalWrite(ACTUATOR_PIN, on ? HIGH : LOW);
  Serial.printf("Zone1 apply: enabled=%d ledOn=%d -> ACT=%s\n", (int)zone1.enabled, (int)zone1.ledOn, on ? "ON" : "OFF");
}

static bool parseZone1(const String& payload, Zone1State& out) {
  StaticJsonDocument<512> doc;
  auto err = deserializeJson(doc, payload);
  if (err) {
    Serial.print("JSON parse error: ");
    Serial.println(err.f_str());
    return false;
  }
  if (!doc.is<JsonObject>()) return false;

  JsonObject o = doc.as<JsonObject>();

  // Null-safe: only update if present
  if (o.containsKey("enabled") && !o["enabled"].isNull()) out.enabled = o["enabled"].as<bool>();
  if (o.containsKey("ledOn") && !o["ledOn"].isNull()) out.ledOn = o["ledOn"].as<bool>();

  return true;
}

static void pollFirebase() {
  const String url = String(DB_BASE) + String(ZONE1_ENDPOINT);
  const String payload = httpGet(url);

  if (payload.length() > 0 && payload.length() <= 160) {
    Serial.printf("payload=%s\n", payload.c_str());
  }

  if (payload.length() == 0) return;

  Zone1State incoming = zone1;
  if (parseZone1(payload, incoming)) {
    if (incoming.enabled != zone1.enabled || incoming.ledOn != zone1.ledOn) {
      Serial.println("Remote update detected -> applying");
    }
    zone1 = incoming;
    applyActuator();
  }
}

static void onLocalToggle() {
  zone1.ledOn = !zone1.ledOn;
  applyActuator();

  // write back so app updates
  StaticJsonDocument<128> patch;
  patch["enabled"] = zone1.enabled;
  patch["ledOn"] = zone1.ledOn;

  String body;
  serializeJson(patch, body);

  httpPatchJson(String(DB_BASE) + String(ZONE1_ENDPOINT), body);
}

static void handleButton() {
  const int raw = digitalRead(BUTTON_PIN);

  if (raw != btnRawLast) {
    btnRawLast = raw;
    btnLastChangeAt = millis();
  }
  if (millis() - btnLastChangeAt < DEBOUNCE_MS) return;

  if (raw != btnStable) {
    btnStable = raw;
    if (btnStable == LOW) { // press
      Serial.println("Button press");
      onLocalToggle();
    }
  }
}

void setup() {
  Serial.begin(115200);
  delay(200);

  pinMode(ACTUATOR_PIN, OUTPUT);
  digitalWrite(ACTUATOR_PIN, LOW);

  pinMode(BUTTON_PIN, INPUT_PULLUP);

  Serial.printf("ACTUATOR_PIN=%d BUTTON_PIN=%d\n", ACTUATOR_PIN, BUTTON_PIN);

  ensureWiFi();
  pollFirebase(); // initial sync
}

void loop() {
  ensureWiFi();
  handleButton();

  if (millis() - lastPollAt >= POLL_MS) {
    lastPollAt = millis();
    pollFirebase();
  }

  delay(10);
}

<div align="center">

# Upwely

**Indoor navigation and warehouse operations, in your pocket.**

A modern Android app for warehouse pickers, packers, and managers — built with Jetpack Compose, BLE beacon positioning, and a direct line into Acumatica ERP.

[![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Min SDK](https://img.shields.io/badge/minSdk-24-blue)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/targetSdk-36-blue)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)

</div>

---

## What it does

Upwely turns a phone into a warehouse co-pilot. It guides workers to the right shelf using BLE beacons, scans barcodes through the camera, and syncs everything back to Acumatica in real time.

| | |
| :--- | :--- |
| **Indoor navigation** | Real-time positioning using AltBeacon-compatible BLE beacons, with a calibration flow for tuning RSSI on a per-site basis. |
| **Warehouse planning** | Visual editor for laying out racks, aisles, and beacon anchors — the same map powers picking and placing. |
| **Picking & placing** | Step-by-step routing through pick lists, with map overlays showing the next position. |
| **Shipping** | Browse shipments, drill into line items, choose box sizes, and confirm with a scan. |
| **Receiving** | Process purchase receipts straight from the receiving dock with live ERP lookups. |
| **Acumatica sync** | Talks to Acumatica's REST endpoint with stored credentials and HTTP cleartext support for on-prem deployments. |
| **Localized** | Ships with English, Romanian, Polish, French, Spanish, German, and Ukrainian. |

## Tech stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Navigation Compose** for screen graph
- **CameraX** + **ML Kit Barcode Scanning** for live scanning
- **AltBeacon** for BLE beacon ranging
- **OkHttp** + Kotlinx Coroutines for ERP I/O
- **Lifecycle ViewModel** for state

## Project layout

```
app/src/main/java/com/warehouse/upwely
├── MainActivity.kt              # Single-activity host
├── navigation/                  # Compose navigation graph
├── data/
│   ├── AcumaticaApi.kt          # REST client for Acumatica
│   ├── BeaconPositioning.kt     # BLE ranging + trilateration
│   ├── SettingsRepository.kt    # Persisted app settings
│   ├── LocaleManager.kt         # Per-app language switching
│   ├── OrderData.kt
│   ├── ShipmentData.kt
│   ├── PurchaseReceiptData.kt
│   └── WarehousePlan.kt         # Editable layout model
└── ui/
    ├── viewmodels/              # State holders
    └── screens/                 # ~20 Compose screens (Map, Picking, Shipments, …)
```

## Getting started

### Prerequisites

- Android Studio Ladybug or newer
- Android device or emulator running **API 24+**
- A reachable Acumatica instance for live data (optional — the app boots without one)
- BLE-capable device for beacon positioning

### Build & run

```bash
git clone <this-repo>
cd Upwely
./gradlew assembleDebug
./gradlew installDebug   # with a device attached
```

Open `Upwely` in Android Studio and hit **Run** for the usual iteration loop.

### Configure Acumatica

On first launch, open **Settings** and fill in:

- Server base URL
- Tenant / company
- Username & password
- Endpoint name and version

Credentials are persisted via `SettingsRepository` and reused across screens.

## Permissions

| Permission | Why |
| :--- | :--- |
| `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` | Range BLE beacons for positioning |
| `ACCESS_FINE_LOCATION` | Required by Android for BLE scanning |
| `CAMERA` | Live barcode scanning during picking and shipping |
| `INTERNET` | Talk to Acumatica |

## Beacon calibration

Place beacons at known positions, open **Calibration**, walk to each anchor, and capture the RSSI. The values are written into the active warehouse plan and used for distance estimation at runtime.

## Localization

Translations live under `app/src/main/res/values-<lang>/strings.xml`. To add a language, copy `values/strings.xml` to a new locale folder and translate. The app uses Android 13+ per-app language selection and includes a manual picker for older devices.

## Roadmap

- [ ] Offline-first cache for pick lists and shipments
- [ ] Multi-floor map support
- [ ] Cycle-count workflow
- [ ] Pluggable ERP backends beyond Acumatica

## License

Proprietary — all rights reserved.

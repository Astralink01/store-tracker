# Store Tracker — Build Instructions

## How to open and build in Android Studio

### Step 1 — Install Android Studio
Download from: https://developer.android.com/studio  
(Free, available for Windows, Mac, Linux)

### Step 2 — Open the project
1. Launch Android Studio
2. Click **"Open"** (or File → Open)
3. Navigate to and select the **`store-tracker-android/`** folder (this folder)
4. Click **OK** — Gradle will sync automatically (takes ~1-2 min on first run, needs internet)

### Step 3 — Add launcher PNG icons (optional but recommended)
The project uses adaptive vector icons (API 26+) which work on most modern phones.
For older devices (API 24-25), add PNG icons:
- `res/mipmap-mdpi/ic_launcher.png`     (48×48)
- `res/mipmap-hdpi/ic_launcher.png`     (72×72)
- `res/mipmap-xhdpi/ic_launcher.png`    (96×96)
- `res/mipmap-xxhdpi/ic_launcher.png`   (144×144)
- `res/mipmap-xxxhdpi/ic_launcher.png`  (192×192)

Or: right-click `res` → New → Image Asset → choose "Launcher Icons" and upload your S logo.

### Step 4 — Run on a device or emulator
- **Emulator**: Click the green ▶ button. Android Studio will create a virtual device.
- **Real phone**: Enable "Developer options" → "USB debugging" on your phone, plug in via USB, select it from the device dropdown, then press ▶.

### Step 5 — Build the APK
**Debug APK** (for testing, signed with a debug key):
> **Build → Build Bundle(s) / APK(s) → Build APK(s)**  
> Output: `app/build/outputs/apk/debug/app-debug.apk`

**Release APK** (for distribution, requires your own signing key):
> **Build → Generate Signed Bundle / APK → APK → Next**  
> Create or select a keystore → fill alias, passwords → choose "release" → Finish  
> Output: `app/build/outputs/apk/release/app-release.apk`

---

## Project structure

```
store-tracker-android/
├── app/
│   ├── build.gradle                  ← Dependencies (Gson, Glide, Material)
│   └── src/main/
│       ├── AndroidManifest.xml       ← Permissions + activity declarations
│       ├── java/com/storetracker/
│       │   ├── models/
│       │   │   ├── Product.java      ← Product data object (serialized to JSON)
│       │   │   ├── OrderItem.java    ← Line item (snapshot of product at sale time)
│       │   │   └── Order.java        ← Completed transaction
│       │   ├── utils/
│       │   │   └── StorageManager.java  ← ALL file I/O (products.json, orders.json)
│       │   ├── adapters/
│       │   │   ├── ProductAdapter.java   ← Inventory list with quick-adjust buttons
│       │   │   ├── CartAdapter.java      ← Active cart list
│       │   │   ├── OrderHistoryAdapter.java
│       │   │   └── OrderItemAdapter.java
│       │   └── activities/
│       │       ├── MainActivity.java         ← Home screen (4 navigation buttons)
│       │       ├── InventoryActivity.java    ← Full product list
│       │       ├── AddEditProductActivity.java ← Add/edit product + photo capture
│       │       ├── POSActivity.java          ← Point of Sale (product selection + cart)
│       │       ├── CheckoutActivity.java     ← Payment, change calc, "Done Order"
│       │       ├── OrderHistoryActivity.java ← Past transactions
│       │       └── CalculatorActivity.java  ← Built-in calculator
│       └── res/
│           ├── layout/               ← All XML UI layouts
│           ├── values/               ← Colors, strings, themes
│           ├── drawable/             ← Vector icons + adaptive icon layers
│           └── xml/file_paths.xml    ← FileProvider config (camera image URIs)
```

## How data is stored (no database!)

| File | Location | Contents |
|------|----------|----------|
| `products.json` | `getFilesDir()/products.json` | JSON array of all Product objects |
| `orders.json` | `getFilesDir()/orders.json` | JSON array of all Order objects |
| Product images | `getFilesDir()/product_images/` | JPEG files named `product_<uuid>.jpg` |

**Gson** converts Java objects → JSON strings on save, and JSON strings → Java objects on load.  
`StorageManager.saveOrder()` automatically deducts sold quantities from `products.json` in the same call — this is the stock-reflection link between POS and inventory.

## Key app flows

```
MainActivity
├── Inventory → InventoryActivity
│   └── [FAB] → AddEditProductActivity (add)
│   └── [Edit] → AddEditProductActivity (edit)
├── New Sale → POSActivity
│   └── [Checkout →] → CheckoutActivity
│       └── [Done Order] → saves Order + deducts stock → back to POSActivity
├── Order History → OrderHistoryActivity
│   └── [Tap order] → dialog with full detail
└── Calculator → CalculatorActivity
```

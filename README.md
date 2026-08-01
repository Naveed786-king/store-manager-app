# Store Manager (Offline Android App)

Offline-first Android store management system built with Kotlin, Jetpack Compose, Material 3, and Room (SQLite).

## Features
Dashboard, Products (barcode/SKU/image), Customers, Suppliers, Sales & PDF invoices, Returns, Purchases,
Inventory & stock history, Expenses, Reports, Backup/Restore, CSV import/export, PIN lock, dark mode.

## How the APK is built
Every push to `main` triggers `.github/workflows/build.yml`, which builds a debug APK on GitHub's
cloud runners (no local Android Studio needed). Download it from the **Actions** tab of this repo →
latest run → **Artifacts** → `store-manager-debug-apk`.

# SmartBiz — Modern Sales, Inventory, Cash Flow & Liquidity Management

**SmartBiz** is a native Android financial and enterprise management application designed for small and medium-sized businesses. Built with Kotlin, Jetpack Compose, Material 3, and Room Database, it is an offline-first system for tracking sales, inventory valuation, purchases, customer receivables, supplier payables, cash registers, bank accounts, expenses, and true liquidity.

---

## 1. Architecture & Design Principles

- **Architecture:** Clean MVVM (Model-View-ViewModel / Repository pattern) separating UI, business logic, and local database transactions.
- **UI Framework:** 100% Jetpack Compose with Material Design 3 guidelines (Navy `#0F172A`, Emerald Green `#10B981`, White, and Slate neutrals).
- **Offline-First Room Storage:** Local SQLite database with ACID transactions for multi-record operations (sales deducting stock, creating ledger entries, and updating account balances).
- **Financial Precision:** All monetary amounts are stored as exact minor currency units (`Long` cents), preventing floating-point rounding errors.
- **True Liquidity Accounting:** Distinguishes available unrestricted liquidity (active cash registers and bank accounts) from restricted escrow funds, customer receivables, and inventory assets.
- **Traceable Inventory Movements:** Every stock change creates a signed `StockMovement` entry in the movement ledger.

---

## 2. Key Modules & Features

### Financial Dashboard
- **Real-Time KPIs:** Today's sales, gross profit, net operating profit, available liquidity, cash registers vs bank balances, inventory value at purchase cost, working capital estimate, customer receivables, and supplier payables.
- **Interactive Visualizations:** 7-day sales and gross profit comparison bar charts; 30-day projected cash flow trajectory curve.
- **Low Stock & Overdue Alerts:** Direct notifications on items below safety threshold.

### Sales & Invoices (POS)
- Complete checkout with product search (by name, SKU, or barcode).
- Real-time stock validation (enforces or permits negative stock based on settings).
- Automatic tax calculation, line discounts, and payment methods (Cash, Card, Bank Transfer, Split, and Credit).
- Instant PDF/text invoice generation with native Android print/share sheet.
- Sales return and refund workflow with optional inventory restock.

### Inventory & Warehousing
- Product catalog with SKU, barcode, unit of measure (pcs, kg, box, liter, meter), cost price, and selling price.
- Multi-location storage tracking (Central Logistics Hub, Retail Branches, Safe Storage).
- Stock movement ledger recording all transactions (Sales, Purchases, Returns, Audits, Breakage).
- CameraX barcode and SKU scanner with live reticle and manual search fallback.

### Finance, Accounts & Liquidity
- Multi-account management: Cash drawers, bank accounts, digital wallets, and restricted reserves.
- Inter-account balance transfers that preserve profit neutrality.
- Expense tracking by operational category (Rent, Utilities, Salaries, Logistics, Taxes).
- Daily cash drawer closing and reconciliation against expected system totals.
- Customer debt collections and supplier settlements.

### Business Intelligence & Reports
- Income statement (Revenue, COGS, Gross Profit, Operating Expenses, Net Operating Profit).
- Inventory asset valuation report calculated via weighted average cost.
- Instant share/export of executive reports.

### Smart AI Business Advisor
- Natural language query answering ("How much profit did I make?", "What is my cash runway?", "Which items should I reorder?", "Who owes me money?").
- Automated cash shortage forecasting and safety stock recommendations.
- Runs 100% locally and offline without external API requirements.

### Settings & Localization
- Language selection: **English**, **Français (French)**, and **العربية (Arabic with RTL layout)**.
- Customizable business profile, tax ID, currency symbols, and invoice notes.
- Optional 4-digit security PIN lock.
- Preloaded sample business dataset for instant testing.

---

## 3. Building and Running the App

### Prerequisites
- Android Studio Ladybug / Meerkat or later
- JDK 17 or JDK 21
- Android SDK 34+

### Debug APK Build
```bash
gradle assembleDebug
```
The generated APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Running Unit Tests
```bash
gradle :app:testDebugUnitTest
```

### Release APK Build
To generate a production release APK:
```bash
gradle assembleRelease
```

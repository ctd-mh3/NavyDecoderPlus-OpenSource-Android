# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Navy Decoder Plus is an Android application (Java, minSdk 26, compileSdk/targetSdk 37) that allows U.S. Navy personnel to look up administrative codes (ratings, designators, billets, etc.). The app bundles a pre-built SQLite database of code definitions and provides an in-app search screen backed by a `ContentProvider` (not the Android Search framework).

## Build Commands

```bash
# Build debug APK
./gradlew :navyDecoderPlus:assembleDebug

# Build release APK
./gradlew :navyDecoderPlus:assembleRelease

# Run lint
./gradlew :navyDecoderPlus:lint

# Run all checks (lint + tests)
./gradlew :navyDecoderPlus:check
```

```bash
# Auto-format Java and XML sources with Spotless
./gradlew :navyDecoderPlus:spotlessApply

# Check formatting without modifying files
./gradlew :navyDecoderPlus:spotlessCheck
```

**Important:** The CLI `./gradlew` requires JDK 17+. The machine's system JDK may be older; in that case builds must be run from Android Studio, which bundles its own JDK. There are no unit tests in the project.

Spotless uses Google Java Format (currently `1.35.0`) for `.java` files and enforces trailing-whitespace cleanup + 4-space indent for `.xml` files. The `tasks.withType(JavaCompile).configureEach` block in `navyDecoderPlus/build.gradle` enables `-Xlint:unchecked` and `-Xlint:deprecation` compiler warnings on every build.

## Database Update Workflow

When Navy source data changes, the embedded SQLite database must be rebuilt and copied into the app assets:

1. Edit the relevant SQL files in `database/`
2. `cd database/` then run `./createAndCopyDatabase.sh` (requires `sqlite3` CLI) — **must be run from within the `database/` directory**; this regenerates `navyDecoderDatabase.sqlite3` and copies it to `navyDecoderPlus/src/main/assets/`
3. Increment `DB_VERSION` in `navyDecoderPlus/src/main/java/com/crashtestdummylimited/navydecoderplus/model/db/DecodeDatabase.java`

The RFAS code tables are **not** populated by `createAndCopyDatabase.sh` — those tables use `fill_table_rfas_codes_dummy.sql` as a placeholder because actual RFAS data comes from restricted non-CUI RESFOR files (only accessible to SELRES personnel). See `fill_table_rfas_*.sql` files for the format.

## Architecture

### Single Gradle Module

The project has one app module: `navyDecoderPlus`. The top-level `build.gradle` is a minimal shell; all real build configuration is in `navyDecoderPlus/build.gradle`. Build uses Java 11 source/target compatibility, ViewBinding is enabled, and release builds apply R8 minification + resource shrinking via ProGuard.

### Package Structure (`com.crashtestdummylimited.navydecoderplus`)

- **`NavyDecoderPlus`** — Main launcher Activity. Displays the list of decode categories in a `RecyclerView` (via `MainCategoryAdapter`), with a "Search All" entry at position 0 followed by each `Category` in ordinal order. Routes to the appropriate Activity on tap: `SearchableDecoderActivity` for a normal category or the global search, `RfasActivity` for the two RFAS categories.
- **`controller/`** — Activities and supporting controllers:
  - `SearchableDecoderActivity` — Single Activity (not one per category) that handles both single-category and cross-category ("Search All") lookups. Receives the category key — or `SearchableDecoderActivity.ALL_CATEGORIES_KEY` for global search — via the `MappingHelper.CATEGORY_KEY_IDENTIFIER` Intent extra. Uses a plain M3-outlined `EditText` + `TextWatcher` (not `onSearchRequested()`/`ACTION_SEARCH`) to query `DecodeProvider` via `ContentResolver` on every keystroke (and on the IME search action), snapshots the `Cursor` into a `List<SearchResultItem>` and closes it immediately, then displays results in a `RecyclerView` via `SearchResultsAdapter` (a `ListAdapter` with `DiffUtil`). Tapping a result launches `SelectedItemActivity`.
  - `MainCategoryAdapter`, `SearchResultsAdapter`, `SearchResultItem` — `RecyclerView` adapter/row classes backing the main category list and the search results list respectively.
  - `SelectedItemActivity` — Displays the full decoded detail for a single item. Also owns Play Store In-App Review logic (3-day install age gate, 7-day cooldown between prompts).
  - `RfasActivity` — Special-cased activity for RFAS codes (enlisted and officer), which use three `MaterialAutoCompleteTextView` exposed dropdowns (first / second-and-third / fourth character) instead of the standard search flow.
  - `DecodeProvider` — Android `ContentProvider` that routes URI-based queries to `DecodeDatabase`. Supports per-category search/suggestions, a global cross-category search URI (`decodeData/all`), and direct item lookup. The `AUTHORITY` string contains mixed case and **must not be changed** — it is baked into `AndroidManifest.xml` and all saved intents on existing installed devices.
  - `Category` — Enum that is the **single source of truth** for all decode categories. Each constant encodes the display order (via ordinal), the internal DB/URI key, the string-resource label ID, and the SQLite FTS table name. RFAS entries have null `key`/`ftsTable`. `Category.fromKey(String)` provides reverse lookup. **Adding a new category requires a new constant here — no other Java files or manifest entries need changing**, since `SearchableDecoderActivity` is registered exactly once and dispatches by category key at runtime.
  - `MappingHelper` — Singleton holding a `Context` for string resolution. Delegates `getSelectionText(key)` and `getAllCategoryIdentifies()` to `Category`. Provides both a `getInstance(Context)` initialiser and a no-arg `getInstance()` for callers (like `DecodeProvider`) that lack a `Context`.
  - `MenuOptions` — Shared options-menu logic reused across Activities (About, Open Source notice, Rate App, Share, Email Author, Privacy Policy).
- **`model/`**:
  - `db/DecodeDatabase` — Manages the pre-packaged SQLite database. On first run it copies the `.sqlite3` asset file to the app's database directory. On upgrade, it deletes and re-copies the database (simple replacement strategy, no data migration). **Room DAOs are not used** — all 14 tables are FTS3 virtual tables (Room only supports FTS4/FTS5), and the ContentProvider requires `Cursor`-returning queries, which doesn't fit Room's model. **`copyDataBase()` runs on the main thread** (via `DecodeProvider.onCreate()`) and is intentionally left there: the copy only happens once (first install / upgrade), the DB is ~1–3 MB, and the fix (async init + `CountDownLatch` or loading screen) adds significant complexity for a risk that has never produced a user-visible ANR. If Play Console ever surfaces ANR reports tied to this, the correct fix is to initialize `DecodeDatabase` on a background thread from `NavyDecoderPlus.onCreate()` with a loading spinner, with `DecodeProvider.query()` blocking on a shared `Future` until init completes.
  - `RFASEnlistedCodes`, `RFASOfficerCodes`, `RFASReferenceData` — Model classes for the RFAS code multistep lookup.
- **`util/`** — `CommonUtilities` (app version-name lookup), `AppUpdateChecker` (wraps Play Core's flexible in-app-update flow with a 24-hour cooldown; wired into `NavyDecoderPlus.onResume()`).

### Search Flow

1. `NavyDecoderPlus` main screen → user taps a category row (or "Search All" at position 0)
2. For RFAS categories, launches `RfasActivity` with the RFAS type extra; otherwise launches `SearchableDecoderActivity` with the category key (or `ALL_CATEGORIES_KEY`) in the `MappingHelper.CATEGORY_KEY_IDENTIFIER` extra
3. `SearchableDecoderActivity` queries `DecodeProvider` via `ContentResolver` as the user types (or submits via IME), building a `Uri` under `DecodeProvider.CONTENT_URI` appended with either the category key or `"all"`
4. `DecodeProvider` resolves the category key to its FTS table via `Category`/`MappingHelper` (or, for `"all"`, unions every category's FTS table in one query via `DecodeDatabase.getAllDecodeMatches()`), then delegates to `DecodeDatabase.getDecodeMatches()` (FTS3 prefix search)
5. Results render in a `RecyclerView`; tapping one launches `SelectedItemActivity`, which re-queries `DecodeProvider` for that single row and displays code/meaning/source

### UI Conventions

- **Edge-to-edge insets**: All four main activities apply `ViewCompat.setOnApplyWindowInsetsListener` on `android.R.id.content` to handle system bar padding (required for API 36+ mandatory edge-to-edge).
- **Toolbar title**: Set explicitly via `if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.app_name)` in each activity — the application-level `android:label` uses a shorter icon label (`app_name_for_icon`) that would otherwise show as the toolbar title. The null check is defensive only; `AppTheme` extends `Theme.Material3.DayNight` which always provides an ActionBar.
- **Dialog styling**: All `MaterialAlertDialogBuilder` dialogs use `R.style.MenuDialogStyle`, which sets Material3 color roles (`colorSurfaceContainerHigh`, `colorOnSurface`, `colorOnSurfaceVariant`, `colorPrimary`) — not `android:background`, which does not paint the Material3 dialog card.
- **Colors**: Navy-themed. Primary blue is `#002855` (Pantone 289 / U.S. Navy official). Separate `values/color.xml` (light) and `values-night/color.xml` (dark) — note the singular filename (`color.xml`, not `colors.xml`); there is no `values-notnight/` directory.
- **Adaptive icon**: `mipmap-anydpi/ic_launcher.xml` and `ic_launcher_round.xml` reference three drawables — `drawable/ic_launcher_background.xml` (solid navy `#002855` fill), `drawable/ic_launcher_foreground.xml` (vector: white radar rings + white ND+ lettering + accent-blue echo blip), and `drawable/ic_launcher_monochrome.xml` (single-color vector for Android 13+ themed icons). One legacy raster `mipmap-mdpi/` webp pair remains but is unreachable since `minSdk` 26 guarantees the adaptive icon is always used.
- **RTL**: `android:supportsRtl="false"` is set in the manifest. The app is English-only for U.S. Navy personnel; `Start`/`End` layout attributes are used as best practice but layouts are not designed for RTL mirroring.
- **Font sizes**: Four breakpoints — `values/` (default phones), `values-sw480dp/`, `values-sw600dp/` (7" tablets), `values-sw720dp/` (10" tablets). Padding dimensions use `dp`; only text sizes use `sp`.

### Adding a New Code Category

1. Add SQL table creation to `database/create_navy_decoder_tables.sql` and a `fill_table_<name>.sql` file
2. Add the new fill script to `database/createAndCopyDatabase.sh`'s list of `run_sql` calls
3. Increment `DB_VERSION` in `DecodeDatabase.java`
4. Add a string resource for the category label in `res/values/strings.xml`
5. Add a new constant to `Category.java` in the correct ordinal position for display order, with the key, label resource, and FTS table name — **this is the only Java change required**; no manifest entry and no new Activity classes are needed, since `SearchableDecoderActivity` already dispatches by category key at runtime.

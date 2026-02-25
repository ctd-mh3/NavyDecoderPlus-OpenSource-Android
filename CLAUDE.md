# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Navy Decoder Plus is an Android application (Java, minSdk 26, compileSdk/targetSdk 36) that allows U.S. Navy personnel to look up administrative codes (ratings, designators, billets, etc.). The app bundles a pre-built SQLite database of code definitions and uses Android's Search framework for full-text search.

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

There are no unit tests in the project currently.

## Database Update Workflow

When Navy source data changes, the embedded SQLite database must be rebuilt and copied into the app assets:

1. Edit the relevant SQL files in `database/`
2. `cd database/` then run `./createAndCopyDatabase.sh` (requires `sqlite3` CLI) — **must be run from within the `database/` directory**; this regenerates `navyDecoderDatabase.sqlite3` and copies it to `navyDecoderPlus/src/main/assets/`
3. Increment `DB_VERSION` in `navyDecoderPlus/src/main/java/com/crashtestdummylimited/navydecoderplus/model/db/DecodeDatabase.java`

The RFAS code tables are **not** populated by `createAndCopyDatabase.sh` — those tables use `fill_table_rfas_codes_dummy.sql` as a placeholder because actual RFAS data comes from restricted non-CUI RESFOR files (only accessible to SELRES personnel). See `fill_table_rfas_*.sql` files for the format.

> Note: `createAndCopyDatabase.sh` contains a second `cp` targeting `../NavyDecoderPlus/` (capitalized) which is a stale path and will silently fail — only the first `cp` to `../navyDecoderPlus/` is correct.

## Architecture

### Single Gradle Module

The project has one app module: `navyDecoderPlus`. The top-level `build.gradle` is a minimal shell; all real build configuration is in `navyDecoderPlus/build.gradle`. Build uses Java 11 source/target compatibility, ViewBinding is enabled, and release builds apply R8 minification + resource shrinking via ProGuard.

### Package Structure (`com.crashtestdummylimited.navydecoderplus`)

- **`NavyDecoderPlus`** — Main launcher Activity. Displays the list of decode categories and routes to the appropriate Activity for each selection.
- **`controller/`** — Activities and supporting controllers:
  - `BlankActivity` + `BlankActivity<CodeType>` subclasses — Thin activities that exist solely to trigger Android's Search dialog for a specific code category. Each subclass is paired with a `SearchableDecoderActivity<CodeType>`.
  - `SearchableDecoderActivity` + subclasses — Handles `ACTION_SEARCH` intents; queries `DecodeProvider` and displays results in a `ListView`.
  - `SelectedItemActivity` — Displays the full decoded detail for a single item.
  - `RfasActivity` — Special-cased activity for RFAS codes (enlisted and officer), which use a multi-step spinner UI instead of the standard search flow.
  - `DecodeProvider` — Android `ContentProvider` that routes URI-based queries to `DecodeDatabase`. Supports search suggestions and direct item lookup.
  - `MappingHelper` — Singleton that maps human-readable category strings (from `strings.xml`) to internal database key identifiers (e.g., `"enlistedratingcodes"`).
  - `MenuOptions` — Shared options-menu logic reused across Activities.
  - `SearchResultsCursorAdapter` — `CursorAdapter` for the search results `ListView`.
- **`model/`**:
  - `db/DecodeDatabase` — Manages the pre-packaged SQLite database. On first run it copies the `.sqlite3` asset file to the app's database directory. On upgrade it deletes and re-copies the database (simple replacement strategy, no data migration). There is a noted TODO to migrate to Android Room DAOs.
  - `RFASEnlistedCodes`, `RFASOfficerCodes`, `RFASReferenceData` — Model classes for the RFAS code multi-step lookup.
  - `ReferenceData` — Generic model for a decoded code/meaning pair.
- **`ui/`** — `AppRater` (prompts users to rate the app).
- **`util/`** — `ChangelogBuilder`, `CommonUtilities`, `DataLoader` (loads raw resource files as strings).

### Search Flow

Each searchable code category follows the same pattern:
1. `NavyDecoderPlus` main screen → user taps a category → launches `BlankActivity<CodeType>` with `MappingHelper.CATEGORY_KEY_IDENTIFIER` extra
2. `BlankActivity<CodeType>` immediately calls `onSearchRequested()`, passing the category key in `APP_DATA`, then finishes when the search dialog is dismissed
3. Android routes the search query to `SearchableDecoderActivity<CodeType>` (configured in `AndroidManifest.xml` via `android.app.default_searchable` / `android.app.searchable` metadata)
4. `SearchableDecoderActivity` queries `DecodeProvider` via `ContentResolver`, displays results, and launches `SelectedItemActivity` on tap
5. `DecodeProvider` uses `MappingHelper` to resolve the category key to a database table name, then delegates to `DecodeDatabase.getDecodeMatches()` (FTS3 prefix search)

### Adding a New Code Category

1. Add SQL table creation to `database/create_navy_decoder_tables.sql` and a `fill_table_<name>.sql` file
2. Update `database/createAndCopyDatabase.sh` to include the new fill script
3. Increment `DB_VERSION` in `DecodeDatabase.java` and add the FTS table constant and entry to `SELECTION_TO_TABLE_MAP`
4. Add a string resource for the category in `res/values/strings.xml`
5. Add entries to both maps in `MappingHelper.buildMap()` and `buildReverseMap()`
6. Create `BlankActivity<NewType>` and `SearchableDecoderActivity<NewType>` (copy an existing pair)
7. Add a searchable XML resource in `res/xml/`
8. Register both activities and their metadata in `AndroidManifest.xml`
9. Add the category string to the `mDecodeOptions` array in `NavyDecoderPlus.java` and add the corresponding `Intent` branch in the `onItemClickListener`

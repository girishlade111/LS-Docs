# LS Docs

**Privacy-first, offline document viewer, reader, inspector, and lightweight editor for Android.**

LS Docs is a fully on-device document toolkit built with Kotlin and Jetpack Compose. It lets you open, read, inspect, annotate, compare, convert, and lightly edit documents — all without a network connection. The app declares **no internet permission**, stores everything locally in a Room database or app-private storage, and ships with an optional AES-256-GCM encrypted private vault.

```
Open any document  →  Read / Inspect / Annotate  →  Organize / Protect  →  All on-device
```

---

## Table of Contents

- [Highlights](#highlights)
- [Features](#features)
  - [Home Dashboard](#home-dashboard)
  - [File Browser & Storage Analytics](#file-browser--storage-analytics)
  - [Document Workspace (Viewer & Editor)](#document-workspace-viewer--editor)
  - [Document Tools](#document-tools)
  - [Library & Private Vault](#library--private-vault)
  - [Settings & Privacy](#settings--privacy)
- [Supported File Formats](#supported-file-formats)
- [Privacy & Security Model](#privacy--security-model)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Data Persistence (Room Schema)](#data-persistence-room-schema)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Open in Android Studio](#open-in-android-studio)
  - [Build from the command line](#build-from-the-command-line)
- [Environment Variables & Secrets](#environment-variables--secrets)
- [Signing & Release Builds](#signing--release-builds)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Highlights

| Area | Detail |
| --- | --- |
| **100% offline** | No `INTERNET` permission in the manifest — nothing ever leaves the device |
| **On-device inspection** | OCR, diff, hex, conversion, watermarking, duplicate scan all run locally |
| **Multi-format reader** | PDF, Markdown, JSON/YAML/XML, CSV/TSV, EPUB, images, source code, ZIP, plain text, binary |
| **Lightweight editor** | Tabbed workspace with undo/redo editing, Markdown split preview, structured tree inspector |
| **Private vault** | PIN + biometrics lock, optional AES-256-GCM document encryption, app-switcher privacy |
| **Persistence** | Room database tracks recent / pinned / favorite / private documents, bookmarks, annotations, OCR history, conversions, and rich metadata |
| **High-density UI** | Material 3 + Compose with glassmorphism surfaces and multiple reader & code themes |
| **Built for testing** | Robolectric unit tests + Roborazzi visual regression screenshots |

---

## Features

### Home Dashboard

The landing screen gives a high-density overview of your document workflow:

- **Search** across recently opened documents (title / path / type matching via Room query).
- **Recent documents**, pinned documents, and favorites with quick open.
- **Statistics boxes** (document counts, totals) computed from the scanned device library.
- **Quick actions** that deep-link into the other tabs (open workspace, jump to a specific tool such as OCR or Diff).
- Search result cards with type-aware icons and metadata.

### File Browser & Storage Analytics

- Recursive scan of `Documents`, `Downloads`, app-external, and app-internal directories (max depth 3, dot-files and caches excluded, deduplicated by canonical path).
- Type-aware file rows (PDF, Markdown, JSON, YAML, XML, CSV, code, EPUB, image, text, ZIP, binary) with size and modified time.
- Manual **rescan** with a toast summary of the number of documents found.
- **Storage Consumption Analytics** dashboard: per-extension and per-directory breakdown of the scanned library.
- Open any file directly into the workspace; delete records or remove physical files from the device.

### Document Workspace (Viewer & Editor)

A tabbed workspace (`openTabs` state) where each opened document becomes a tab:

| Mode | What it does |
| --- | --- |
| **Markdown viewer/editor** | Split `source | preview` layout (configurable default), live rendering |
| **Code text editor** | Syntax-aware editing with an inline **Undo/Redo stack** (`UndoRedoState`), word-wrap toggle, monospace/custom font, adjustable font size |
| **Structured tree inspector** | Collapsible tree rendering for JSON / YAML / XML documents |
| **CSV spreadsheet viewer** | Tabular layout parsed by `CsvParser` (commas, quotes, TSV support) |
| **Generic document viewer** | Plain text fallback with charset reading and 10 MB read cap + truncation notice |
| **PDF / EPUB / images** | Opened via their own render paths; position and page are remembered |

Workspace extras:

- Pinned tabs and multi-tab management (close individual tab, close all).
- Per-document **reading progress** (page + scroll) persisted to Room when reopening.
- **Document Details dialog**: name, path, size, MIME type, extension, word/character/line counts, timestamps, editable author & description metadata.
- **PDF Watermark & Sign dialog**: overlay one of several watermark styles (`Diagonal Center`, `Signature Stamp`, `Top Header Stamp`) — previewed live — and configure the watermark string.
- Edits are written back to the same URI (opened via `content://` or `file://`), with confirm-on-overwrite behavior configurable in settings.

### Document Tools

Five inspection utilities under **Tools**:

1. **On-Device OCR** — pick an image from the gallery; the local engine decodes the bitmap and extracts text on-device. Every run is saved to the **OCR history** table in Room with its language and timestamp. Default language configurable in settings.
2. **Side-by-Side Diff** — pick two files, `DiffEngine` produces a line-level comparison with `UNCHANGED / ADDED / DELETED / MODIFIED` classification plus per-side line numbers and added/deleted/modified counts; shows whether the files are identical.
3. **Hex Inspection** — a `HexViewerUtil`-powered binary/hexadecimal inspector (offset, hex bytes, ASCII column) with **magic-signature guessing** for unknown or binary files.
4. **Format Converter** — local text → JSON (and related) conversion with a converted-output pane; every conversion is logged to the Room **conversion history**.
5. **Watermark & Sign** — document signing/watermark overlay tool with a **live canvas preview**, watermark text, and style selection that ties into the global watermark settings.

### Library & Private Vault

- **Library**: bookmarks (title, note, page/line) and annotations (highlight, underline, strikethrough, note, callout, drawing) per document, sorted and grouped by file.
- **Private Vault**: documents imported into app-private storage (`filesDir/private_vault`), hidden from the regular library. The vault is locked with a **PIN** (stored as a hash) and optional **biometric** unlock.
- **Encryption**: documents can be AES-256-GCM encrypted at rest in the Room table via `AesEncryptionHelper` (toggle encrypt/decrypt per document); encrypted records are flagged with `isEncryptedWithAes256` and stored in `encryptedContent`.

### Settings & Privacy

| Group | Settings |
| --- | --- |
| **Appearance & Density** | Dark theme, glassmorphism containers, blur intensity, global font scale, language (system default) |
| **Reader & Code Inspector** | Reader themes (`Light`, `Dark`, `Sepia`, `High Contrast`, `OLED Black`), code syntax themes (`Dark Modern`, `Light Clean`, `Monokai`, `Solarized Dark`, `High Contrast`), reader font size, code font size/family, restore tabs on launch, remember reading position, keep screen awake, default PDF mode (`vertical`), default Markdown mode (`split`), default code word wrap |
| **Watermarking & Signature** | Global watermark string, watermark style, auto-apply watermark on PDF export |
| **Privacy & Security Vault** | App lock (PIN + biometrics), hide content in app switcher, hide recent file names, high-contrast mode, reduced motion |
| **Database Backup** | Manual backup now, enable auto-backup with interval (hours), backup folder picker, last-backup timestamp; falls back to `externalFilesDir/backups` when no folder is configured |
| **About** | App name/description, privacy promise |

---

## Supported File Formats

| Format | Extensions | Workspace mode |
| --- | --- | --- |
| PDF | `pdf` | PDF viewer path (position remembered) |
| Markdown | `md`, `markdown`, `mdown`, `mkd` | Split source/preview editor |
| JSON | `json` | Structured tree inspector |
| YAML | `yaml`, `yml` | Structured tree inspector |
| XML | `xml` | Structured tree inspector |
| CSV / TSV | `csv`, `tsv` | Spreadsheet table viewer (auto delimiter detection, export to JSON/TSV) |
| Source code | `kt`, `java`, `py`, `js`, `ts`, `cpp`, `c`, `cs`, `go`, `rs`, `php`, `sql`, `sh`, `dart`, `swift`, `rb`, `lua`, `html`, `css`, `toml`, `ini`, `properties` | Code editor with undo/redo |
| EPUB | `epub` | eBook parser + reader |
| Images | `png`, `jpg`, `jpeg`, `webp`, `gif`, `bmp`, `svg`, `heic`, `heif` | Image viewer, OCR source |
| Plain text | `txt`, `log`, `conf`, `cfg`, `env` | Text viewer/editor |
| Archives | `zip`, `rar`, `tar`, `gz`, `7z` | Archive detection (entry inspection) |
| Binary / unknown | anything else | Hex inspection |

> File type detection is done via `DocumentFileType.fromExtension()` with a MIME-type fallback (`DocumentFileType.fromMimeType()`).

---

## Privacy & Security Model

- **No network permission.** The manifest (`AndroidManifest.xml`) declares no `INTERNET` — the app cannot send data anywhere. All parsing, OCR, diffing, conversion, duplicate scanning, and indexing are local.
- **Room database** on-device only (`lsdocs_database`, version 4, destructive-migration fallback).
- **AES-256-GCM encryption** (`AesEncryptionHelper`) — SHA-256-derived 256-bit key, random 12-byte IV per encryption, 128-bit GCM tag, Base64 output. Used for at-rest document encryption.
- **Private vault** in app-internal storage — private documents are flagged `isPrivate` in the database and excluded from public queries.
- **App lock** — PIN gate on the entire app (`AppLockScreen`) with biometric unlock via `USE_BIOMETRIC` permission.
- **App-switcher privacy** — hide content in the recents overview (setting-backed).
- **Scoped storage** — Android 13+ media reads use `READ_MEDIA_IMAGES`; legacy storage permissions are capped to old API levels.
- **Intent-based opening** — the app registers `VIEW`/`EDIT` intent filters for files and content URIs (PDF, text, JSON, XML, CSV, EPUB, images, ZIP) so documents open from other apps directly into the workspace.

---

## Tech Stack

| Layer | Choice |
| --- | --- |
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (Material 3, Compose BOM `2024.09.00`), Material Icons Extended |
| Async | Kotlin Coroutines (core + android, `1.10.2`) |
| Persistence | Room `2.7.0` (runtime, KTX) + KSP codegen |
| Navigation | Navigation Compose `2.8.9` |
| Lifecycle | Lifecycle runtime/viewmodel `2.8.7` (compose + ktx) |
| Images | Coil Compose `2.7.0` |
| Build | Android Gradle Plugin `9.1.1`, Kotlin Compose plugin, KSP `2.3.5`, Gradle configuration cache, parallel builds |
| Secrets | Google Secrets Gradle Plugin `2.0.1` (reads `.env` / `.env.example`) |
| Firebase | google-services plugin wired with `WARN` passthrough (not required to build) |
| Tests | JUnit 4, Robolectric `4.16.1`, Roborazzi `1.59.0` (Compose screenshot tests), Espresso |

**Build configuration**

| Property | Value |
| --- | --- |
| Application ID | `com.aistudio.lsdocs.app` |
| Namespace | `com.example` |
| minSdk | 24 (Android 7.0) |
| targetSdk / compileSdk | 36 (with minor API 1) |
| Java compatibility | 11 |
| versionCode / versionName | `1` / `1.0` |
| JVM args | `-Xmx4g`, UTF-8, workers max 4, in-process Kotlin compiler |

---

## Project Structure

```
ls-docs/
├── app/
│   ├── build.gradle.kts              # app module build config
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml   # no INTERNET permission; VIEW/EDIT intent filters
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt   # entry point, nav bar, app lock gate
│       │   │   ├── repository/
│       │   │   │   └── DocumentRepository.kt   # source of truth: Room + in-memory tabs/settings
│       │   │   ├── ui/
│       │   │   │   ├── viewmodel/MainViewModel.kt
│       │   │   │   ├── screens/      # Home, Browse, Workspace, Tools, Library, Settings, AppLock
│       │   │   │   ├── components/   # HighDensityCard, HighDensityBadge, glassmorphism, empty states
│       │   │   │   └── theme/        # Color / Type / Theme
│       │   │   └── data/
│       │   │       ├── database/     # AppDatabase, Entities, Daos (Room)
│       │   │       ├── model/        # DocumentFileType, ReaderTheme, CodeSyntaxTheme, OpenTab, AppSettings…
│       │   │       └── util/         # parsers, OCR, diff, hex, crypto, printing, scanning
│       │   └── res/                  # launcher icons, themes, strings, backup rules
│       ├── test/                     # Robolectric unit + Roborazzi screenshot tests
│       └── androidTest/              # instrumented tests (Espresso)
├── gradle/
│   └── libs.versions.toml            # version catalog
├── assets/.aistudio/                 # AI Studio asset scratch space (gitignored)
├── build.gradle.kts                  # root build (plugin declarations)
├── settings.gradle.kts               # root project "LS Docs", :app module
├── gradle.properties                 # daemon/parallel/cache/flags
├── .env.example                      # optional GEMINI_API_KEY placeholder
├── .gitignore
└── metadata.json                     # AI Studio app metadata
```

### Key source files

| File | Responsibility |
| --- | --- |
| `MainActivity.kt` | 6-tab shell (`Home`, `Browse`, `Viewer`, `Tools`, `Library`, `Settings`), top header, bottom navigation, theme wiring, app-lock gate |
| `MainViewModel.kt` | Exposes all Room flows + commands: open/close/save tabs, bookmarks, annotations, OCR, diff, duplicate scan, vault lock, backup, settings |
| `DocumentRepository.kt` | Orchestrates DAOs, in-memory tab state, settings state, vault import, DB backup, AES encrypt/decrypt toggles |
| `data/util/FileHelper.kt` | URI/file I/O, device storage scanning, truncation guards, physical deletes |
| `data/util/AesEncryptionHelper.kt` | AES-256-GCM encrypt/decrypt (SHA-256 key derivation, random IV, Base64) |
| `data/util/OcrEngine.kt` | On-device bitmap OCR pipeline + `OcrResult` model |
| `data/util/DiffEngine.kt` | Line-level text diff (added/deleted/modified/identical) |
| `data/util/DuplicateScanner.kt` | Size-based duplicate grouping across a folder (groups files with identical byte sizes) |
| `data/util/CsvParser.kt`, `MarkdownParser.kt`, `EpubParser.kt`, `StructuredDataParser.kt` | Format parsers (tables, TOC, chapters, validation) |
| `data/util/HexViewerUtil.kt`, `CodeSyntaxHighlighter.kt`, `PrintHelper.kt` | Hex rows, syntax tokenizer, printing support |

---

## Data Persistence (Room Schema)

Database name: **`lsdocs_database`** (version 4, `fallbackToDestructiveMigration`).

| Table | Entity | Purpose |
| --- | --- | --- |
| `document_records` | `DocumentRecord` | Recent/pinned/favorite/private documents; reading progress, page, and encrypted content at rest |
| `bookmark_records` | `BookmarkRecord` | Bookmarks with title, note, page/line, file type |
| `annotation_records` | `AnnotationRecord` | Highlights, underlines, strikethroughs, notes, callouts, drawings (per page/line, color hex, extra data) |
| `ocr_records` | `OcrRecord` | OCR history (image URI, extracted text, language, timestamp) |
| `conversion_records` | `ConversionRecord` | Format conversion history (source → target → output URI) |
| `document_metadata` | `DocumentMetadataRecord` | Rich per-document metadata: counts (words/chars/lines), author, description, timestamps, searchable |

DAOs (`DocumentDao`, `BookmarkDao`, `AnnotationDao`, `OcrDao`, `ConversionDao`, `DocumentMetadataDao`) expose reactive `Flow`-based queries; all screens subscribe through the ViewModel with `stateIn(WhileSubscribed(5000))`.

---

## Getting Started

### Prerequisites

- **JDK 17+** (AGP 9.x requires JDK 17 or newer)
- **Android Studio** (latest stable, e.g., Ladybug or newer) with SDK Platform 36
- Android SDK Build Tools + Platform 36 (the build can auto-download via the SDK manager)
- Gradle is managed via the wrapper — no manual install needed
- (Optional) ADB-connected device or emulator for instrumented tests

### Open in Android Studio

1. `File > Open` and select the repository root (`ls-docs/`).
2. Let Gradle sync (AGP `9.1.1`, Kotlin `2.2.10` will be resolved on first sync).
3. Select the `app` run configuration, choose a device/emulator, and press **Run**.
4. If `local.properties` is missing, Android Studio will create it with your SDK path. (It is gitignored.)

### Build from the command line

```powershell
# Configure (needs your SDK location)
# Ensure a local.properties exists with: sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk

# Debug APK
.\gradlew.bat assembleDebug

# Release APK (see signing section first)
.\gradlew.bat assembleRelease

# Install on device
.\gradlew.bat installDebug
```

`local.properties` (gitignored):
```
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
```

---

## Environment Variables & Secrets

The project uses the **Gradle Secrets Plugin**, which reads a local `.env` file (with `.env.example` as the default template).

```
# .env.example
# GEMINI_API_KEY=MY_GEMINI_API_KEY
```

- The app is **fully offline by default**, so no key is required to build or run.
- `GEMINI_API_KEY` is a placeholder consumed by Google AI Studio (declared as a server-side capability in `metadata.json`); uncommenting it packages the key into the APK for AI Studio tooling. Leave it commented out for a keyless, network-free build.
- `FIREBASE_APPCHECK_DEBUG_TOKEN` is excluded from packaging via the secrets plugin `ignoreList`.
- `google-services.json` is **optional** — `googleServices.missing.passthrough=true` and `MissingGoogleServicesStrategy.WARN` keep builds green without Firebase setup.

---

## Signing & Release Builds

Signing is configured via environment variables or defaults:

| Item | Source | Default |
| --- | --- | --- |
| Keystore | `KEYSTORE_PATH` | `<project>/my-upload-key.jks` |
| Store password | `STORE_PASSWORD` | — |
| Key alias | fixed | `upload` |
| Key password | `KEY_PASSWORD` | — |
| Debug keystore | — | `<project>/debug.keystore` (`android`/`android`/`androiddebugkey`) |

To produce a signed release:

```powershell
$env:KEYSTORE_PATH = "C:\path\to\upload-key.jks"
$env:STORE_PASSWORD = "<store-pass>"
$env:KEY_PASSWORD   = "<key-pass>"
.\gradlew.bat assembleRelease
```

Release build flags: minification **disabled** for now (ProGuard rules file present and ready to enable), `isCrunchPngs = false`, release signing from the `release` signing config, and dependency info excluded from the APK (included in the bundle).

---

## Testing

The project has three test layers, all runnable without a device:

```powershell
# 1) Robolectric unit tests (JVM, no emulator)
.\gradlew.bat test

# 2) Roborazzi Compose screenshot tests (JVM, renders UI to images)
.\gradlew.bat validateDebugScreenshotTest   # or: recordDebugScreenshotTest to regenerate

# 3) Instrumented tests (device/emulator required)
.\gradlew.bat connectedDebugAndroidTest
```

| Test | Layer | What it covers |
| --- | --- | --- |
| `ExampleUnitTest.kt` | unit | Plain JVM assertion |
| `ExampleRobolectricTest.kt` | Robolectric | Framework-dependent behavior on the JVM |
| `GreetingScreenshotTest.kt` | Roborazzi | Compose UI rendered to screenshot artifacts |
| `ExampleInstrumentedTest.kt` | instrumented | Espresso on device/emulator |

Roborazzi screenshots are emitted under `app/build/outputs/roborazzi/` (verify mode compares against recorded golden images). `testOptions { unitTests { isIncludeAndroidResources = true } }` is enabled so Robolectric can load real resources.

---

## Troubleshooting

| Problem | Fix |
| --- | --- |
| `SDK location not found` | Create `local.properties` with `sdk.dir` pointing at your Android SDK |
| `Could not connect to Kotlin compile daemon` | Already mitigated by `kotlin.compiler.execution.strategy=in-process` in `gradle.properties`; also restart the Gradle daemon (`.\gradlew.bat --stop`) |
| Slow first build | Expected — AGP 9 + configuration cache warms up; subsequent builds use `org.gradle.caching=true` |
| `google-services.json` warnings | Expected & safe; building without Firebase config is intentional (`missing.passthrough=true`) |
| Build fails on AGP 9 plugins | Ensure Gradle wrapper + JDK 17+ are current; AGP `9.1.1` requires recent tooling |
| Debug keystore missing | Run any debug build once (`assembleDebug`) — the signing config points at `<root>/debug.keystore`; generate with `keytool -genkeypair -v -keystore debug.keystore -storepass android -keypass android -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000` |

---

## Roadmap

- [x] Offline multi-format reader & inspector
- [x] Tabbed workspace with lightweight editing (undo/redo, Markdown split preview)
- [x] Private vault (PIN + biometrics) with AES-256-GCM document encryption
- [x] On-device toolbelt: OCR, diff, hex, conversion, watermarking, duplicate scan
- [x] Room-based history, bookmarks, annotations, metadata, backups
- [ ] Enable release minification (ProGuard) once rules are validated
- [ ] Full Text-to-Speech and accessibility polish pass
- [ ] Real neural OCR model (ML Kit / on-device) replacing the inspection stub
- [ ] Cloud-independent sync/export of vault backups
- [ ] Gemini API assistant (server-side capability scaffolded in `metadata.json`)

---

## Contributing

1. Fork the repository and create a feature branch.
2. Follow existing conventions: Kotlin official style, Compose Material 3 components, `HighDensity*` components for lists/cards, Room flows exposed through `MainViewModel`.
3. Add tests (Robolectric for logic, Roborazzi for UI screens) and run `.\gradlew.bat test validateDebugScreenshotTest`.
4. Open a pull request describing the change and the verification performed.

---

## License

No license file is currently included in this repository — all rights reserved by default. Add a `LICENSE` (e.g., Apache-2.0 or MIT) before distributing the app.
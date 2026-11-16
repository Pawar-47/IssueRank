# ILs

ILs (Issue List) is an offline-first native Android app for ranking records — people, tickets
or reported issues — and tracking whether each one is completed and whether its issue is
actually resolved. Kotlin, Jetpack Compose, Room, DataStore. The interface follows iOS
system-app conventions (large titles, inset grouped lists, floating translucent tab bar,
sheets, segmented controls). Android 13 (API 33) and newer.

The written quality bar, with measurable targets and an honest status table, is in
[docs/DESIGN_STANDARDS.md](docs/DESIGN_STANDARDS.md).

## Tabs

| Tab | Purpose |
| --- | --- |
| **Rank** | Dashboard on top, then every record in priority order. Edit mode gives up/down controls; ranks renumber automatically. |
| **Name** | A–Z directory with initial avatars. |
| **Issues** | Issue title, description and notes, filtered All / Open / Resolved. |
| **Status** | Pending and Completed, with a switch per row. |
| **Resolved** | Resolved and Not Resolved sections, switches, swipe to resolve or delete. |

Status and Resolution are independent: a record can be Completed and still Not Resolved.

## Dashboard

Resolution ring, Total / Pending / Completed / Overdue counts, four tappable stat tiles,
completion and resolution progress bars, quick actions (New, Search, Share), and three short
lists: Top priority, Needs attention, Recently updated. Everything updates immediately after
any change.

## Search

Open it from the dashboard field or the magnifier in any top bar. It searches name, issue
title, description and notes; case- and accent-insensitive; every word must match. Scope
(All / Name / Issues), filter (Any / Open / Resolved / Pending / Completed) and sort (Best
match / Rank / Recently updated / Name). Matches are highlighted, recent searches are kept on
the device.

## Records and dates

Each record has a unique ID, rank, name, issue title, description, notes, status, resolution,
**created** and **last-updated** timestamps (set automatically), and optional **reported**,
**expected** and **resolved-on** dates. Deleting is undoable for four seconds; cancelling an
edit with unsaved changes asks first.

## Privacy

No permissions, no account, no network access, no analytics. Data lives in a local Room
database and a small DataStore file.

## Getting the APK

GitHub Actions builds every push to `main`.

1. Open the repository's **Actions** tab and pick the latest **Build ILs** run.
2. Download the **ILs** artifact; it contains **`ILs.apk`**.
3. Install it on an Android 13+ phone (allow "install unknown apps" for your browser or
   file manager).

Pushing a tag such as `v2.0` additionally publishes `ILs.apk` to **Releases**.

The workflow runs, in order: lint, unit tests, debug APK, release APK, instrumented-test
compile, APK validation (including a signature check). A separate job runs the instrumented
tests on an API 34 emulator. A failing step fails the run; artifacts are only uploaded when
the build steps passed.

> The release APK is signed with the debug key unless you add a keystore. To use your own,
> provide `ILS_KEYSTORE_PATH`, `ILS_KEYSTORE_PASSWORD`, `ILS_KEY_ALIAS` and `ILS_KEY_PASSWORD`
> to the workflow environment.

## Building locally

JDK 17 and the Android SDK:

```
./gradlew :ILs:testDebugUnitTest :ILs:assembleRelease
```

Output: `ILs/build/outputs/apk/release/ILs-release.apk`.

## Layout

```
ILs/src/main/java/com/pawar47/issuerank/
  MainActivity.kt        edge-to-edge host
  EntriesViewModel.kt    one AppState flow: records + dashboard + search + recents
  domain/                pure Kotlin: Entry, Search, Stats, Ranking, Dates, Recents, Summary
  data/                  Room entity/DAO/database, EntryRepository, DataStore settings,
                         one-time import of version 1.0 data
  ui/                    design system (Theme, Components, Glyphs), Dashboard, Screens,
                         SearchScreen, EntryEditor, SwipeRow, App
ILs/src/test/            unit tests for everything in domain/
ILs/src/androidTest/     database integrity and UI smoke tests (run on an emulator in CI)
```

## Upgrading from 1.0

Version 1.0 kept records in SharedPreferences. On first launch of 2.0 they are copied into the
database once (the old notes become the issue description) and the old copy is removed.

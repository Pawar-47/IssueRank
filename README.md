# IssueRank

A native Android app for ranking people, tickets or issues and tracking their status.
It is built with Kotlin and Jetpack Compose and follows the iOS 27 (Liquid Glass) design
system: large titles, inset grouped lists, translucent floating tab bar, iOS switches,
segmented controls and bottom sheets.

## Features

Five tabs over one shared list of entries:

- **Rank** — entries ordered by priority, rank 1 first. Edit mode swaps the status badges
  for up/down controls so the order can be changed; ranks renumber automatically.
- **Name** — alphabetical index with A–Z sections, initial avatars and a live search field.
- **Issues** — the issue description of every entry, with filters for All / Open / Resolved.
  Notes are written in the editor sheet and shown on the card.
- **Status** — pending and completed entries in two segments, each row carrying an iOS
  switch that moves the entry between the two states.
- **Resolved** — counts plus two sections (Not Resolved / Resolved) with switches that
  flip an entry's resolution.

Every entry stores a rank (its list position), a name, an issue description (with room for
short notes), a status (Pending / Completed) and a resolution (Resolved / Not Resolved).
Entries are created and edited in a sheet with Cancel / Save, and deleted from the same
sheet behind a confirmation tap. Everything is stored locally; no network permission is
requested.

## Requirements

- Android 13 (API 33) and newer, including the latest release.
- Built in CI with JDK 17, Android Gradle Plugin 8.11.1, Gradle 8.13, Kotlin 2.2.0,
  compileSdk / targetSdk 36, Compose BOM 2025.06.01.

## Build

The APK is compiled on GitHub Actions (`.github/workflows/build-apk.yml`) on every push to
`main` and on demand:

1. Open the **Actions** tab.
2. Pick the latest **Build APK** run (or run the workflow manually).
3. Download the **IssueRank-debug-apk** artifact and install `app-debug.apk` on the phone
   (allow "install unknown apps" for whatever you download it with).

Locally, with a JDK 17 and an Android SDK installed:

```
./gradlew :app:assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

## Project layout

```
app/src/main/java/com/pawar47/issuerank/
  MainActivity.kt            edge-to-edge host activity
  EntriesViewModel.kt        list state, reordering and persistence
  data/Entry.kt              model, status / resolution enums, JSON store
  ui/Theme.kt                iOS system colors and type scale
  ui/Glyphs.kt               hand-drawn SF-style tab and control glyphs
  ui/Components.kt           glass surfaces, grouped cards, switch, segmented control, tab bar
  ui/Screens.kt              the five tab screens
  ui/EntryEditor.kt          create / edit sheet
  ui/App.kt                  tab switching and sheet presentation
```

## Notes

- No third-party UI library: the iOS look is implemented directly on Compose foundation, so
  there is no Material styling leaking through.
- Data is a single JSON document in `SharedPreferences`, written on every change.
- Ranks are derived from list order, which keeps them unique and gap-free.

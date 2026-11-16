# ILs — Product, Industry and Design Standards

This is the written bar the app is built against. Every line is a number or an observable
behaviour, so it can be checked rather than argued about. Section 7 records honestly what
is and is not done.

Units: iOS points map 1:1 to Android dp.

---

## 1. Product

ILs (Issue List, formerly IssueRank) is a native Android app for ranking records and tracking
their issues. Android 13 (API 33) through the latest release. Built by GitHub Actions and
delivered as **`ILs.apk`**.

Five tabs over one shared list: **Rank** (also the dashboard), **Name**, **Issues**,
**Status** (Pending / Completed), **Resolved** (Resolved / Not Resolved). Status and
Resolution are independent: a record can be Completed and still Not Resolved.

Global **Search** is reachable from the dashboard search field and from the magnifier in every
screen's top bar.

---

## 2. Industry standard, in tangible terms (OEM-app parity)

What stock apps from Google, Samsung and Apple do that a hobby app usually does not:

| Area | Measurable standard |
| --- | --- |
| Data safety | Records in a real database (Room/SQLite), multi-row writes in one transaction, no destructive migration, stable IDs |
| Offline / privacy | Works with no network; the manifest declares **zero permissions**; no analytics; no account |
| Reversibility | Delete is undoable for 4 s; Cancel with edits asks before discarding; Back never loses typed text silently |
| Lifecycle | State survives rotation, font-size, dark-mode change and process death; UI collects flows with lifecycle awareness |
| Responsiveness | All database work off the main thread (suspend + Flow); search is pure in-memory Kotlin, recomputed per keystroke; lists are lazy |
| Accessibility | Roles on tabs and switches, content descriptions on icon buttons, 44 dp targets, all text in `sp`, meaning never by colour alone |
| Platform fit | Edge-to-edge, predictive back opted in, themed (monochrome) icon, light/dark follows system, share sheet for data export |
| Time | Created / updated stored on every record, optional reported / expected / resolved dates, one date formatter app-wide |
| Quality gate | Lint + unit tests + both APKs + instrumented-test compile on every push; emulator tests on `main` |
| Delivery | Tagged release (`v*`) publishes `ILs.apk` to GitHub Releases |

---

## 3. iOS system-app standard, in tangible terms

### 3.1 Layout and touch
- Minimum tap target **44 x 44**; 32 glass buttons sit inside hit areas of at least that size.
- Screen side margin **16**. Inset grouped cards: **16** from the edges, corner radius **16**.
  Sheets: **28** top radius. Pills: fully rounded.
- Row minimum height **44**; row padding **16 / 12**; separators **0.5** thick, inset **16**.
- Section header: **13** medium, uppercase, secondary label colour.
- Spacing scale: 4 / 8 / 12 / 16 / 20 / 28.

### 3.2 Typography (Dynamic Type "Large")
Large title 34 bold · Title 22 semibold · Headline 17 semibold · Body 17 · Subhead 15 ·
Footnote 13 · Caption 12 · Tab label 10. All in `sp`, so the system font scale applies.

### 3.3 Colour
- System palette: blue `#007AFF` (dark `#0A84FF`), green `#34C759`/`#30D158`, orange
  `#FF9500`/`#FF9F0A`, red `#FF3B30`/`#FF453A`.
- Grouped background `#F2F2F7` (dark `#000000`); cards `#FFFFFF` (dark `#1C1C1E`).
- Three label levels (primary, secondary 60 %, tertiary 30 %) — never arbitrary greys.
- Accent restraint: blue is the only interactive colour; green/orange/red carry state only.

### 3.4 Navigation and chrome
- **Large title** that collapses to a compact 54 dp bar with a hairline as the list scrolls.
- **Floating translucent tab bar**: icon 26 over a 10 label, selected tab in system blue,
  announced as a selected tab.
- **Sheets** for create/edit: slide up, dimmed scrim, 36 x 5 grabber, Cancel left, title
  centre, prominent Save right.
- **Search** is a full-height layer with a Cancel button, scope control, filter chips.
- **Glass**: translucent fill + 0.7 hairline + soft shadow.

### 3.5 Motion and feedback
- **No Android ripple** anywhere. Switches animate 180 ms; sheet and banner use eased slides.
- Haptics on tab changes and swipe actions.
- Swipe actions on Resolved rows: swipe right = Resolve / Reopen, swipe left = Delete;
  action width 92; a long swipe commits.

### 3.6 Content states
- Every list has a designed empty state (glyph in a 64 circle, headline, one line of help).
- Zero data on the dashboard shows a welcome card, not an empty grid.
- Destructive actions are undoable (banner) rather than blocked behind modal dialogs.

---

## 4. Dashboard (top of the Rank tab)

| Block | Content |
| --- | --- |
| Search field | Opens global search |
| Overview card | Ring showing % resolved + counts: Total, Pending, Completed, Overdue |
| Stat tiles (2 x 2) | Records, Open, Pending, Resolved; each opens its tab |
| Progress bars | Completion % (completed / total) and Resolution % (resolved / total) |
| Quick actions | New, Search, Share |
| Top priority | Highest-ranked open records (3) |
| Needs attention | Open records not already shown above: overdue first, then longest untouched (3) |
| Recently updated | Last 3 edited records with relative time |

All values are derived from the same database flow, so they update the instant a record
changes.

---

## 5. Search

- **Fields:** name, issue title, issue description, notes.
- **Scope:** All / Name / Issues. **Filter:** Any / Open / Resolved / Pending / Completed.
  **Sort:** Best match / Rank / Recently updated / Name.
- **Matching:** case- and accent-insensitive; every word must match (AND), in any order.
- **Relevance:** exact name > name prefix > name word start > name contains > title >
  description > notes; ties keep rank order.
- **Highlighting** of matches in name and snippet; the snippet is centred on the first match.
- **Result header** with correct singular/plural; **Reset** when scope or filter is narrowed.
- **Recents:** last 8 searches in DataStore, individually removable, Clear all.
- **Empty states:** no query shows recents; no hits explains how to widen the search.
- **Keyboard:** focused on open, Search key records the query, Cancel and Back close it.

---

## 6. Architecture

```
ui/        Compose design system and screens (no business rules)
domain/    Pure Kotlin: Entry, Search, Stats, Ranking, Dates, Recents  <- unit tested
data/      Room (EntryEntity, EntryDao, IssueRankDatabase), EntryRepository,
           DataStore (SettingsRepository), LegacyStore (one-time import from v1)
EntriesViewModel   combines repository + search query + settings into one AppState flow
```

Rules: the UI never touches the DAO; timestamps and trimming are applied only in the
repository; rank bookkeeping lives in `domain/Ranking.kt` and runs inside DAO transactions.

---

## 7. Status against this document

| Item | State |
| --- | --- |
| Five tabs, all record fields, dates, ranking, status, resolution | Implemented |
| Dashboard, global search, recents, highlighting, sort/filter | Implemented |
| Room persistence with transactions, undo delete, v1 data import | Implemented |
| Unit tests (search, stats, ranking, dates, recents, model) | Written; run in CI |
| Instrumented tests (database ranks, UI smoke) | Written; run on the CI emulator |
| Lint, debug + release APK, `ILs.apk` artifact, tagged releases | Configured in CI |
| **Verified by an actual CI run** | **Pending the first run after this commit** |
| Database **migration tests** | Not written: schema is v1, there is no migration to test yet |
| Search filter by **date range** | Not implemented (sort by updated date is) |
| Real **background blur** for glass | Not implemented (translucent fill + shadow) |
| Strings in `strings.xml` / localisation | Not done (English text is inline) |
| **Play-ready signing** | Release APK is signed with the debug key unless `ILS_KEYSTORE_*` env secrets are supplied |
| Screenshot / golden-image tests, orientation and font-scale test matrix | Not done |

## 8. Known deviations from iOS

- San Francisco and SF Symbols are not licensed for Android: the font is the system font and
  the icons are hand-drawn shapes.
- The system date picker (Material) is used for choosing dates; it is themed but not iOS-styled.
- Swipe actions exist on the Resolved tab, not on every list.

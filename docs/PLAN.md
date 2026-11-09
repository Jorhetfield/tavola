# Tavola plan (restart)

Tick `[x]` when done. One task at a time; build and verify before the next one.

## Next up (agreed order)
1. ~~Clear the small pending checks~~ Done: the recipe CRUD is merged (PR #5), the `strings.xml` prefixes are in and both emulator checkpoints are passed.
2. ~~Past days collapsed~~ Done (PR #8).
3. ~~Tasks per day~~ Done: add, tick, remove with Undo and edit the text were already in; this closed it with the "Add task" chip, read-only past days and editing by long press (PRs #10 and #11).
4. ~~Close 0.0.3 and open 0.1.0~~ Done: `release/0.0.3` is in `main` (PR #13, pre-release `v0.0.3`) and `release/0.1.0` is open for Firebase.
5. ~~Firebase + Auth + groups, then the shared calendar in real time~~ Done in release 0.1.0 (decisions below): the biggest change to the data layer, in 4 or 5 small PRs. It goes before the widget because the widget reads through the repositories and would have to be reworked after the move.
6. **Interactive widget** (Phase 3): it can be defined on paper any time (open questions below), but it is built after Firebase.
7. ~~Shopping list~~ Done in release 0.1.1 (PRs #21, #22 and #23, decisions below). Still to do: recipe photos and carousel, import by URL, push notifications (Phase 4).
8. AI (Phase 5), last.

## Firebase decisions (agreed)
| # | Decision |
|---|---|
| 1 | Login with **Google** only (Credential Manager). Name and photo come from the Google account. |
| 2 | **Firestore only**: its disk cache gives the offline behavior. Room goes away from the calendar and the recipes. |
| 3 | The current local data is **discarded** (it is test data); the debug menu can still load sample data. |
| 4 | **One group per person**; joining is by an **invite code** (`inviteCode` in the model). No invite links for now. |
| 5 | Recipes belong to **the group**. Every member has the same permissions: no roles, no admin. |
| 6 | The author (`addedBy`, `ownerId`) is **stored but not shown** yet. The member color is assigned from the Mediterranean palette. |
| 7 | **Spark plan** (free) and security rules that only let the members of a group read and write it. **One Firebase project** for debug and release. |
| 8 | Close 0.0.3 and open **0.1.0**; Firebase goes in **4 or 5 small PRs**. |

Groups decisions (agreed for PR 2, which is split in 2a data and rules, and 2b screens):
| # | Decision |
|---|---|
| 1 | Joining by code uses a separate `invites/{code}` document that only holds the group id. It can be read by its exact id (`get`), never listed. The group itself stays members-only. No Cloud Functions (Spark plan). |
| 2 | The code has **8 characters** (letters and digits without look-alikes), **never expires**, and **any member can renew it** (the old one stops working). |
| 3 | After the first sign-in the person chooses: **create a group** (asks for a name) or **join with a code**. |
| 4 | When the last member leaves, **nothing is deleted** (the group stays without members). Leaving is done from a "My group" screen (name, code, members, leave with a confirmation). |
| 5 | The creator chooses the **name**, and any member can rename it. **Nobody can remove another member.** |

Data decisions (agreed for PRs 3 and 4):
| # | Decision |
|---|---|
| 1 | **Recipes go first, the calendar second.** A planned meal refers to a recipe by id, so the calendar can only be shared once the recipes are. |
| 2 | The **debug menu goes away completely**: "Load sample data", "Clear everything", the calendar button and the design showcase screen (`:feature:showcase`, which was only reachable from that menu; it stays in the git history). |
| 3 | When a group is **created**, it gets **three example recipes** (Gazpacho, Tortilla de patatas and Pasta al pomodoro, complete, with ingredients and steps) that the creator can delete like any other. A use case in `:domain` does it, since it combines the group and the recipe repositories. Joining a group adds nothing. |
| 4 | Recipes live at `groups/{groupId}/recipes/{recipeId}` with the ingredients and the steps inside the document; any member reads and writes them (rules in `firestore.rules`). The author (`createdBy`) is stored, not shown. |
| 5 | Real-time updates need nothing more than the Firestore listeners, so they arrive with each repository; PR 5 is only the rules and the checks. |

Planned PRs, each one building, tested and reviewed on its own:
1. Firebase and Auth with Google sign-in.
2. Groups with an invite code, in two PRs: 2a the group model, the Firestore repository and `firestore.rules` (the group, the members and the invites); 2b the screens (create or join, "My group") and the session gate with a group.
3. The recipes in Firestore behind `RecipeRepository`, the example recipes of a new group, and the debug menu out (see "Data decisions").
4. The calendar in Firestore behind the same `CalendarRepository`. Once it is in, remove the Room code and dependency, and update `CLAUDE.md` (`:data` is described as Firestore/Room) and the README, which still mention Room.
5. The security rules for the calendar and a check of the whole flow between two accounts (the real-time updates come with the Firestore listeners of PRs 3 and 4).

Still to decide, each before the PR that needs it (these came out of the review of the decisions above):
- ~~Joining by code with member-only rules~~ Decided in PR 2: an `invites/{code}` document read by its exact id (see "Groups decisions").
- ~~Debug menu on shared data~~ Decided: the debug menu is removed (see "Data decisions").
- **First launch** (decided in PR 1, to confirm): the app opens on the sign-in screen when nobody is signed in; a stored session is kept offline. Cancelling the Google sheet shows nothing; no connection, no Google account on the phone and any other failure show their own message with the button still there. There is no way to use the app without an account.
- ~~Leaving a group~~ Decided in PR 2: nothing is deleted when the last member leaves; the code can be renewed (see "Groups decisions").

To check before or while doing it:
- **First launch of a person who already has a group, with no network.** The group gate waits for the server's answer about their group (so a person on a new phone does not see the first step for a moment). With no connection on a phone where the app was never used, it keeps the loading screen. Decide what to show after a while (a message and a retry) when there is time.
- **Firestore rules are published by hand and not tested with the emulator.** `firestore.rules` (PR 2a) is pasted in the Firebase console (Firestore Database, Rules, Publish) or deployed with `firebase deploy --only firestore:rules`; the repository only works against the rules that are live. There is no Node here, so the rules were not run in the Firebase emulator; they are checked by the console when published and by trying the flows in the app (PR 2b). Add emulator tests (`@firebase/rules-unit-testing`) when Node is available, before the rules grow with the calendar and the recipes.
- **Recipe photos and the Spark plan.** Firebase Storage may need the paid Blaze plan, even on a new project. Verify before planning the photos (Phase 4); it may change decision 7 or postpone them.
- ~~Google sign-in in the CI APK~~ Verified: the CI APK signs in and syncs on a second device.
- **`google-services.json` in CI.** Decided: it is committed (it holds no secrets), in PR #14. Follow-up for the owner of the project: restrict its API key in Google Cloud (APIs and services, Credentials) to the package `com.tavola` and the SHA-1 of the debug and CI keys, because the key identifies the project and is only safe to commit if it is restricted.
- **The widget.** It must learn the group and the signed-in state, and decide how it refreshes (listener or periodic work) and what it shows with no session or no network.

Shopping list decisions (agreed):
| # | Decision |
|---|---|
| 1 | One list per group, shared in real time. One document per item at `groups/{groupId}/shopping/{itemId}` (no array in a single document, so concurrent adds do not overwrite each other). |
| 2 | Item: `name`, `category`, `checked`, `quantity?` (free text), `createdAt`, `addedBy` (stored, not shown). |
| 3 | Fixed Mediterranean categories: fruit and vegetables, meat and fish, dairy and eggs, pantry, bakery, frozen, drinks, household, other (default). Not editable. |
| 4 | New "Shopping" tab in the floating bar, items grouped by category; ticking strikes the item and sends it to the bottom of its category. |
| 5 | **Changed while building it:** adding is done from a bottom sheet opened by the "+" docked to the floating bar (like "New recipe"), not from an inline field with chips. The sheet has the name, an optional quantity and the category, which is **guessed from the name** (`ShoppingCategorySuggester`, Spanish and English); tapping it opens a grid of tiles to pick another. It stays open after adding. **Autocomplete from the group history is a second iteration.** |
| 6 | Swipe to remove with a 5 s Undo, and "Remove ticked" after shopping, also with Undo. |
| 7 | From a recipe: "Add to shopping list" opens a sheet where the user **picks ingredients by hand** (some may already be at home). Items already on the list are not duplicated; "to taste" ingredients go without quantity. | Done: the sheet is provided by `:app` from `:feature:shopping`; ingredients already on the list (same name ignoring case, accents and plural) cannot be picked, and everything is added in one batched write.
| 8 | Adding a whole week from the calendar is out for now. |
| 9 | New `:feature:shopping`. `:app` hands the recipe button to `:feature:recipes` through a slot; communication goes through the domain repository. |
| 10 | Quantities are free text: no summing or unit conversion. |
| 11 | Same rule as recipes and the calendar: only group members read and write. |
| 12 | Design: Material 3 Expressive like the rest of the app, with `motionScheme` for every animation, touch feedback on everything pressable, light and dark, and screenshots (light, dark, large text). |

PRs (all merged into 0.1.1): 1 domain, Firestore repository, rules, fake and tests (#21); 2 the screen, the tab and the add sheet (#22); 3 add from a recipe (#23). The swipe-to-remove gesture and the entrance motion moved to `:core:designsystem` so recipes and shopping share them.

To check: the add sheets were never tried on a device (keyboard and focus), and the Firestore rules for `shopping` must be published by hand in the console.

Shopping list redesign decisions (agreed, Bring-style; they replace decisions 4, 5, 6 and 8 above, and the rest stay):
| # | Decision |
|---|---|
| 1 | Product icons come from **Noto Emoji** (free: Apache 2.0 for the images, OFL for the font), bundled as **~128 px WebP**, not as vectors (the SVGs use gradients that Android converts badly). The license notice goes in the app. Check the exact license of the files before bundling them. |
| 2 | An unknown product is stored anyway. It shows the icon of its guessed category on a tile tinted with the category color, or a generic basket when no category is guessed. |
| 3 | The list is a **grid like Bring**: tiles with a big icon and the name below, grouped by collapsible category sections in supermarket order. 4 columns on a phone, more on a foldable or tablet. The quantity is a small label on the tile. |
| 4 | **Tap a tile = bought**: it leaves the list and goes to the history. No strike-through, no "Remove ticked", no swipe to remove. |
| 5 | **Long press = edit**: a small sheet with quantity or note, category and "Remove without buying" (which does not go to the recent strip). |
| 6 | A strip "Bought recently" at the end of the list: what was bought in the last **24 h**, at most **12 tiles**, shared with the group. Tapping a tile adds it back. It is the safety net for an accidental tap. |
| 7 | The "+" docked to the floating bar opens a **big sheet**: a search field on top, then "Recent" and the **built-in catalog** by category as a grid. Tapping a tile adds it and the sheet stays open. A tile already on the list shows a mark, and a second tap removes it. |
| 8 | Typing filters and autocompletes from the history and the catalog. When nothing matches, the first option is `Add "x"`. |
| 9 | The quantity is parsed from the text **only when a new product is created** ("Chicken 1 kg", "2 milk"). Catalog tiles are added without quantity; it is set by long press. |
| 10 | The **history belongs to the group** and is ordered by use. Long press on an own tile offers "Forget"; catalog entries cannot be deleted, only hidden from "Recent". |
| 11 | The built-in catalog has about 150-200 common products in Spanish and English with synonyms. Matching ignores case, accents and plurals, like `ShoppingNames`. |
| 12 | Adding a whole week from the calendar is back in: it merges the ingredients of the planned recipes, joins duplicates and lets the user untick what is already at home. |
| 13 | Data: new collection `groups/{groupId}/history/{key}` with `name`, `category`, `useCount`, `lastUsedAt`, `lastBoughtAt?` and `hidden`. Buying deletes the list document and updates the history; "Bought recently" is derived from the history, so no ticked documents remain. `checked` is retired. Existing items keep working: their icon is resolved at runtime by name against the catalog, so nothing new is stored on the item. |
| 14 | Where things live: the catalog and the icon resolution in `:domain` (no Android), the images in a resources module, the tile and the grid in `:core:designsystem`, the screens in `:feature:shopping`. The Firestore rules for `history` are published by hand again. |
| 15 | Design as the rest of the app: `motionScheme`, touch feedback, light and dark, and screenshots (light, dark, large text) for each new component. |

PRs of the redesign, one at a time: 1 domain (catalog, history, Firestore repository, rules, fakes and tests); 2 the grid of the tab (tiles with icon, collapsible categories, buy with a tap, edit with a long press, "Bought recently"); 3 the big add sheet with search, recent and catalog; 4 add from the week of the calendar.

## Phase 0 — Project base
- [x] `libs.versions.toml` with the stack (recent Compose BOM, M3 Expressive, Hilt+KSP, Navigation 3, Kotlinx Serialization, Coil 3, Firebase BOM, JUnit5/Turbine, fakes instead of mocks)
- [x] Modules: `:domain`, `:data`, `:ui`, `:ui:components` + `settings.gradle.kts` + a `build.gradle.kts` for each (reorganized into `:core:designsystem` and `:feature:*`)
- [x] Clean the Android Studio sample code (only `ExampleInstrumentedTest` is left, it can be deleted by hand)
- [x] `strings.xml` (es) and `values-en` with a prefix convention (checked by `StringResourcesConventionTest` in `:app`: same names and placeholders in both languages, snake_case, a declared prefix per module, no name in two modules)

- [x] Build: convention plugins (`build-logic`), `settings` plugin, typed accessors and Spotless/ktlint

## Phase 1 — Design system (`:core:designsystem`)
- [x] `Color.kt` with ALL the tokens in light and dark (see `CLAUDE.md`)
- [x] `Typography.kt`, `Shape.kt`, `Spacing.kt`
- [x] `TavolaTheme.kt` (no dynamicColor, no placeholders)
- [x] `TavolaScaffold` (with the insets strategy decided)
- [x] Floating navigation bar (overlapping the content)
- [x] `TavolaDayCard`, `TavolaMealSlot`, `TavolaTaskChip`, `TavolaTopBar`, `TavolaLoadingIndicator`: complete, with light/dark previews
- [x] `TavolaWeekSelector` (week selector) and `TavolaAddTaskDialog`
- [x] **Visual checkpoint:** compared with the mockup on the emulator (reference screenshots in `app/src/screenshotTestDefaultDebug/reference`)

## Phase 2 — Calendar with fake data
- [x] Models and repository in `:domain` (calendar: `WeekPlan`, `DayPlan`, `CalendarRepository`; recipes, groups and shopping are missing)
- [x] `CalendarUiState`, `CalendarViewModel`, `CalendarScreen` (`:feature:calendar`, the landing of the app with Navigation 3)
- [x] Meal picker: recipe or quick meal (`PlannedMeal`), bottom sheet with search, change and remove (`:feature:recipes`)
- [x] Mediterranean fake data (`MediterraneanSampleData` and `FakeCalendarRepository` in `:core:testing`)
- [x] ViewModel tests (8 for the ViewModel, 5 for date formatting, 6 for the in-memory repository)
- [x] **Checkpoint:** build on the emulator, light and dark

## Phase 3 — Real MVP (product phase 1)
The items are grouped by topic, not in work order: follow "Next up" above.

- [ ] Firebase + Auth + groups with an invite code (see "Firebase decisions" above)
  - [x] Firebase and Auth with Google sign-in: `:feature:auth` has the sign-in screen and the session gate (`AuthGate`); `AuthRepository` in `:domain`, Firebase Auth in `:data`. The CI APK was installed on another device and signs in fine.
  - [x] Groups with an invite code: 2a data and rules (done), 2b the gate and the first step, create or join (done), 2c the "My group" screen with the button in the calendar bar: code, copy, share, renew, rename, members, leave (done)
- [x] Shared calendar in real time (Firestore): the recipes, the calendar and the group are in Firestore, and every change shows up on the other phones through the listeners
  - [x] The calendar in Firestore, Room out: `groups/{groupId}/meals` and `groups/{groupId}/tasks`, ordered by creation time; Room, its schema and its dependencies are gone from the project (PR 4)
  - [x] The recipes in Firestore (PR 3)
  - [x] A check of the whole flow between two accounts: done with the CI APK on a second device (groups, calendar and meals sync)
  - [ ] Emulator tests of the rules (needs Node)
- [x] Recipe CRUD: list, detail and editor (the model already has structured ingredients and steps)
  - [x] Persistence: Room for recipes and the calendar (`RoomRecipeRepository`, `RoomCalendarRepository`); no fake data in the app, sample data only through the debug menu ("Load sample data")
  - [x] List tab + detail (read-only), search and empty state
  - [x] Editor: title, prep time, free-text tags with suggestions, ingredients (keeping "to taste"), reorderable steps
  - [x] Delete with Undo: drag a row halfway across the list, with a countdown snackbar (same pattern as task chips)
- [x] Tasks per day
  - [x] Persistence in Room, add, tick and remove with Undo (5 second snackbar)
  - [x] "Add task" chip at the end of the task list of today and the days to come (no "+" in the header)
  - [x] Past days are read-only: tasks can only be ticked, and their recipe meals open the recipe detail
  - [x] Edit the text of a task: long press on its chip opens the task dialog with the current text (today and the days to come)
  - Not done, to decide if it is worth it: move a pending task to another day, repeat tasks, reorder within a day
- [x] **Calendar: past days collapsed.** Every day before today, in the current week and in earlier ones, starts collapsed and can be expanded.
  - Collapsed: one compact, flat row of about 68 dp (smaller corners and padding than the full card, no shadow) with the date, the weekday, the month and the lunch and the dinner on two short lines with the name of the recipe or the quick meal. No tasks, no empty-slot buttons. The date and the weekday follow the app locale through the existing helpers in `CalendarFormatting.kt` (for example "27 oct" and "martes" in Spanish). With large text (above 1.3x) the two meals go under the date, where they stay readable. A whole past week fits in one screen.
  - Today and the days to come keep the full card, as now.
  - Expand and collapse with a tap on the header, animated with the motion scheme: the row turns into the full card (the corners, the padding and the date number grow with the spatial spring), the meals and tasks open and fade in, the add button pops in and the chevron turns.
  - The expanded state lives in the screen state (`CalendarUiState`), survives scrolling and rotation, and starts collapsed again when the week changes.
  - "Today" comes from the existing `TodayProvider`, so the ViewModel and the screenshots can fix it.
  - Accessibility: the header of the card is the expandable control (role, expanded / collapsed state announced), with a touch target of at least 48 dp; once expanded, the meal slots and the tasks keep their own controls.
  - Done when: ViewModel tests (which days are past, toggling, reset on week change), screenshots of the collapsed and the expanded card in light, dark and large text, and the calendar screenshots updated.
  - Decided: an empty slot shows a dash; a collapsed day shows a small counter with the tasks left (read as "2 tareas pendientes" by screen readers); an expanded past day can still be edited, as today; every day opens on its own (no "earlier days" group).
- [ ] **Interactive widget** (to be defined; Jetpack Glance is the starting point).
  - Idea: a home-screen widget for today that is not only a viewer: the user can act on it without opening the app.
  - To decide:
    - What it shows: today's lunch and dinner, today's tasks, or the next meal.
    - Which interactions: tick a task off, remove it, jump to the recipe, open the picker of an empty slot, add a quick task.
    - Sizes: small (next meal), medium (today), large (today with tasks), and whether it is resizable.
    - Look: the same palette in light and dark (no dynamic color), rounded shapes, the app typography where Glance allows it.
    - Data: reads and writes through the repositories (Room now, Firestore later), so it follows the move to the shared calendar; how it refreshes when the app changes something and at midnight.
    - Tapping on a recipe or an empty slot: which screen it opens, through `:app`, which is the only module that knows the routes.
    - A new module (for example `:feature:widget`) or inside `:app`, and how it is tested (Glance has its own testing support; screenshots if they are possible).
  - Depends on: tasks per day (done) and, to be built, on Firebase (see "Firebase decisions"). Define it with the user before building.

## Phase 4 — Shopping list and premium design
- [x] Manual shopping list by category (release 0.1.1: tab, add sheet with a guessed category, tick, swipe to remove with Undo, "Remove ticked", add the ingredients of a recipe)
- [ ] Shopping list redesign, Bring-style (decisions above), in 4 PRs:
  - [x] 1. Domain: catalog, history, Firestore repository and rules, fakes and tests (`checked` and `setChecked` stay until PR 2 replaces the screen; the `history` rules must be published by hand)
  - [ ] 2. The grid of the tab: tiles with icon, collapsible categories, buy with a tap, edit with a long press, "Bought recently"
  - [ ] 3. The big add sheet: search, recent and catalog
  - [ ] 4. Add the ingredients of a week from the calendar
- [ ] Recipe photos, recipe book carousel
- [ ] Import a recipe by URL
- [ ] Push notifications
- [ ] Onboarding / feature discovery: tell the user about the features that are not visible on screen. To decide the form (first-run tour, coach marks, tips) once there are more features. Hidden gestures so far:
  - long press on a task chip edits its text
  - drag a recipe row halfway to delete it (with Undo)
  - tap the header of a past day to open or close it
  - on the shopping list, a long press on a tile edits it (a tap means "bought")
  - tap a recipe meal of a past day to see the recipe

## Phase 5 — AI (at the end)
- [ ] Suggestions by ingredients, weekly planner, suggestions in the shopping list

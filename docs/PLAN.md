# Tavola plan (restart)

Tick `[x]` when done. One task at a time; build and verify before the next one.

## Next up (agreed order)
1. ~~Clear the small pending checks~~ Done: the recipe CRUD is merged (PR #5), the `strings.xml` prefixes are in and both emulator checkpoints are passed.
2. ~~Past days collapsed~~ Done (PR #8).
3. ~~Tasks per day~~ Done: add, tick, remove with Undo and edit the text were already in; this closed it with the "Add task" chip, read-only past days and editing by long press (PRs #10 and #11).
4. ~~Close 0.0.3 and open 0.1.0~~ Done: `release/0.0.3` is in `main` (PR #13, pre-release `v0.0.3`) and `release/0.1.0` is open for Firebase.
5. **Firebase + Auth + groups, then the shared calendar in real time** (decisions below): the biggest change to the data layer, in 4 or 5 small PRs. It goes before the widget because the widget reads through the repositories and would have to be reworked after the move.
6. **Interactive widget** (Phase 3): it can be defined on paper any time (open questions below), but it is built after Firebase.
7. Shopping list, recipe photos and carousel, import by URL, push notifications (Phase 4).
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

Planned PRs, each one building, tested and reviewed on its own:
1. Firebase and Auth with Google sign-in.
2. Groups with an invite code, in two PRs: 2a the group model, the Firestore repository and `firestore.rules` (the group, the members and the invites); 2b the screens (create or join, "My group") and the session gate with a group.
3. The calendar in Firestore behind the same `CalendarRepository` (Room out).
4. The recipes in Firestore behind `RecipeRepository`. Once both are in, remove the Room code and dependency, and update `CLAUDE.md` (`:data` is described as Firestore/Room) and the README, which still mention Room.
5. The security rules for the calendar and the recipes, and real-time updates between members.

Still to decide, each before the PR that needs it (these came out of the review of the decisions above):
- ~~Joining by code with member-only rules~~ Decided in PR 2: an `invites/{code}` document read by its exact id (see "Groups decisions").
- **Debug menu on shared data** (PR 3). Decision 3 keeps "Load sample data" and "Clear everything", but there is one Firebase project, so a debug build in the real group could wipe or fill the real calendar. Proposal, to confirm: the debug menu only works on a separate test group, or "Clear everything" is limited to what the loader added.
- **First launch** (decided in PR 1, to confirm): the app opens on the sign-in screen when nobody is signed in; a stored session is kept offline. Cancelling the Google sheet shows nothing; no connection, no Google account on the phone and any other failure show their own message with the button still there. There is no way to use the app without an account.
- ~~Leaving a group~~ Decided in PR 2: nothing is deleted when the last member leaves; the code can be renewed (see "Groups decisions").

To check before or while doing it:
- **Firestore rules are published by hand and not tested with the emulator.** `firestore.rules` (PR 2a) is pasted in the Firebase console (Firestore Database, Rules, Publish) or deployed with `firebase deploy --only firestore:rules`; the repository only works against the rules that are live. There is no Node here, so the rules were not run in the Firebase emulator; they are checked by the console when published and by trying the flows in the app (PR 2b). Add emulator tests (`@firebase/rules-unit-testing`) when Node is available, before the rules grow with the calendar and the recipes.
- **Recipe photos and the Spark plan.** Firebase Storage may need the paid Blaze plan, even on a new project. Verify before planning the photos (Phase 4); it may change decision 7 or postpone them.
- **Google sign-in in the CI APK.** It needs the SHA-1 of the signing key registered in Firebase, and CI signs the debug APK with its own key, so that APK may not be able to sign in. Decide how to sign it (a fixed key) before publishing.
- **`google-services.json` in CI.** Decided: it is committed (it holds no secrets), in PR #14. Follow-up for the owner of the project: restrict its API key in Google Cloud (APIs and services, Credentials) to the package `com.tavola` and the SHA-1 of the debug and CI keys, because the key identifies the project and is only safe to commit if it is restricted.
- **The widget.** It must learn the group and the signed-in state, and decide how it refreshes (listener or periodic work) and what it shows with no session or no network.

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
  - [x] Firebase and Auth with Google sign-in: `:feature:auth` has the sign-in screen and the session gate (`AuthGate`); `AuthRepository` in `:domain`, Firebase Auth in `:data`. Still to do: register the SHA-1 of the CI signing key in Firebase so the CI APK can sign in.
  - [ ] Groups with an invite code (2a data and rules, 2b screens)
- [ ] Shared calendar in real time (Firestore)
  - [ ] The calendar in Firestore, Room out
  - [ ] The recipes in Firestore
  - [ ] Security rules and real-time updates between members
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
- [ ] Manual shopping list by category
- [ ] Recipe photos, recipe book carousel
- [ ] Import a recipe by URL
- [ ] Push notifications
- [ ] Onboarding / feature discovery: tell the user about the features that are not visible on screen. To decide the form (first-run tour, coach marks, tips) once there are more features. Hidden gestures so far:
  - long press on a task chip edits its text
  - drag a recipe row halfway to delete it (with Undo)
  - tap the header of a past day to open or close it
  - tap a recipe meal of a past day to see the recipe

## Phase 5 — AI (at the end)
- [ ] Suggestions by ingredients, weekly planner, suggestions in the shopping list

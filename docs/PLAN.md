# Tavola plan (restart)

Tick `[x]` when done. One task at a time; build and verify before the next one.

## Next up (agreed order)
1. Close the recipe CRUD (PR #5) and clear the small pending checks: Phase 0 `strings.xml` prefixes and the two emulator checkpoints (Phase 1 visual, Phase 2 light and dark).
2. **Past days collapsed** (Phase 3): self-contained in the calendar, no backend, and the most visible change for the daily use of the app.
3. **Tasks per day** (Phase 3): completes the local features, and the widget needs it.
4. **Interactive widget** (Phase 3): define it first (open questions below), then build it on the same repositories as the app.
5. Firebase + Auth + groups, then the shared calendar in real time: the biggest change to the data layer, so it goes after the local features are settled. The widget reads through the repositories, so it follows the move.
6. Shopping list, recipe photos and carousel, import by URL, push notifications (Phase 4).
7. AI (Phase 5), last.

## Phase 0 — Project base
- [x] `libs.versions.toml` with the stack (recent Compose BOM, M3 Expressive, Hilt+KSP, Navigation 3, Kotlinx Serialization, Coil 3, Firebase BOM, JUnit5/Turbine, fakes instead of mocks)
- [x] Modules: `:domain`, `:data`, `:ui`, `:ui:components` + `settings.gradle.kts` + a `build.gradle.kts` for each (reorganized into `:core:designsystem` and `:feature:*`)
- [x] Clean the Android Studio sample code (only `ExampleInstrumentedTest` is left, it can be deleted by hand)
- [ ] `strings.xml` (es) and `values-en` with a prefix convention

- [x] Build: convention plugins (`build-logic`), `settings` plugin, typed accessors and Spotless/ktlint

## Phase 1 — Design system (`:core:designsystem`)
- [x] `Color.kt` with ALL the tokens in light and dark (see `CLAUDE.md`)
- [x] `Typography.kt`, `Shape.kt`, `Spacing.kt`
- [x] `TavolaTheme.kt` (no dynamicColor, no placeholders)
- [x] `TavolaScaffold` (with the insets strategy decided)
- [x] Floating navigation bar (overlapping the content)
- [x] `TavolaDayCard`, `TavolaMealSlot`, `TavolaTaskChip`, `TavolaTopBar`, `TavolaLoadingIndicator`: complete, with light/dark previews
- [x] `TavolaWeekSelector` (week selector) and `TavolaAddTaskDialog`
- [ ] **Visual checkpoint:** compare with the mockup before going on (there are reference screenshots in `app/src/screenshotTestDefaultDebug/reference`; they still have to be reviewed together on the emulator)

## Phase 2 — Calendar with fake data
- [x] Models and repository in `:domain` (calendar: `WeekPlan`, `DayPlan`, `CalendarRepository`; recipes, groups and shopping are missing)
- [x] `CalendarUiState`, `CalendarViewModel`, `CalendarScreen` (`:feature:calendar`, the landing of the app with Navigation 3)
- [x] Meal picker: recipe or quick meal (`PlannedMeal`), bottom sheet with search, change and remove (`:feature:recipes`)
- [x] Mediterranean fake data (`MediterraneanSampleData` and `FakeCalendarRepository` in `:core:testing`)
- [x] ViewModel tests (8 for the ViewModel, 5 for date formatting, 6 for the in-memory repository)
- [ ] **Checkpoint:** build on the emulator, light and dark

## Phase 3 — Real MVP (product phase 1)
The items are grouped by topic, not in work order: follow "Next up" above.

- [ ] Firebase + Auth + groups with an invite code
- [ ] Shared calendar in real time (Firestore)
- [ ] Recipe CRUD: list, detail and editor (the model already has structured ingredients and steps)
  - [x] Persistence: Room for recipes and the calendar (`RoomRecipeRepository`, `RoomCalendarRepository`); no fake data in the app, sample data only through the debug menu ("Load sample data")
  - [x] List tab + detail (read-only), search and empty state
  - [ ] Editor: title, prep time, free-text tags with suggestions, ingredients (keeping "to taste"), reorderable steps
  - [ ] Delete with Undo (same pattern as task chips)
- [ ] Tasks per day
- [ ] **Calendar: past days collapsed.** Every day before today, in the current week and in earlier ones, starts collapsed and can be expanded.
  - Collapsed: only the date, the weekday and the lunch and the dinner with the name of the recipe or the quick meal. No tasks, no empty-slot buttons. The date and the weekday follow the app locale through the existing helpers in `CalendarFormatting.kt` (for example "27 oct" and "martes" in Spanish).
  - Today and the days to come keep the full card, as now.
  - Expand and collapse with a tap on the card, animated with the motion scheme (spatial spring for the size and the shape, effects spec for the opacity; the slots and tasks come in one after the other, the chevron turns). Respect reduced motion.
  - The expanded state lives in the screen state (`CalendarUiState`), survives scrolling and rotation, and starts collapsed again when the week changes.
  - "Today" comes from the existing `TodayProvider`, so the ViewModel and the screenshots can fix it.
  - Accessibility: the header of the card is the expandable control (role, expanded / collapsed state announced), with a touch target of at least 48 dp; once expanded, the meal slots and the tasks keep their own controls.
  - Done when: ViewModel tests (which days are past, toggling, reset on week change), screenshots of the collapsed and the expanded card in light, dark and large text, and the calendar screenshots updated.
  - Open questions: what to show when a past day has no meal in a slot (a dash?), whether unfinished tasks of a past day deserve a small indicator, and whether a past day can still be edited once expanded (the proposal is yes, as today).
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
  - Depends on: tasks per day. Define it with the user before building.

## Phase 4 — Shopping list and premium design
- [ ] Manual shopping list by category
- [ ] Recipe photos, recipe book carousel
- [ ] Import a recipe by URL
- [ ] Push notifications

## Phase 5 — AI (at the end)
- [ ] Suggestions by ingredients, weekly planner, suggestions in the shopping list

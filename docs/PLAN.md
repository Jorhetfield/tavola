# Plan de Tavola (reinicio)

Marca `[x]` al terminar. Una tarea por vez; compilar y verificar antes de la siguiente.

## Fase 0 — Base del proyecto
- [x] `libs.versions.toml` con el stack (Compose BOM reciente, M3 Expressive, Hilt+KSP, Navigation 3, Kotlinx Serialization, Coil 3, Firebase BOM, JUnit5/Turbine, fakes sin mocks)
- [x] Módulos: `:domain`, `:data`, `:ui`, `:ui:components` + `settings.gradle.kts` + `build.gradle.kts` de cada uno (reorganizados a `:core:designsystem` y `:feature:*`)
- [x] Limpiar el código de ejemplo de Android Studio (queda solo `ExampleInstrumentedTest`, se puede borrar a mano)
- [ ] `strings.xml` (es) y `values-en` con convención de prefijos

- [x] Build: plugins de convención (`build-logic`), plugin `settings`, accesores tipados y Spotless/ktlint

## Fase 1 — Design system (`:core:designsystem`)
- [x] `Color.kt` con TODOS los tokens en light y dark (ver `CLAUDE.md`)
- [x] `Typography.kt`, `Shape.kt`, `Spacing.kt`
- [x] `TavolaTheme.kt` (sin dynamicColor, sin placeholders)
- [x] `TavolaScaffold` (con estrategia de insets decidida)
- [x] Barra flotante de navegación (superpuesta en el content)
- [x] `TavolaDayCard`, `TavolaMealSlot`, `TavolaTaskChip`, `TavolaTopBar`, `TavolaLoadingIndicator` — completos y con preview light/dark
- [x] `TavolaWeekSelector` (selector de semana) y `TavolaAddTaskDialog`
- [ ] **Checkpoint visual:** comparar con el mockup antes de seguir (hay 18 capturas de referencia en `app/src/screenshotTestDefaultDebug/reference`; falta revisarlas contigo en el emulador)

## Fase 2 — Calendario con datos fake
- [x] Modelos y repositorio en `:domain` (calendario: `WeekPlan`, `DayPlan`, `CalendarRepository`; faltan recetas, grupos y compra)
- [x] `CalendarUiState`, `CalendarViewModel`, `CalendarScreen` (`:feature:calendar`, landing de la app con Navigation 3)
- [x] Selector de comida: receta o comida rápida (`PlannedMeal`), hoja inferior con buscador, cambiar y quitar (`:feature:recipes`)
- [x] Datos fake mediterráneos (`MediterraneanSampleData` y `FakeCalendarRepository` en `:core:testing`)
- [x] Tests de ViewModel (8 del ViewModel, 5 de formato de fechas, 6 del repositorio en memoria)
- [ ] **Checkpoint:** build en emulador, light y dark

## Fase 3 — MVP real (Fase 1 del producto)
- [ ] Firebase + Auth + grupos con código de invitación
- [ ] Calendario compartido en tiempo real (Firestore)
- [ ] CRUD de recetas: lista, detalle y editor (el modelo ya tiene ingredientes estructurados y pasos)
  - [x] Persistence: Room for recipes and the calendar (`RoomRecipeRepository`, `RoomCalendarRepository`); no fake data in the app, sample data only through the debug menu ("Load sample data")
  - [ ] List tab + detail (read-only), search and empty state
  - [ ] Editor: title, prep time, free-text tags with suggestions, ingredients (keeping "to taste"), reorderable steps
  - [ ] Delete with Undo (same pattern as task chips)
- [ ] Tareas por día

## Fase 4 — Lista de la compra y diseño premium
- [ ] Lista de la compra manual por categorías
- [ ] Fotos en recetas, carousel de recetario
- [ ] Importar receta por URL
- [ ] Notificaciones push

## Fase 5 — IA (al final)
- [ ] Sugerencias por ingredientes, planificador semanal, sugerencias en la compra

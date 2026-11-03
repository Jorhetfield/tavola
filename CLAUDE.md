# Tavola — contexto para Claude Code

Planificador semanal de comidas compartido (pareja, familia, compañeros de piso). Android nativo, Kotlin + Compose.
Cada día: comida y cena, tareas rápidas ("sacar a descongelar", "comprar pimientos"), recetas propias como tarjetas (ingredientes + pasos) y lista de la compra manual.

> Estado: **reinicio desde cero** (la primera versión se abandonó). Lee `docs/PLAN.md` para saber en qué fase estamos y `docs/LESSONS.md` antes de tocar UI.

## Cómo trabajar (ahorrar tokens)
- Una tarea pequeña por vez. No regeneres archivos enteros si solo cambia una cosa.
- No leas el repo entero: pide solo los archivos que necesites.
- Compila y verifica antes de pasar a la siguiente tarea. Marca el checklist de `docs/PLAN.md`.
- Si algo es ambiguo, pregunta antes de asumir.

## Normas de código y de git (OBLIGATORIAS)
Estas normas no se negocian y no tienen excepciones salvo que el usuario diga lo contrario. Aplican a todo código y a todo trabajo nuevo.

### Kotlin: nada de `it`
Prohibido el nombre implícito `it` en lambdas. Nombra siempre el parámetro con algo legible que diga qué es, también en lambdas de un solo parámetro y en las anidadas.
```kotlin
// MAL
recipes.filter { it.matches(query) }.map { it.title }
// BIEN
recipes.filter { recipe -> recipe.matches(query) }.map { recipe -> recipe.title }
```
Con pares o destructuring, nombra cada parte (`{ (date, type) -> ... }`). El código antiguo que lo incumple se corrige al tocarlo; el nuevo nunca lo incumple.

### Kotlin: imports arriba, nunca nombres calificados en el código
Prohibido escribir nombres completos de clase en mitad del código (`androidx.compose.ui.graphics.Color`, `java.time.LocalDate.now()`, `com.tavola.domain.model.DayTask`...). Si necesitas una clase de otro sitio, pon su `import` en la zona de imports y usa el nombre corto. Si dos clases chocan de nombre, usa un alias **en el import** (`import com.tavola.domain.model.MealType as DomainMealType`), nunca el nombre completo en el cuerpo.
```kotlin
// MAL
val effects = MaterialTheme.motionScheme.defaultEffectsSpec<androidx.compose.ui.graphics.Color>()
// BIEN (con `import androidx.compose.ui.graphics.Color` arriba)
val effects = MaterialTheme.motionScheme.defaultEffectsSpec<Color>()
```

### Idioma del repositorio: inglés
Todo lo que se escriba en el repo va en **inglés**: plantillas, documentación nueva, comentarios y KDoc, mensajes de commit, títulos y descripciones de PR. Excepciones: los textos de la app para el usuario, que siguen en `values/` (es) y `values-en/` (en), y la conversación con Claude, que puede ser en español. Lo existente en español se traduce al tocarlo.

### Git flow: release train
Branches:
- `main` only receives a finished `release/x.y.z` and is what publishes an APK. Never commit or push to it directly.
- `release/x.y.z` is the **integration branch**. Exactly one is open at a time (the latest one). It is cut from an up-to-date `main`, and its first commit sets the `VERSION` file to `x.y.z`.
- `feature/<short-kebab-name>` (also `fix/`, `docs/`, `build/`) branches start **from the latest open release** and go back **into it** through a pull request (squash merge).

```bash
git switch release/0.0.1        # the latest open release
git pull
git switch -c feature/what-we-are-building
```

Rules:
- Check the branch before editing (`git status -sb`). On `main` or `release/**`, create the feature branch first. Neither accepts direct commits.
- PRs always target the latest release (`gh pr create --base release/x.y.z`), never `main`, except hotfixes.
- Small commits with a clear message. Before proposing a PR: `spotlessCheck`, build and tests green.
- **Never push, open a PR or merge without the user's explicit confirmation**: publishing is hard to undo. If the user did not say the branch type, ask; until then use `feature/`.
- **After a successful merge the merged branch is deleted** (remote and local): `gh pr merge <n> --squash --delete-branch`, then `git switch <release> && git pull`.
- Closing a release: open a PR `release/x.y.z` -> `main` and merge it with a **merge commit** (not squash, so the branches do not diverge). CI then publishes the APK as the pre-release `vX.Y.Z`. Afterwards delete the release branch (the tag stays) and cut the next `release/x.y.z+1` from `main`. Merge to `main` on milestones (a block of `docs/PLAN.md`, or when we want an APK), not on a timer.
- Hotfix: branch from `main`, PR into `main` with a bumped `VERSION`, then bring the fix into the open release.
- Repo protection is not available (private repo, free plan), so these rules are convention only.

## Comandos
Requieren `JAVA_HOME` apuntando al JDK de Android Studio (`%LOCALAPPDATA%\Programs\Android Studio\jbr`).
```bash
./gradlew assembleDebug                      # compilar
./gradlew testDebugUnitTest :domain:test :core:testing:test   # tests unitarios (JUnit 5)
./gradlew lintDebug                          # lint de Android
./gradlew spotlessApply                      # formatear (ktlint). spotlessCheck solo verifica
```
Antes de dar una tarea por terminada: `spotlessApply` + `assembleDebug` + tests.

### CI
Every pull request into `main` or `release/**`, and every push to them, runs `.github/workflows/ci.yml` (spotlessCheck, build, lint and tests). Only a push to `main` (a release merge) also builds the signed debug APK and publishes the pre-release `vX.Y.Z`, with the version read from the `VERSION` file; it fails if that version is already published. Before pushing, run locally the same as CI. Screenshot tests are not in CI.

### Tests de capturas (Compose Preview Screenshot Testing, experimental)
```bash
./gradlew :app:updateDebugScreenshotTestDefaultTestSuite   # genera/actualiza las imágenes de referencia
./gradlew :app:testDebugScreenshotTestDefaultTestSuite     # compara contra las referencias
```
- Viven en `app/src/screenshotTest/kotlin` (no en `:core:designsystem`: en una librería el motor falla al resolver las clases `R`). Referencias en `app/src/screenshotTestDefaultDebug/reference`.
- Un componente nuevo del design system = una función `@PreviewTest` + `@TavolaPreviews` (light/dark) allí. Pantallas completas: `@TavolaAdaptivePreviews` (móvil light/dark, plegable, tablet). Accesibilidad: `@TavolaFontScalePreviews`.
- Los nombres de `@Preview(name = ...)` **no pueden llevar punto** (rompe el nombre del archivo de referencia).
- Si un cambio de diseño es intencionado, regenera las referencias y revisa las imágenes. Si cambias el nombre o los parámetros de una preview, borra las referencias antiguas a mano.
- Las referencias se generan en Windows; renderizar en otro SO (CI en Linux) puede dar diferencias de fuente.

## Build
- **Plugins de convención** en `build-logic/`: `tavola.android.application`, `tavola.android.library`, `tavola.android.compose.library`, `tavola.kotlin.jvm`. Un módulo nuevo aplica uno de ellos y no repite `compileOptions`, Compose ni `buildFeatures`.
- **SDKs** (`compileSdk`, `targetSdk`, `minSdk`) se definen una sola vez en `settings.gradle.kts` (plugin `com.android.settings`). Java 17 en `javaVersion` de `libs.versions.toml`.
- **Dependencias entre módulos** con accesores tipados: `projects.domain`, `projects.ui.components`.
- **Formato:** ktlint vía Spotless, reglas en `.editorconfig` (estilo android_studio, líneas de 140, comas finales).

## Identidad visual
Cocina mediterránea (España, Italia, Grecia): cálida, orgánica, muy redondeada. Referentes: Airbnb (fotografía, elegancia), Duolingo (playful), Linear (premium). Light + dark desde el inicio. Iconos/ilustraciones con personalidad propia.

### Paleta (tokens en `Color.kt`, nunca hex en pantallas)
| Nombre | Hex |
|---|---|
| Azafrán (primary) | `#D4813A` |
| Limón | `#F0B840` |
| Trigo | `#FDF4E0` |
| Oliva oscuro (texto light) | `#3D2200` |
| Egeo (secondary) | `#2980B9` |
| Mediterráneo | `#A8D8EA` |
| Albahaca (tertiary) | `#5D8A3C` |
| Oliva claro | `#C8E6A0` |
| Terracota | `#B85C38` |
| Arcilla | `#E8A87C` |
| Noche Egeo | `#1A3A5C` (fondo dark profundo `#0F1A24`) |
| Cal griega (bg light) | `#F7F0E6` |

### Reglas de color (aprendidas)
- **Dark mode** funciona por contraste de tres tonos: azul noche (fondo/cards), verde oliva (slots comida/cena), terracota/naranja (chips y acentos). Es la referencia de "personalidad".
- **Light mode** debe replicar ese contraste en clave diurna, NO ser unitono crema: fondo crema `#F7F0E6`, cards de día azul mediterráneo suave `#E8F0F8`, slots verde oliva suave `#D0E4D0`, chips azafrán/primaryContainer, día activo azafrán fuerte, texto azul noche `#1A3A5C`.
- Todos los tokens `surfaceContainer*`, `surfaceVariant`, `outline*` y `*Container` deben definirse explícitamente en ambos `ColorScheme`; si no, M3 pone grises/azules fríos.
- `dynamicColor` desactivado siempre.

### Formas (muy redondeado)
extraSmall 8dp · small 12dp · medium 16dp · large 20dp · extraLarge 28dp · botones/nav pill.

## Arquitectura
Clean Architecture multi-módulo por capas:
```
:app                   entry point, DI raíz, navegación
:domain                Kotlin puro (sin android.*): modelos, UseCases, interfaces de repositorio
:data                  Firestore/Room, implementaciones de repositorio
:core:designsystem     tema, tokens, TavolaScaffold, componentes base (paquete com.tavola.core.designsystem)
:feature:<nombre>      una funcionalidad: pantallas + ViewModels (paquete com.tavola.feature.<nombre>)
                       hoy: calendar (landing), recipes (selector de comida) y showcase (solo debug)
:core:testing          fakes (FakeCalendarRepository), datos de ejemplo mediterráneos y MainDispatcherExtension (JUnit 5)
```
Dependencias: `:domain` no depende de nadie; `:data` depende de `:domain`; `:core:*` nunca depende de `:feature:*`; `:core:designsystem` solo de Compose/M3 (ni `:domain`); cada `:feature:*` depende de `:domain` y `:core:designsystem`, **nunca de otra feature**. Para que una feature use a otra, `:app` le cede el contenido por un *slot* (p. ej. la hoja del selector en el calendario) y la comunicación va por IDs y repositorios del dominio; `:app` ensambla todo y es el único que conoce las rutas de navegación.
Hoy existen `:feature:auth` (login con Google y `AuthGate`, que decide entre la pantalla de acceso y la app), `:feature:calendar` (landing: calendario semanal con datos fake) y `:feature:showcase` (pantalla temporal para ver el tema y los componentes; `:app` la incluye solo en debug y se abre desde el botón de depuración del calendario, ver `src/debug/.../DebugNavigation.kt`). Una funcionalidad nueva = un módulo `:feature:<nombre>` nuevo.

**UI:** MVVM clásico. `ViewModel` + `StateFlow<UiState>` con `UiState` como `sealed class` (Loading / Success / Error).
Estructura por feature: `XScreen.kt` (solo composables) · `XViewModel.kt` · `XUiState.kt` · `components/`.
Convenciones de estado (guía de arquitectura de Google):
- Las pantallas leen el estado con `collectAsStateWithLifecycle()`.
- Si el estado deriva de un Flow del repositorio, el ViewModel lo expone con `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)`.
- Los ViewModels son solo de pantalla (nunca dentro de componentes reutilizables) y no reciben `Context`/`Activity`. Los eventos del ViewModel a la UI se expresan como estado, no como canales de eventos.
- `UiState` es `sealed class` solo si los estados son excluyentes; si hay campos combinados (p. ej. datos + refrescando), usa `data class`.
- **UseCases:** solo cuando combinan varios repositorios o comparten lógica entre ViewModels. Si solo reenvían una llamada, el ViewModel usa el repositorio directamente.

**Stack:** Compose + Material 3 Expressive (alpha) · Hilt + KSP · Navigation 3 · Kotlinx Serialization · Coil 3 · Firebase (Auth, Firestore, Storage) · version catalog `libs.versions.toml`.
**Tests desde el inicio:** JUnit5, Turbine, Compose Testing. **Fakes en lugar de mocks** (sin MockK): los fakes viven en `:core:testing`.
**IA (Claude API):** fase 3, aparcada. No implementar hasta completar la fase 2.

## Reglas de oro
1. Nunca colores ni dimensiones hardcodeados: tokens de `TavolaTheme` / `TavolaSpacing`.
2. Nunca strings literales en Compose: `stringResource(R.string.x)`. Strings en `values/` (es) y `values-en/` (en), prefijo por pantalla (`calendar_add_meal`).
3. Sin lógica de negocio en composables.
4. Nunca `Scaffold` directo: siempre `TavolaScaffold`.
5. Cada composable con `@Preview` light y dark.
6. SRP: composable > ~80 líneas → extraer subcomponentes. Un archivo, una responsabilidad.
7. `:domain` sin imports de Android, Compose ni Firebase.
8. Todo UseCase, ViewModel y repositorio con tests.
9. Nunca `Dispatchers.IO/Default/Main` directos en código de producción: inyecta `@IoDispatcher`, `@DefaultDispatcher` o `@MainDispatcher` (`com.tavola.domain.dispatcher`; los provee `DispatchersModule` en `:data`). En tests, pasa un `StandardTestDispatcher`/`UnconfinedTestDispatcher`.
10. Hilt: `TavolaApplication` (`@HiltAndroidApp`) y `MainActivity` (`@AndroidEntryPoint`) en `:app`. Los módulos de DI de `:data` son `internal` y usan `@Binds` (implementación `XRepositoryImpl` → interfaz `XRepository` de `:domain`). Los ViewModels llevan `@HiltViewModel`. `:domain` solo usa `javax.inject` (`@Inject constructor`), nunca Hilt.
11. Animaciones y movimiento: siempre con `MaterialTheme.motionScheme` (`defaultSpatialSpec`/`fastSpatialSpec` para tamaño, posición y forma; `defaultEffectsSpec`/`fastEffectsSpec` para color y opacidad), nunca `tween`/`spring` sueltos. Todo elemento pulsable tiene respuesta táctil. Solo tokens del tema (claro y oscuro), y cada pantalla o componente nuevo lleva capturas en claro, oscuro y con texto grande.
12. Nomenclatura: `XScreen`, `XViewModel`, `XUiState`, `GetXUseCase`, `XRepository` / `XRepositoryImpl`, componentes base `TavolaX`.

## Modelo de datos (Firestore)
```
Group        { id, name, memberIds[], inviteCode }
WeekPlan     { groupId, weekStart, days[{ date, lunch: PlannedMeal?, dinner: PlannedMeal?, tasks[] }] }
PlannedMeal  FromRecipe(RecipeRef: recipeId + título copiado) | Quick(title, note?)
Recipe       { id, ownerId, title, photoUrl, ingredients[{ name, quantity?, unit }], steps[], tags[], prepTimeMinutes }
ShoppingList { groupId, items[{ name, category, checked, addedBy }] }
GroupMember  { userId, displayName, avatarUrl, color }
```

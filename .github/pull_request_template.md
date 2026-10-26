## Qué cambia
<!-- Una o dos frases: qué hace esta PR y por qué. Enlaza el punto de docs/PLAN.md si aplica. -->

## Tipo de cambio
- [ ] Feature
- [ ] Mejora / refactor
- [ ] Arreglo
- [ ] Docs / build / CI

## Cómo se ha hecho
<!-- Decisiones relevantes, alternativas descartadas y módulos tocados. -->

## Capturas / vídeo
<!-- Obligatorio si hay cambios de UI: claro, oscuro y texto grande. Antes / después si es un cambio de diseño. -->

## Comprobaciones
- [ ] `./gradlew spotlessCheck`
- [ ] `./gradlew assembleDebug`
- [ ] `./gradlew testDebugUnitTest :domain:test :core:testing:test`
- [ ] `./gradlew lintDebug`
- [ ] Probado a mano en emulador o dispositivo (indica cuál)

## Normas del proyecto (CLAUDE.md)
- [ ] Sin `it` implícito en lambdas; sin nombres calificados en el cuerpo (imports arriba)
- [ ] Sin colores, dimensiones ni strings hardcodeados (tokens del tema y `stringResource`, es y en)
- [ ] Animaciones con `MaterialTheme.motionScheme` y respuesta táctil en lo pulsable
- [ ] Sin lógica de negocio en composables; ViewModel, UseCase o repositorio con tests
- [ ] Dependencias entre módulos respetadas (`:domain` puro, features sin depender entre sí)
- [ ] Capturas de referencia regeneradas y revisadas si cambia la UI (`updateDebugScreenshotTestDefaultTestSuite`)
- [ ] `docs/PLAN.md` actualizado si se completa un punto

## Notas para quien revise
<!-- Riesgos, partes dudosas, deuda que queda, trabajo pendiente en otras PRs. -->

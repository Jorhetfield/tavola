# Lecciones de la primera versión de Tavola

Léelo antes de tocar UI, tema o navegación.

## Qué pasó
La primera build se diseñó a ciegas, prompt a prompt, y se desvió mucho de los mockups (colores, tarjetas, navbar). Se perdió tiempo iterando sobre síntomas.

## Errores a no repetir
1. **Componentes stub sin contenido.** Se generaron `TavolaDayCard`, `TavolaMealSlot`… como `Box` vacíos y luego se iteró encima. Implementar cada componente completo, con su preview, antes de montarlo en una pantalla.
2. **Paleta a medias en `ColorScheme`.** Solo se definieron `primary/secondary/background/surface`; M3 rellenó `surfaceContainer*`, `surfaceVariant` y `outline*` con grises/azules fríos. Definir TODOS los tokens en light y dark desde el primer día.
3. **Light mode unitono.** El dark tenía contraste (azul noche + oliva + terracota) y el light era todo crema. Diseñar ambos modos a la vez con la misma lógica de contraste.
4. **Status bar / edge-to-edge.** El contenido pasaba bajo la barra de estado y el scaffold empujaba o recortaba contenido. Decidir una sola estrategia de insets (`enableEdgeToEdge()` + insets en el scaffold/topBar) y probarla en la primera pantalla.
5. **Navbar flotante dentro del `bottomBar` del Scaffold.** Reserva un hueco opaco debajo y el contenido no pasa por detrás. La barra flotante va superpuesta dentro del `content` (Box con `Alignment.BottomCenter`), con `contentPadding` inferior en la lista.
6. **`HorizontalFloatingToolbar` con `fillMaxWidth()`** crashea (`maxWidth must be >= than minWidth`). Calcula su propio ancho: ponerlo dentro de un Box centrado, sin `fillMaxWidth`.
7. **Uso del toolbar como navegación.** M3 Expressive no tiene barra de navegación flotante oficial; `HorizontalFloatingToolbar` está pensado para acciones contextuales. Usarlo como navegación es un apaño aceptado, pero hay que decidirlo conscientemente (alternativa: `FlexibleBottomAppBar` / `NavigationBar` expressive).
8. **Mockups HTML ≠ Compose.** Los mockups eran aproximaciones. Antes de construir, fijar con el usuario 1–2 pantallas de referencia y comparar captura vs. mockup tras cada paso.
9. **`TavolaTheme` con placeholders** (`Typography()` por defecto, shapes duplicadas privadas). Usar los archivos reales `Typography.kt` y `Shape.kt` desde el principio.

## Cómo comprobar bien una pantalla
- Captura del emulador en light y dark, comparada con la referencia.
- Revisar: status bar, scroll bajo la navbar, contraste de los tres tonos, radios de las cards.

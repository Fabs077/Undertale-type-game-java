# Plan de Interfaz — libGDX + Bit-Quest Monolith

## Contexto

El motor lógico (`com.rpg.engine`) está cerrado: 120/120 tests, combate completo, persistencia JSON, 5 fases procedurales. Ahora construimos la **capa visual** sobre él sin tocar el motor.

**Stack:** libGDX + Bit-Quest Monolith (el design system que generamos en Stitch).

**Diseños de referencia:** carpeta [stitch_undertale_battle_interface/](stitch_undertale_battle_interface/) — 4 pantallas mockeadas más el DESIGN.md del sistema visual.

**Principio:** cero imports de `com.badlogic.gdx.*` dentro de `com.rpg.engine.*`. La UI consume el motor por su API pública; la comunicación inversa va por callbacks/listeners.

---

## 1. Sistema de diseño — Bit-Quest Monolith

Toda la UI sigue las reglas del [DESIGN.md de Stitch](stitch_undertale_battle_interface/bit_quest_monolith/DESIGN.md). Resumen operativo para libGDX:

### Paleta (exactos del HTML de Stitch)
```java
public final class Colors {
    public static final Color BACKGROUND = Color.valueOf("000000");  // negro absoluto
    public static final Color SURFACE    = Color.valueOf("131313");  // casi negro
    public static final Color PRIMARY    = Color.valueOf("FF9D00");  // naranja acento
    public static final Color PRIMARY_LT = Color.valueOf("FFC485");  // naranja claro (text)
    public static final Color ON_SURFACE = Color.valueOf("E2E2E2");  // blanco texto
    public static final Color SECONDARY  = Color.valueOf("C6C6C7");  // gris secundario
    public static final Color WARNING    = Color.valueOf("FFFF00");  // amarillo (HP bar, warnings)
    public static final Color ERROR      = Color.valueOf("FFB4AB");  // rojo error
    public static final Color OUTLINE    = Color.valueOf("A28D79");  // bordes inactivos
}
```

### Tipografía
- **`Space Mono`** (headlines, body text) — para títulos y diálogos.
- **`JetBrains Mono`** (labels, UI metadata) — para etiquetas pequeñas y números.
- Ambas son monospace, ambas en Google Fonts (descarga gratuita).
- **Tamaños base:** headline-lg 32px, headline-md 24px, body-lg 18px, body-md 16px, label-lg 14px, label-sm 12px.

### Reglas duras
- **Bordes:** 2px (sub-menús) o 4px (paneles principales). **Naranja** = seleccionado/activo. **Blanco** = inactivo.
- **Esquinas:** SIEMPRE rectas (0px radius). Nunca redondeadas.
- **Sin sombras, sin gradientes, sin blurs.** Fondos siempre opacos.
- **Grid base:** 4px. Todo se alinea a múltiplos de 8px o 16px.
- **Pixel-perfect:** desactivar antialiasing en sprites (`Texture.setFilter(Nearest, Nearest)`).

### Skin de libGDX
Crear un `skin.json` (o usar Skin Composer) que materialice toda la paleta + fuentes en estilos reutilizables: `TextButton.PrimaryStyle`, `TextButton.SecondaryStyle`, `Label.HeadlineStyle`, `Label.BodyStyle`, `Window.PanelStyle`, etc. Así cada widget de Scene2D usa la skin automáticamente.

---

## 2. Pantallas — las que ya tenemos diseñadas

### A. `MainMenuScreen` — diseño en [rpg_retro_main_menu/](stitch_undertale_battle_interface/rpg_retro_main_menu/screen.png)
- Logo "ENTRANCE LOGO" (placeholder — sustituiremos por logo final).
- 3 botones grandes con icono pixel: **PLAY** (espada), **LOAD GAME** (diskette), **EXIT** (cruz).
- Borde naranja 4px en el botón seleccionado, blanco en los demás.
- Navegación con ↑/↓, Z para confirmar.

### B. `SaveSlotScreen` — diseño en [save_slot_selection_screen_1/](stitch_undertale_battle_interface/save_slot_selection_screen_1/screen.png)
- Header "SELECT SLOT" en naranja, separador horizontal 4px abajo.
- 3 paneles de slot, cada uno con:
  - **Izquierda:** "SLOT N" + "NOMBRE - LV N" (o "---EMPTY---")
  - **Derecha:** ubicación/fase + tiempo de juego (o "----------" + "--:--")
- Slot seleccionado tiene borde naranja 4px, los demás blanco.
- Footer con botón "← BACK".
- **HTML de referencia disponible** en [code.html](stitch_undertale_battle_interface/save_slot_selection_screen_1/code.html) — saca coordenadas y estructura exacta.

### C. `CombatScreen` ★ — diseños en [battle_interface_narrow_box/](stitch_undertale_battle_interface/battle_interface_narrow_box/screen.png) y [battle_interface_wide_box/](stitch_undertale_battle_interface/battle_interface_wide_box/screen.png)

Layout vertical (de arriba abajo):
1. **Zona del enemigo** — grid 6×2 con bordes verdes (placeholder para sprite del boss / patrón de bullet hell). Esta es la zona donde aparecen las balas.
2. **Caja de combate** — rectángulo blanco con borde grueso. **Dos tamaños:**
   - **Narrow** — cuando aparece el diálogo del boss (caja más alta con texto adentro).
   - **Wide** — durante el bullet hell (más ancha y baja, el alma se mueve dentro).
   - La caja se redimensiona dinámicamente según el `CombatState` activo.
3. **Barra de stats** — "LV 1" + barra HP amarilla + "20 / 20" en blanco. Centrado.
4. **Botones de acción** — 4 botones naranjas con icono pixel: ⚔ FIGHT, ☆ ACT, 🎒 ITEM, ✕ MERCY. Borde naranja 2px, texto naranja.

### D. `SaveExitDialog` — diseño en [save_slot_selection_screen_2/](stitch_undertale_battle_interface/save_slot_selection_screen_2/screen.png)
- Modal pequeño centrado: caja con texto *"Would you like to SAVE and EXIT or CONTINUE the battle?"*
- 2 botones grandes abajo: **💾 SAVE & EXIT** y **⚔ CONTINUE**.
- Aparece al presionar ESC durante combate (pausa).

### Pantallas que aún faltan diseñar en Stitch
- `DialogueScreen` — intro narrativa entre fases (texto largo de `StoryGenerator`).
- `GameOverScreen` — pantalla de muerte.
- `VictoryScreen` — pantalla final con la ruta narrativa.
- `InventorySubmenu` / `ActSubmenu` — submenús que aparecen dentro de `CombatScreen` (no son pantalla completa).

**Próximo paso de diseño:** generar estos 4 mockups en Stitch con el mismo design system (Bit-Quest Monolith) para tener consistencia.

---

## 3. Estructura de paquetes (nueva capa UI)

```
com.rpg
├── engine            ← motor (intocable)
└── ui                ← capa libGDX nueva
    ├── RpgGame.java               (extends com.badlogic.gdx.Game)
    ├── DesktopLauncher.java       (main() — configura ventana y arranca)
    │
    ├── theme
    │   ├── Colors.java            (paleta Bit-Quest Monolith)
    │   ├── Fonts.java             (Space Mono + JetBrains Mono cargadas)
    │   └── Skin.java              (genera/carga skin.json)
    │
    ├── screens
    │   ├── MainMenuScreen.java
    │   ├── SaveSlotScreen.java
    │   ├── DialogueScreen.java
    │   ├── CombatScreen.java      ★ principal
    │   ├── GameOverScreen.java
    │   └── VictoryScreen.java
    │
    ├── combat
    │   ├── CombatRenderer.java    (dibuja boss, alma, caja, UI)
    │   ├── CombatBox.java         (caja redimensionable narrow/wide)
    │   ├── Soul.java              (corazón rojo controlado por el jugador)
    │   ├── BulletPattern.java     (interfaz — Strategy real)
    │   ├── Bullet.java
    │   └── patterns
    │       ├── RadialSpreadPattern.java
    │       ├── BoneRainPattern.java
    │       └── ...
    │
    ├── menu
    │   ├── ActionMenu.java        (FIGHT/ACT/ITEM/MERCY)
    │   ├── ItemSubmenu.java       (lista del inventario)
    │   ├── ActSubmenu.java        (subopciones de ACT por boss)
    │   ├── DialogueBox.java       (texto del boss/narrador)
    │   └── SaveExitDialog.java    (modal SAVE/CONTINUE)
    │
    ├── input
    │   ├── PlayerInputAdapter.java
    │   └── InputContext.java      (qué teclas activas según CombatState)
    │
    ├── assets
    │   ├── AssetPaths.java        (constantes con rutas)
    │   └── GameAssets.java        (AssetManager wrapper)
    │
    └── bridge
        ├── CombatController.java  ★ puente UI ↔ engine.CombatManager
        ├── UiEventListener.java   (callbacks del motor a la UI)
        ├── SaveSlotManager.java   (3 slots: lee/escribe JSON, metadata)
        └── BossRegistry.java      (registra prototipos en BossFactory)
```

---

## 4. Dependencias Maven

Añadir al `pom.xml` (mantenemos Maven, no migramos a Gradle):

```xml
<properties>
    <gdx.version>1.12.1</gdx.version>
</properties>

<dependencies>
    <dependency>
        <groupId>com.badlogicgames.gdx</groupId>
        <artifactId>gdx</artifactId>
        <version>${gdx.version}</version>
    </dependency>
    <dependency>
        <groupId>com.badlogicgames.gdx</groupId>
        <artifactId>gdx-backend-lwjgl3</artifactId>
        <version>${gdx.version}</version>
    </dependency>
    <dependency>
        <groupId>com.badlogicgames.gdx</groupId>
        <artifactId>gdx-platform</artifactId>
        <version>${gdx.version}</version>
        <classifier>natives-desktop</classifier>
    </dependency>
    <dependency>
        <groupId>com.badlogicgames.gdx</groupId>
        <artifactId>gdx-freetype</artifactId>
        <version>${gdx.version}</version>
    </dependency>
    <dependency>
        <groupId>com.badlogicgames.gdx</groupId>
        <artifactId>gdx-freetype-platform</artifactId>
        <version>${gdx.version}</version>
        <classifier>natives-desktop</classifier>
    </dependency>
</dependencies>
```

Y actualizar `exec.mainClass` a `com.rpg.ui.DesktopLauncher` (mantener `com.rpg.engine.Main` como driver de tests separado).

---

## 5. Configuración de ventana

```java
public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Proyecto RPG");
        config.setWindowedMode(1280, 720);           // resolución estándar
        config.setForegroundFPS(60);
        config.useVsync(true);
        config.setResizable(false);                  // mantener proporción retro
        config.setWindowIcon("sprites/icon.png");
        new Lwjgl3Application(new RpgGame(), config);
    }
}
```

**Resolución:** 1280×720 fija. El diseño Bit-Quest funciona perfecto a esa resolución (grid de 4px = 320 columnas visibles).

Alternativa pixel-perfect: 640×360 con escalado 2× (más fiel al estilo retro auténtico). Decidir al inicio.

---

## 6. Sistema de Bullet Hell

Es la pieza con **código nuevo real** (no solo conexión). Esto NO viene de Stitch.

### Arquitectura
```java
public interface BulletPattern {
    void start(CombatBox box);
    void update(float delta, CombatBox box);
    boolean isFinished();
    List<Bullet> getActiveBullets();
}

public class Bullet {
    Vector2 position, velocity;
    Sprite sprite;
    Rectangle hitbox;
    void update(float delta) { position.mulAdd(velocity, delta); }
}

public class Soul {
    Vector2 position;
    float speed = 200f;
    Rectangle hitbox;
    void update(float delta, InputContext input, CombatBox box) {
        // mueve con flechas, clamp dentro de la caja
    }
    boolean collidesWith(Bullet b) { return hitbox.overlaps(b.hitbox); }
}
```

### Registro de patrones (mapeo motor → UI)
```java
patternRegistry.register("basic_pattern_phase_1", () -> new RadialSpreadPattern(8, 150f));
patternRegistry.register("basic_pattern_phase_2", () -> new BoneRainPattern(12, 200f));
// ...
```

El motor sigue devolviendo solo el String del patrón; la UI lo materializa.

### Cambio importante al motor
`EnemyBulletHellState.update()` actualmente aplica daño automático con fórmula fija. Para que el bullet hell sea real:
- **Opción recomendada:** quitar el `player.takeDamage()` de `update()` y dejar que la UI lo dispare en cada colisión bala-alma.
- El estado del motor solo controla la transición temporal (cuándo termina el patrón y vuelve a `PlayerMenuState`).

---

## 7. Sistema de saves (nuevo según diseños de Stitch)

El plan original solo contemplaba un save único. Los mockups de Stitch muestran **3 slots con metadata** y un **diálogo SAVE/CONTINUE mid-battle**. Esto agrega trabajo:

### `SaveSlotManager`
```java
public class SaveSlotManager {
    private static final int MAX_SLOTS = 3;
    private static final String SAVE_DIR = "saves/";
    
    public SaveSlotInfo getSlotInfo(int slot);    // metadata para mostrar en la pantalla
    public void saveToSlot(int slot, Player p, PhaseManager pm, HistoryManager hm);
    public GameState loadFromSlot(int slot);
    public void deleteSlot(int slot);
}

public record SaveSlotInfo(
    int slotNumber,
    boolean empty,
    String characterName,    // "KRIS"
    int level,               // LV 1 (en nuestro caso = phaseLevel)
    String location,         // "THE RUINS" / "FASE 1" / nombre del último boss
    String playTime          // "12:45"
) {}
```

### Archivos en disco
- `saves/slot_1.json` — contiene Player + PhaseManager + HistoryManager serializados.
- `saves/slot_2.json`
- `saves/slot_3.json`
- `saves/slot_N_meta.json` — solo metadata (lectura rápida sin parsear todo).

El motor ya tiene `Persistable` listo en las 3 clases — solo orquestamos.

---

## 8. Conexión UI ↔ motor (capa `bridge`)

### `CombatController`
```java
public class CombatController {
    private final CombatManager engine;       // del motor
    private final UiEventListener listener;   // la pantalla UI
    
    public void executeFight()         { engine.executeAction(new FightAction());    notify(); }
    public void executeAct(String t)   { engine.executeAction(new ActAction(t));     notify(); }
    public void executeItem(Item i)    { engine.executeAction(new ItemAction(i));    notify(); }
    public void executeMercy()         { engine.executeAction(new MercyAction());    notify(); }
    
    public void tick(float delta)      { engine.tick(); }
    
    public void applyBulletHit(int dmg) {
        engine.getPlayer().takeDamage(dmg);
        listener.onPlayerHurt(dmg);
    }
}

public interface UiEventListener {
    void onActionResult(ActionResult r);
    void onStateChanged(CombatState newState);
    void onCombatEnded(boolean victory, boolean spared);
    void onPlayerHurt(int damage);
}
```

`CombatScreen` implementa `UiEventListener` y reacciona a cada evento (animaciones, mensajes, sonidos).

---

## 9. Recursos visuales a producir

### Lo que tenemos (de Stitch)
✅ Design system completo (DESIGN.md + code.html)
✅ Layout de 4 pantallas (mockups PNG)
✅ Paleta y tipografía exactas

### Lo que falta producir/conseguir

**Sprites pixel art (no vienen de Stitch — necesitan otro source):**
- 1 logo del juego (sustituir "ENTRANCE LOGO")
- 5 sprites de bosses (1 por fase, mínimo estáticos)
- 1 sprite del alma (corazón rojo 16×16)
- 3-5 sprites de bullets (hueso, lanza, círculo, etc.)
- 4 iconos para los botones de menú (⚔ ☆ 🎒 ✕ pixel art)
- 3 iconos para el menú principal (espada, diskette, cruz)
- Sprites de ítems (poción, arma, armadura) — opcional, MVP usa texto

**Fuentes (descarga directa de Google Fonts):**
- [Space Mono](https://fonts.google.com/specimen/Space+Mono) (400 y 700)
- [JetBrains Mono](https://fonts.google.com/specimen/JetBrains+Mono) (500, 700, 800)

**Audio (opcional, último):**
- 1 tema de menú
- 1-2 temas de combate
- 4-5 sfx (selección menú, confirmar, hit, mercy, daño)

**Fuentes recomendadas para sprites pixel:**
- itch.io (asset packs gratuitos)
- Aseprite (crearlos a mano, ~$20)
- spriters-resource.com (Undertale assets como placeholder durante dev)

---

## 10. Orden de implementación (capas)

### UI-1 — Cimientos (1-2h)
1. Añadir dependencias libGDX al `pom.xml`.
2. `DesktopLauncher` con ventana 1280×720.
3. `RpgGame extends Game` con un screen negro.
4. `Colors.java` y carga de `Space Mono` con FreeType.
5. **Validar:** ventana abre, fondo negro, texto "Hello" en Space Mono naranja.

### UI-2 — Theme + MainMenuScreen (3-4h)
6. `Skin.java` con estilos básicos (TextButton primario/secundario).
7. `MainMenuScreen` replicando [rpg_retro_main_menu/screen.png](stitch_undertale_battle_interface/rpg_retro_main_menu/screen.png).
8. Navegación con flechas + Z, animación de borde naranja al hover.
9. **Validar:** se ve idéntico al mockup, navegable con teclado.

### UI-3 — SaveSlotScreen (2-3h)
10. `SaveSlotManager` con I/O básico (sin metadata todavía).
11. `SaveSlotScreen` replicando [save_slot_selection_screen_1](stitch_undertale_battle_interface/save_slot_selection_screen_1/screen.png).
12. **Validar:** 3 slots visibles, navegables, "EMPTY" en los vacíos.

### UI-4 — CombatScreen estática (4-5h)
13. `CombatScreen` con layout completo: grid superior, caja blanca, barra HP, 4 botones.
14. `CombatBox` con redimensión narrow↔wide.
15. `ActionMenu` con resaltado naranja.
16. **Validar:** se ve idéntico a los mockups, caja cambia de tamaño con tecla de prueba.

### UI-5 — Input + lógica básica de combate (3-4h)
17. `PlayerInputAdapter` + `InputContext`.
18. `CombatController` conectado a `CombatManager`.
19. FIGHT funciona (HP del boss baja). MERCY funciona si `canBeSpared`.
20. `DialogueBox` con texto letra-por-letra mostrando `ActionResult.message`.
21. **Validar:** combate básico jugable sin bullet hell, HP del boss llega a 0.

### UI-6 — Bullet Hell ★ (8-12h)
22. `Soul`, `Bullet`, `BulletPattern` (interfaz).
23. 2-3 patrones concretos (`RadialSpread`, `BoneRain`, `Wave`).
24. `BulletPatternRegistry` mapeando strings del motor a patrones UI.
25. Detección de colisiones + `applyBulletHit`.
26. Modificar `EnemyBulletHellState` del motor para no aplicar daño automático.
27. **Validar:** el alma esquiva balas reales, recibe daño al chocar.

### UI-7 — Submenús y diálogos completos (4-6h)
28. `ItemSubmenu` con inventario navegable.
29. `ActSubmenu` con opciones por boss.
30. `SaveExitDialog` (modal en ESC).
31. Diálogo del boss durante ACT (`Boss.nextDialogue()`).
32. **Validar:** ITEM cura, ACT incrementa `actsPerformed`, todo el flujo Undertale funciona.

### UI-8 — Flujo completo (5-7h)
33. `DialogueScreen` entre fases (con `StoryGenerator`).
34. `GameOverScreen` + `VictoryScreen`.
35. Transiciones entre pantallas, música por estado.
36. `SaveSlotManager` con metadata real (level, location, playtime).
37. **Validar:** partida completa de 5 fases, save/load funciona.

### UI-9 — Polish (variable)
38. Animaciones de bosses (idle, ataque, daño).
39. Más patrones de bullet hell (uno único por boss).
40. Efectos de partículas (golpe, mercy, victoria).
41. Música y sfx completos.
42. Logo final, iconos pulidos.

---

## 11. Estimación total

| Bloque | Horas |
|---|---|
| UI-1 a UI-3 (cimientos + 2 pantallas) | 6-9h |
| UI-4 a UI-5 (combate básico) | 7-9h |
| **UI-6 (bullet hell — el grande)** | **8-12h** |
| UI-7 a UI-8 (submenús + flujo completo) | 9-13h |
| UI-9 (polish + assets finales) | 10h+ |
| **Total MVP jugable (UI-1 a UI-8)** | **30-43h** |

---

## 12. Próximas decisiones inmediatas

Antes de escribir la primera línea de libGDX, conviene resolver:

1. **Resolución final:** ¿1280×720 directo o 640×360 escalado 2× (más pixel-perfect)?
2. **Pantallas que faltan diseñar en Stitch:** generar mockups de `DialogueScreen`, `GameOverScreen`, `VictoryScreen`, `InventorySubmenu`, `ActSubmenu` antes de codear UI-7.
3. **Sprites de bosses:** ¿usamos placeholders cuadrados de colores durante dev y los reemplazamos al final? ¿O conseguimos arte real desde UI-4?
4. **Cambio al motor:** confirmar que `EnemyBulletHellState` deja de aplicar daño automático (necesario para UI-6).
5. **Logo del juego:** "ENTRANCE LOGO" del mockup es placeholder — ¿tenés nombre/concepto para el juego?

---

## Referencias rápidas

- **Design system:** [stitch_undertale_battle_interface/bit_quest_monolith/DESIGN.md](stitch_undertale_battle_interface/bit_quest_monolith/DESIGN.md)
- **HTML/CSS exacto del SaveSlot:** [save_slot_selection_screen_1/code.html](stitch_undertale_battle_interface/save_slot_selection_screen_1/code.html)
- **libGDX wiki:** https://libgdx.com/wiki/
- **Skin Composer (oficial libGDX):** https://github.com/raeleus/skin-composer
- **Google Fonts (Space Mono + JetBrains Mono):** ya enlazados arriba

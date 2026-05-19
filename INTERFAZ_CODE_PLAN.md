# Plan de codificación — UI libGDX

> Complemento de [INTERFAZ.md](INTERFAZ.md). Este documento se enfoca en **qué escribir y en qué orden** para construir la UI completa, asumiendo que ya tenemos:
> - 9 mockups en [stitch_undertale_battle_interface/](stitch_undertale_battle_interface/)
> - 2 fuentes en [Fuentes de texto/](Fuentes%20de%20texto/) — `DTM-Mono.otf` y `DTM-Sans.otf`
> - Motor lógico cerrado con 120/120 tests pasando

---

## Inventario actual

### Pantallas mockeadas (9)
| Mockup | Pantalla libGDX | HTML referencia |
|---|---|---|
| `rpg_retro_main_menu` | `MainMenuScreen` | – |
| `save_slot_selection_screen_1` | `SaveSlotScreen` | ✓ |
| `save_slot_selection_screen_2` | `SaveExitDialog` (modal) | – |
| `battle_interface_narrow_box` | `CombatScreen` (modo diálogo) | – |
| `battle_interface_wide_box` | `CombatScreen` (modo bullet hell) | – |
| `battle_act_submenu` | `ActSubmenu` (overlay) | ✓ |
| `battle_inventory_submenu` | `ItemSubmenu` (overlay) | ✓ |
| `narrative_dialogue_phase_3` | `DialogueScreen` | – |
| `game_over_screen` | `GameOverScreen` | ✓ |
| `victory_game_ending_screen` | `VictoryScreen` | ✓ |

### Fuentes
- **`DTM-Mono.otf`** — Determination Mono (Undertale auténtica). Para HP, números, labels.
- **`DTM-Sans.otf`** — Determination Sans (Undertale auténtica). Para diálogos, narrativa, títulos.

> **Cambio respecto al plan anterior:** abandonamos Space Mono / JetBrains Mono. Las fuentes DTM dan el feeling Undertale puro y son las que el usuario ya tiene.

### Lo que NO tenemos (a producir o sustituir por placeholders)
- Sprites de bosses (5+)
- Sprite del alma (corazón rojo 16×16)
- Sprites de bullets (3-5 tipos)
- Iconos pixel art para los 4 botones FIGHT/ACT/ITEM/MERCY (sustituimos los Material Symbols del HTML)
- Logo del juego (sustituir "ENTRANCE LOGO")
- Audio (puede esperar al final)

### Lo que descartamos del HTML de Stitch
- **Bottom navigation bar** ("COMMAND / STATUS / INVENTORY / SYSTEM") — es estilo web/mobile, no aplica al juego. Solo usamos el contenido central de cada mockup.
- **Side navigation bar** ("OPERATOR_01 / QUESTS / MAP / LOGS") — idem.
- **CRT scanline overlay** — opcional, lo dejamos para UI-9 (polish).
- **Material Symbols Icons** — los reemplazamos con sprites pixel art propios.

---

## 1. Setup inicial — `pom.xml` y estructura de paquetes

### `pom.xml`
Añadir las dependencias libGDX (ver [INTERFAZ.md §4](INTERFAZ.md)) y cambiar `exec.mainClass`:

```xml
<exec.mainClass>com.rpg.ui.DesktopLauncher</exec.mainClass>
```

Mantener un perfil Maven secundario para correr el `Main.java` actual (los tests de consola), o moverlo a `com.rpg.engine.test.ConsoleTests`.

### Estructura de paquetes a crear
```
src/main/java/com/rpg/ui/
├── DesktopLauncher.java
├── RpgGame.java
├── theme/
│   ├── Theme.java          (colores + acceso a fuentes + skin)
│   ├── Fonts.java          (carga DTM-Mono y DTM-Sans con FreeType)
│   └── Palette.java        (constantes de color del Bit-Quest)
├── screens/
│   ├── BaseScreen.java     (abstracta — viewport, batch común)
│   ├── MainMenuScreen.java
│   ├── SaveSlotScreen.java
│   ├── DialogueScreen.java
│   ├── CombatScreen.java
│   ├── GameOverScreen.java
│   └── VictoryScreen.java
├── widgets/
│   ├── PixelButton.java        (botón rectangular con borde 2/4 px)
│   ├── PixelPanel.java         (panel con borde grueso)
│   ├── HpBar.java              (barra amarilla con texto "20/20")
│   ├── DialogueBox.java        (caja con texto letra-por-letra)
│   ├── ActionMenu.java         (los 4 botones FIGHT/ACT/ITEM/MERCY)
│   ├── ItemListWidget.java     (lista grid 2-columnas del inventario)
│   ├── ActListWidget.java      (lista con header "* BOSSNAME")
│   ├── SaveSlotCard.java       (panel de slot con metadata)
│   └── SaveExitDialog.java     (modal SAVE/CONTINUE)
├── combat/
│   ├── CombatBox.java          (caja redimensionable narrow↔wide)
│   ├── Soul.java               (corazón rojo movido por input)
│   ├── Bullet.java             (proyectil)
│   ├── BulletPattern.java      (interfaz)
│   ├── BulletPatternRegistry.java
│   └── patterns/
│       ├── RadialSpreadPattern.java
│       ├── BoneRainPattern.java
│       └── WavePattern.java
├── input/
│   ├── InputContext.java       (mapeo teclas según estado)
│   └── KeyMap.java             (constantes: CONFIRM=Z, CANCEL=X, etc.)
├── assets/
│   ├── AssetPaths.java
│   └── GameAssets.java         (wrapper AssetManager)
└── bridge/
    ├── CombatController.java   (UI ↔ engine.CombatManager)
    ├── UiEventListener.java
    ├── SaveSlotManager.java    (lee/escribe 3 slots JSON)
    ├── SaveSlotInfo.java       (record con metadata)
    └── BossRegistry.java       (registra prototipos al iniciar)
```

### Carpeta de assets
```
src/main/resources/
├── fonts/
│   ├── DTM-Mono.otf            ← mover desde "Fuentes de texto/"
│   └── DTM-Sans.otf            ← mover desde "Fuentes de texto/"
├── sprites/
│   ├── ui/                     (botones, marcos, iconos UI)
│   ├── bosses/                 (5+ sprites)
│   ├── bullets/                (3-5 tipos)
│   ├── items/                  (poción, espada, etc. — opcional)
│   └── soul.png                (corazón rojo)
├── audio/                      (opcional, UI-9)
└── data/
    └── saves/                  (3 archivos JSON al usar)
```

---

## 2. Capa Theme — colores, fuentes y skin

### `Palette.java`
```java
package com.rpg.ui.theme;
import com.badlogic.gdx.graphics.Color;

public final class Palette {
    public static final Color BG          = Color.valueOf("000000");
    public static final Color SURFACE     = Color.valueOf("131313");
    public static final Color SURFACE_LOW = Color.valueOf("0E0E0E");
    public static final Color PRIMARY     = Color.valueOf("FF9D00");  // naranja activo
    public static final Color PRIMARY_LT  = Color.valueOf("FFC485");  // naranja text
    public static final Color ON_SURFACE  = Color.valueOf("E2E2E2");  // blanco principal
    public static final Color SECONDARY   = Color.valueOf("C6C6C7");  // gris hint
    public static final Color OUTLINE     = Color.valueOf("A28D79");  // bordes inactivos
    public static final Color WARNING     = Color.valueOf("FFFF00");  // amarillo HP
    public static final Color ERROR       = Color.valueOf("FFB4AB");  // rojo GAME OVER
    private Palette() {}
}
```

### `Fonts.java`
```java
package com.rpg.ui.theme;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;

public class Fonts implements Disposable {
    // Sans → diálogos, narrativa, títulos grandes
    public BitmapFont sans72, sans32, sans24, sans18, sans16;
    // Mono → labels, HP, números, botones de menú
    public BitmapFont mono24, mono18, mono14, mono12;

    private FreeTypeFontGenerator sansGen, monoGen;

    public void load() {
        sansGen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/DTM-Sans.otf"));
        monoGen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/DTM-Mono.otf"));
        sans72 = gen(sansGen, 72);
        sans32 = gen(sansGen, 32);
        sans24 = gen(sansGen, 24);
        sans18 = gen(sansGen, 18);
        sans16 = gen(sansGen, 16);
        mono24 = gen(monoGen, 24);
        mono18 = gen(monoGen, 18);
        mono14 = gen(monoGen, 14);
        mono12 = gen(monoGen, 12);
    }

    private BitmapFont gen(FreeTypeFontGenerator g, int size) {
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size = size;
        p.color = Palette.ON_SURFACE;
        p.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
        p.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
        return g.generateFont(p);
    }

    @Override public void dispose() {
        sansGen.dispose(); monoGen.dispose();
        // dispose todas las fonts...
    }
}
```

### `Theme.java`
Punto único de acceso desde cualquier screen:
```java
public class Theme {
    public final Fonts fonts;
    public final Skin skin;        // libGDX Skin con estilos preconfigurados
    public Theme() {
        this.fonts = new Fonts();
        this.fonts.load();
        this.skin = buildSkin();
    }
    private Skin buildSkin() {
        Skin s = new Skin();
        s.add("sans32", fonts.sans32);
        s.add("mono18", fonts.mono18);
        // ... registrar estilos de TextButton, Label, etc.
        return s;
    }
}
```

---

## 3. Capa de Widgets — piezas reutilizables

Cada widget está calcado de los mockups. Aquí van los más importantes.

### `PixelButton`
Rectángulo con texto + icono opcional, borde 2px o 4px, color naranja si está seleccionado:
```java
public class PixelButton {
    private final Rectangle bounds;
    private final String text;
    private final BitmapFont font;
    private boolean selected;
    private int borderWidth = 2;

    public PixelButton(float x, float y, float w, float h, String text, BitmapFont font) { ... }

    public void render(ShapeRenderer shapes, SpriteBatch batch) {
        Color borderColor = selected ? Palette.PRIMARY : Palette.ON_SURFACE;
        int bw = selected ? 4 : 2;
        drawBorderRect(shapes, bounds, borderColor, bw);
        font.setColor(selected ? Palette.PRIMARY : Palette.ON_SURFACE);
        drawTextCentered(batch, font, text, bounds);
    }
    public void setSelected(boolean s) { this.selected = s; }
}
```

### `HpBar`
Replica del HTML: `"LV 1   HP  [▮▮▮▮▮▮▮▮░░] 20/20"`:
```java
public class HpBar {
    private final BitmapFont mono;
    private int currentHp, maxHp, level;
    public void render(SpriteBatch batch, ShapeRenderer shapes, float x, float y) {
        // 1. "LV N" en mono18 blanco
        // 2. "HP" en mono14 blanco
        // 3. rectángulo: fondo gris, fill amarillo proporcional al hp
        // 4. "N / M" en mono18 blanco
    }
}
```

### `DialogueBox`
Caja con texto letra-por-letra + triángulo parpadeante:
```java
public class DialogueBox {
    private String fullText;
    private int charsShown;
    private float charTimer;
    private final float charsPerSecond = 30f;
    private boolean complete;

    public void show(String text) { fullText = text; charsShown = 0; complete = false; }
    public void update(float delta) {
        if (complete) return;
        charTimer += delta;
        while (charTimer > 1f/charsPerSecond && charsShown < fullText.length()) {
            charsShown++;
            charTimer -= 1f/charsPerSecond;
            // sfx "blip" — opcional
        }
        if (charsShown == fullText.length()) complete = true;
    }
    public void skip() { charsShown = fullText.length(); complete = true; }
    public boolean isComplete() { return complete; }
    public void render(SpriteBatch batch, ShapeRenderer shapes, Rectangle bounds) {
        // borde 4px blanco
        // texto fullText.substring(0, charsShown) en sans18
        // si complete: triángulo ▼ parpadeante abajo-derecha
    }
}
```

### `ActionMenu`
Los 4 botones del combate con navegación:
```java
public class ActionMenu {
    private final PixelButton[] buttons = new PixelButton[4]; // FIGHT, ACT, ITEM, MERCY
    private int selectedIndex = 0;
    public void moveLeft()  { selectedIndex = (selectedIndex + 3) % 4; updateSelection(); }
    public void moveRight() { selectedIndex = (selectedIndex + 1) % 4; updateSelection(); }
    public int getSelected() { return selectedIndex; }
    public void render(SpriteBatch batch, ShapeRenderer shapes) {
        for (PixelButton b : buttons) b.render(shapes, batch);
    }
}
```

### `SaveSlotCard`
Calcado del HTML: panel con nombre del slot a la izquierda, ubicación y tiempo a la derecha:
```java
public class SaveSlotCard {
    private final int slotNumber;
    private SaveSlotInfo info;        // null = vacío
    private boolean selected;
    public void render(SpriteBatch batch, ShapeRenderer shapes, Rectangle bounds) {
        // borde naranja 4px si selected, blanco 4px si no
        // si info != null:
        //   IZQ: "SLOT N" en naranja, "NAME - LV N" en blanco
        //   DER: "LOCATION" + "TIME" en gris
        // si info == null:
        //   IZQ: "SLOT N" en blanco, "---EMPTY---" en gris
        //   DER: "----------" + "--:--" en gris oscuro
    }
}
```

---

## 4. Capa de Pantallas — una por mockup

Cada `Screen` implementa `com.badlogic.gdx.Screen` (o extiende `BaseScreen`).

### `BaseScreen`
```java
public abstract class BaseScreen implements Screen {
    protected final RpgGame game;
    protected final OrthographicCamera camera;
    protected final SpriteBatch batch;
    protected final ShapeRenderer shapes;
    protected final Theme theme;

    public BaseScreen(RpgGame game) {
        this.game = game;
        this.theme = game.getTheme();
        this.batch = game.getBatch();
        this.shapes = game.getShapes();
        this.camera = new OrthographicCamera(1280, 720);
        this.camera.position.set(640, 360, 0);
        this.camera.update();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Palette.BG);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
        renderContent(delta);
    }
    protected abstract void renderContent(float delta);
    // resize, dispose, etc. con defaults
}
```

### Implementación de cada pantalla (orden recomendado)

#### `MainMenuScreen` (sprint UI-2)
Layout vertical centrado:
- Logo placeholder arriba (puede ser un texto "PROYECTO RPG" en `sans72` naranja hasta tener el sprite).
- 3 botones apilados: `PLAY`, `LOAD GAME`, `EXIT` — usar `PixelButton`.
- Navegación: ↑/↓ cambia `selectedIndex`, Z confirma:
  - PLAY → `game.setScreen(new DialogueScreen(...))` (intro de fase 1).
  - LOAD GAME → `game.setScreen(new SaveSlotScreen(SaveMode.LOAD))`.
  - EXIT → `Gdx.app.exit()`.

#### `SaveSlotScreen` (sprint UI-3)
Replica de [save_slot_selection_screen_1](stitch_undertale_battle_interface/save_slot_selection_screen_1/screen.png):
- Header "SELECT SLOT" arriba en naranja `sans32` + separador 4px blanco.
- 3 `SaveSlotCard` apilados verticalmente (centrados, 800px ancho, 100px alto, 16px gap).
- Footer "← BACK" abajo-izquierda con `PixelButton`.
- Modo de uso: enum `SaveMode { SAVE, LOAD }`:
  - SAVE: al confirmar slot vacío o ocupado → `saveSlotManager.saveToSlot(n, ...)`.
  - LOAD: al confirmar slot ocupado → `saveSlotManager.loadFromSlot(n)` → restaurar partida.

#### `CombatScreen` (sprints UI-4, UI-5, UI-6)
La pantalla más compleja. Estructura:
- Zona del enemigo arriba (~40% altura): cuadrícula 6×2 con bordes verdes como placeholder + sprite del boss centrado.
- `CombatBox` en el centro (50% altura). Renderiza distinto según `CombatState`:
  - `PlayerMenuState` → modo `narrow`, contiene `DialogueBox` con el último mensaje del `ActionResult`.
  - `PlayerQteState` → modo `narrow`, barra de timing animada dentro.
  - `EnemyBulletHellState` → modo `wide`, el `Soul` y los `Bullet` activos se renderizan dentro.
- `HpBar` debajo de la caja.
- `ActionMenu` abajo de todo. Solo visible si el estado es `PlayerMenuState`.
- Si el jugador selecciona ITEM → muestra `ItemSubmenu` sobre el `DialogueBox`.
- Si selecciona ACT → muestra `ActSubmenu` sobre el `DialogueBox`.

Esqueleto:
```java
public class CombatScreen extends BaseScreen implements UiEventListener {
    private final CombatController controller;
    private final CombatBox combatBox;
    private final HpBar hpBar;
    private final ActionMenu actionMenu;
    private final DialogueBox dialogueBox;
    private final Soul soul;
    private BulletPattern activePattern;
    private Submenu activeSubmenu; // null, ItemSubmenu, ActSubmenu
    private OverlayMode overlay = OverlayMode.NONE;

    @Override
    protected void renderContent(float delta) {
        controller.tick(delta);              // avanza el motor
        updateBulletHellIfActive(delta);
        renderEnemyZone();
        combatBox.render(...);
        renderInsideCombatBox();             // soul + bullets, o dialogue
        hpBar.render(...);
        if (isPlayerMenuActive()) actionMenu.render(...);
        if (activeSubmenu != null) activeSubmenu.render(...);
    }

    // UiEventListener
    @Override public void onActionResult(ActionResult r) {
        dialogueBox.show(r.getMessage());
    }
    @Override public void onStateChanged(CombatState newState) {
        combatBox.setMode(newState instanceof EnemyBulletHellState ? WIDE : NARROW);
    }
    @Override public void onCombatEnded(boolean victory, boolean spared) {
        if (victory) game.setScreen(new DialogueScreen(...)); // siguiente fase
        else         game.setScreen(new GameOverScreen(game));
    }
    @Override public void onPlayerHurt(int dmg) {
        // efecto visual: flash rojo, screen shake leve
    }
}
```

#### `DialogueScreen` (sprint UI-8)
Replica de [narrative_dialogue_phase_3](stitch_undertale_battle_interface/narrative_dialogue_phase_3/screen.png):
- Header con título "PHASE N / ROUTE" arriba.
- Panel central grande con portrait placeholder + `DialogueBox` con el texto de `StoryGenerator.generateNarrativeForPhase(n)`.
- Footer con hints "[Z] CONTINUE   [X] SKIP".
- Z avanza al `CombatScreen` con el boss de la fase actual.

#### `GameOverScreen` (sprint UI-8)
Replica de [game_over_screen](stitch_undertale_battle_interface/game_over_screen/screen.png):
- "GAME OVER" en `sans72` color `Palette.ERROR`, centrado.
- Línea 2px blanca debajo, 400px ancho.
- Texto narrativo en `sans18` italic centrado (3 líneas).
- 2 botones apilados (300×60): "▶ RETRY PHASE" (naranja), "← RETURN TO MENU" (blanco).
- Hints abajo: "[↑↓] NAVIGATE   [Z] CONFIRM".

#### `VictoryScreen` (sprint UI-8)
Replica de [victory_game_ending_screen](stitch_undertale_battle_interface/victory_game_ending_screen/screen.png):
- "THE END" en `sans72` naranja.
- "ROUTE: PACIFIC/NEUTRAL/GENOCIDE" en `sans24` amarillo (color según ruta).
- Línea 4px blanca.
- Panel central con borde naranja 4px conteniendo el texto narrativo final.
- 3 celdas (200×100) con borde 2px blanco: SPARED, DEFEATED, TIME.
- Botón "← RETURN TO MAIN MENU" abajo (400×60).

#### `SaveExitDialog` (sprint UI-7) — overlay/modal sobre CombatScreen
Replica de [save_slot_selection_screen_2](stitch_undertale_battle_interface/save_slot_selection_screen_2/screen.png):
- Aparece al presionar ESC durante combate. Pausa el `controller.tick()`.
- Caja centrada con texto "Would you like to SAVE and EXIT or CONTINUE the battle?".
- 2 botones grandes: "💾 SAVE & EXIT" / "⚔ CONTINUE".

---

## 5. Capa de Bullet Hell (sprint UI-6)

Esta capa es **código nuevo real**, no traducción de mockups.

### `Soul`
```java
public class Soul {
    private final Vector2 pos = new Vector2();
    private final Vector2 hitboxSize = new Vector2(12, 12); // pequeña, justa
    private float speed = 200f;
    private Texture sprite;

    public void update(float delta, CombatBox box) {
        Vector2 dir = readInputDirection();
        pos.mulAdd(dir.nor(), speed * delta);
        // clamp dentro de la caja
        pos.x = MathUtils.clamp(pos.x, box.getX() + 6, box.getX() + box.getW() - 6);
        pos.y = MathUtils.clamp(pos.y, box.getY() + 6, box.getY() + box.getH() - 6);
    }
    public boolean collidesWith(Bullet b) {
        Rectangle soulRect = new Rectangle(pos.x - 6, pos.y - 6, 12, 12);
        return soulRect.overlaps(b.getHitbox());
    }
    public void render(SpriteBatch batch) { batch.draw(sprite, pos.x - 8, pos.y - 8, 16, 16); }
}
```

### `Bullet`
```java
public class Bullet {
    public final Vector2 pos, vel;
    public final Rectangle hitbox;
    public final Sprite sprite;
    private float lifetime;

    public void update(float delta) {
        pos.mulAdd(vel, delta);
        hitbox.setPosition(pos.x - hitbox.width/2, pos.y - hitbox.height/2);
        lifetime += delta;
    }
    public boolean shouldDie(CombatBox box) {
        return !box.contains(pos) || lifetime > 10f;
    }
}
```

### `BulletPattern` (Strategy real)
```java
public interface BulletPattern {
    void start(CombatBox box);
    void update(float delta, CombatBox box);
    boolean isFinished();
    List<Bullet> getActiveBullets();
}
```

### `BulletPatternRegistry`
```java
public class BulletPatternRegistry {
    private final Map<String, Supplier<BulletPattern>> registry = new HashMap<>();
    public void register(String name, Supplier<BulletPattern> factory) {
        registry.put(name, factory);
    }
    public BulletPattern get(String name) {
        Supplier<BulletPattern> f = registry.get(name);
        return f == null ? new EmptyPattern() : f.get();
    }
}
```

Al inicio del juego (en `RpgGame.create()`):
```java
patternRegistry.register("basic_pattern_phase_1", () -> new RadialSpreadPattern(8, 150f));
patternRegistry.register("basic_pattern_phase_2", () -> new BoneRainPattern(12, 200f));
// ... uno por fase mínimo
```

### Patrones concretos a implementar (MVP)
- **`RadialSpreadPattern(int n, float speed)`** — N balas desde el centro hacia afuera en círculo.
- **`BoneRainPattern(int count, float speed)`** — huesos cayendo desde arriba.
- **`WavePattern(float amplitude, float frequency)`** — balas en onda sinusoidal.

### Modificación al motor (necesaria para UI-6)
`EnemyBulletHellState.update()` actualmente aplica daño automático. Cambiar para que el daño venga de colisiones reales:

```java
// Antes:
if (mitigated > 0) ctx.getPlayer().takeDamage(mitigated);

// Después: dejar update() solo controlando la transición temporal.
// El daño se aplica desde CombatController.applyBulletHit() cuando la UI detecta colisión.
```

---

## 6. Capa Bridge — pegamento UI ↔ motor

### `CombatController`
```java
public class CombatController {
    private final CombatManager engine;
    private final UiEventListener listener;
    private final BulletPatternRegistry patterns;
    private BulletPattern activePattern;

    public void executeFight()        { run(new FightAction()); }
    public void executeAct(String t)  { run(new ActAction(t)); }
    public void executeItem(Item i)   { run(new ItemAction(i)); }
    public void executeMercy()        { run(new MercyAction()); }
    private void run(CombatAction a) {
        CombatState before = engine.getCurrentState();
        engine.executeAction(a);
        if (before != engine.getCurrentState()) listener.onStateChanged(engine.getCurrentState());
        if (engine.isCombatOver()) {
            engine.handleVictoryLootDrop();
            listener.onCombatEnded(engine.getCurrentBoss().isSpared() || !engine.getCurrentBoss().isAlive(),
                                    engine.getCurrentBoss().isSpared());
        }
    }

    public void tick(float delta) {
        engine.tick();
        if (engine.getCurrentState() instanceof EnemyBulletHellState) {
            if (activePattern == null) {
                String name = engine.getCurrentBoss().executeBulletHellPattern();
                activePattern = patterns.get(name);
                activePattern.start(...);
            }
            activePattern.update(delta, ...);
        } else {
            activePattern = null;
        }
    }

    public void applyBulletHit(int damage) {
        int mitigated = Math.max(0, damage - engine.getPlayer().getTotalDefense());
        engine.getPlayer().takeDamage(mitigated);
        listener.onPlayerHurt(mitigated);
        if (!engine.getPlayer().isAlive()) {
            listener.onCombatEnded(false, false);
        }
    }
}
```

### `SaveSlotManager`
```java
public class SaveSlotManager {
    private static final String DIR = "saves/";

    public SaveSlotInfo getSlotInfo(int slot) {
        FileHandle meta = Gdx.files.local(DIR + "slot_" + slot + "_meta.json");
        if (!meta.exists()) return SaveSlotInfo.empty(slot);
        return JsonUtil.fromJson(meta.readString(), SaveSlotInfo.class);
    }

    public void saveToSlot(int slot, Player p, PhaseManager pm, HistoryManager hm, long playMillis) {
        String full = JsonUtil.toJson(new SaveBundle(p.saveData(), pm.saveData(), hm.saveData()));
        Gdx.files.local(DIR + "slot_" + slot + ".json").writeString(full, false);
        SaveSlotInfo meta = new SaveSlotInfo(slot, false, p.getName(), pm.getCurrentPhase(),
                                              currentLocation(pm), formatTime(playMillis));
        Gdx.files.local(DIR + "slot_" + slot + "_meta.json").writeString(JsonUtil.toJson(meta), false);
    }

    public void loadFromSlot(int slot, Player p, PhaseManager pm, HistoryManager hm)
        throws SaveCorruptionException {
        SaveBundle b = JsonUtil.fromJson(
            Gdx.files.local(DIR + "slot_" + slot + ".json").readString(), SaveBundle.class);
        p.loadData(b.playerJson);
        pm.loadData(b.phaseJson);
        hm.loadData(b.historyJson);
    }

    private record SaveBundle(String playerJson, String phaseJson, String historyJson) {}
}
```

### `SaveSlotInfo`
```java
public record SaveSlotInfo(
    int slot,
    boolean empty,
    String characterName,   // "KRIS"
    int level,              // phaseLevel
    String location,        // "THE RUINS" / nombre del último boss
    String playTime         // "12:45"
) {
    public static SaveSlotInfo empty(int slot) {
        return new SaveSlotInfo(slot, true, null, 0, null, null);
    }
}
```

---

## 7. Sprints — orden de implementación con entregables concretos

Cada sprint produce algo **visualmente verificable**. Los tests del motor (120/120) deben seguir pasando después de cada sprint.

### Sprint 0 — Preparación (~30min)
- [ ] Copiar `DTM-Mono.otf` y `DTM-Sans.otf` a `src/main/resources/fonts/`.
- [ ] Añadir dependencias libGDX al `pom.xml`.
- [ ] Crear estructura de paquetes vacía en `com.rpg.ui`.
- [ ] Actualizar `run.bat` para ejecutar `com.rpg.ui.DesktopLauncher`.
- **Entregable:** `mvn compile` pasa con las nuevas deps.

### Sprint 1 — Cimientos (UI-1) ~1-2h
- [ ] `DesktopLauncher` con ventana 1280×720.
- [ ] `RpgGame extends Game` con `Theme`, batch, shapeRenderer compartidos.
- [ ] `Palette.java` con todos los colores.
- [ ] `Fonts.java` cargando DTM-Sans y DTM-Mono con FreeType.
- [ ] `BaseScreen` con render base (camera, clear color).
- [ ] Una `TestScreen` que muestre "DETERMINATION" en `sans72` naranja sobre fondo negro.
- **Entregable:** ventana abre, fuente Undertale visible.

### Sprint 2 — MainMenuScreen (UI-2) ~3-4h
- [ ] Widgets básicos: `PixelButton`, `PixelPanel`.
- [ ] `MainMenuScreen` con logo placeholder + 3 botones.
- [ ] Navegación con ↑/↓, Z confirma.
- [ ] Transiciones (al menos a screens vacías por ahora).
- **Entregable:** pantalla idéntica al mockup, navegable con teclado.

### Sprint 3 — SaveSlotScreen + SaveSlotManager (UI-3) ~3-4h
- [ ] `SaveSlotInfo` (record) y `SaveSlotManager` con I/O básico.
- [ ] `SaveSlotCard` widget.
- [ ] `SaveSlotScreen` con los 3 slots, navegación, modo SAVE/LOAD.
- [ ] Botón BACK que regresa al menú.
- **Entregable:** se ven 3 slots (todos EMPTY al inicio), navegables.

### Sprint 4 — CombatScreen estática (UI-4) ~5-6h
- [ ] `CombatBox` con modos NARROW y WIDE (sin lógica todavía).
- [ ] `HpBar`.
- [ ] `ActionMenu` con los 4 botones.
- [ ] `CombatScreen` con layout completo, datos de prueba hardcoded.
- [ ] Tecla M alterna manualmente entre NARROW y WIDE para validar.
- **Entregable:** combate visualmente idéntico a los dos mockups (narrow/wide).

### Sprint 5 — Combate funcional sin bullet hell (UI-5) ~4-5h
- [ ] `KeyMap`, `InputContext`, `PlayerInputAdapter`.
- [ ] `CombatController` conectado a `CombatManager` del motor.
- [ ] `BossRegistry` que registra prototipos al iniciar.
- [ ] `DialogueBox` con texto letra-por-letra.
- [ ] FIGHT baja HP del boss; MERCY funciona si `canBeSpared`; ITEM permite usar pociones (solo logica, sin submenú).
- [ ] Transición a `CombatScreen` → `GameOverScreen` placeholder cuando combate termina.
- **Entregable:** combate jugable sin bullet hell. Boss muere, mensajes salen.

### Sprint 6 — Bullet Hell (UI-6) ★ ~10-12h
- [ ] Modificar `EnemyBulletHellState.update()` del motor para no aplicar daño automático.
- [ ] Tests del motor: añadir un test que confirme que ya no aplica daño (validar regresión).
- [ ] `Soul`, `Bullet`, `BulletPattern`, `BulletPatternRegistry`.
- [ ] Implementar `RadialSpreadPattern`.
- [ ] Detección de colisiones + `applyBulletHit` en `CombatController`.
- [ ] Sprite de soul (corazón rojo placeholder) y bullet (círculo blanco placeholder).
- [ ] Implementar `BoneRainPattern` y `WavePattern`.
- [ ] Registrar mínimo 3 patrones en `BulletPatternRegistry` mapeados a los nombres que devuelve el motor.
- **Entregable:** el alma esquiva balas reales, recibe daño al chocar, retorna al menú al terminar el patrón.

### Sprint 7 — Submenús + SaveExitDialog (UI-7) ~5-6h
- [ ] `ItemListWidget` con grid 2-columnas (calcado del mockup).
- [ ] `ActListWidget` con header "* BOSSNAME" (calcado del mockup).
- [ ] `ItemSubmenu` y `ActSubmenu` que aparecen sobre la `DialogueBox`.
- [ ] Navegación ↑↓←→ + Z confirma + X cancela.
- [ ] `SaveExitDialog` (modal) en ESC con SAVE & EXIT / CONTINUE.
- [ ] Diálogo del boss durante ACT (`Boss.nextDialogue()` se muestra en `DialogueBox`).
- **Entregable:** flujo Undertale completo funcional. ITEM usa pociones, ACT incrementa `actsPerformed`, ESC pausa.

### Sprint 8 — Pantallas terminales + flujo completo (UI-8) ~6-7h
- [ ] `DialogueScreen` entre fases con texto de `StoryGenerator`.
- [ ] `GameOverScreen` funcional (retry / menú).
- [ ] `VictoryScreen` funcional con stats de `HistoryManager`.
- [ ] `PhaseManager` orquesta la secuencia: dialogue → combat → dialogue → combat ... → victory.
- [ ] `SaveSlotManager.saveToSlot()` con metadata real (level, location, playtime).
- [ ] Tiempo de juego medido (acumulador en `RpgGame`).
- **Entregable:** partida completa de 5 fases. Save/load round-trip funciona.

### Sprint 9 — Polish (UI-9) variable
- [ ] Sprites reales de bosses (5 mínimo).
- [ ] Sprite del alma real, sprites de bullets variados.
- [ ] Iconos pixel art para los 4 botones del menú (sustituyen los Material Symbols del HTML).
- [ ] Logo del juego (sustituye "ENTRANCE LOGO").
- [ ] Más patrones de bullet hell (al menos 1 único por boss, ~5 nuevos).
- [ ] Animaciones de bosses (idle, hurt).
- [ ] Música de combate, menú; sfx (select, confirm, hit, mercy).
- [ ] CRT scanline overlay (opcional, shader o textura).
- [ ] Flash rojo + screen shake al recibir daño.

---

## 8. Estimación total

| Sprint | Horas | Acumulado |
|---|---|---|
| 0 — Setup | 0.5 | 0.5h |
| 1 — Cimientos | 1.5 | 2h |
| 2 — MainMenu | 3.5 | 5.5h |
| 3 — SaveSlot | 3.5 | 9h |
| 4 — CombatScreen estática | 5.5 | 14.5h |
| 5 — Combate funcional | 4.5 | 19h |
| **6 — Bullet Hell** | **11** | **30h** |
| 7 — Submenús | 5.5 | 35.5h |
| 8 — Pantallas terminales | 6.5 | 42h |
| 9 — Polish | 10+ | 52h+ |

**MVP jugable (sprints 0-8): ~42h.**

---

## 9. Decisiones pendientes antes de Sprint 0

1. **¿Confirmamos cambio al motor en Sprint 6?** Quitar el daño automático de `EnemyBulletHellState.update()` para que el bullet hell sea real. Sin este cambio, el daño se duplicaría (motor + UI lo aplicarían). **Recomendado: sí.**

2. **¿Sprites placeholders o reales desde el inicio?** Recomendado: **placeholders coloridos** (rectángulos rellenos) durante sprints 4-7, sprites reales en sprint 9. Acelera todo el flujo.

3. **¿Nombre del juego?** "ENTRANCE LOGO" del mockup es placeholder. Si no decidimos algo, usamos "DETERMINATION" o "PROYECTO PROGRA II" hasta sprint 9.

4. **¿Qué hacer con el `Main.java` actual?** Tiene 120 tests de consola. Opciones:
   - (a) Renombrarlo a `com.rpg.engine.ConsoleTests` y mantenerlo como herramienta de regresión paralela.
   - (b) Eliminarlo cuando arranque UI-5 (los tests se reemplazan por verificación visual).
   - Recomendado: **(a)** — los tests del motor siguen siendo valiosos incluso con UI.

5. **¿Resolución final?** 1280×720 (los mockups están a esa escala). Pixel-perfect retro vendría a 640×360 escalado 2× pero los mockups ya están al doble. Recomendado: **mantener 1280×720**.

---

## 10. Referencias rápidas

- **Mockups:** [stitch_undertale_battle_interface/](stitch_undertale_battle_interface/)
- **HTML de referencia** (coordenadas exactas):
  - [save_slot_selection_screen_1/code.html](stitch_undertale_battle_interface/save_slot_selection_screen_1/code.html)
  - [battle_act_submenu/code.html](stitch_undertale_battle_interface/battle_act_submenu/code.html)
  - [battle_inventory_submenu/code.html](stitch_undertale_battle_interface/battle_inventory_submenu/code.html)
  - [game_over_screen/code.html](stitch_undertale_battle_interface/game_over_screen/code.html)
  - [victory_game_ending_screen/code.html](stitch_undertale_battle_interface/victory_game_ending_screen/code.html)
- **Design system:** [bit_quest_monolith/DESIGN.md](stitch_undertale_battle_interface/bit_quest_monolith/DESIGN.md)
- **Fuentes:** [Fuentes de texto/](Fuentes%20de%20texto/)
- **Plan general:** [INTERFAZ.md](INTERFAZ.md)
- **libGDX docs:** https://libgdx.com/wiki/

# Fase 3 — Base class para `*BossAssets`

**Objetivo:** Extraer el ciclo de vida común (`stateTime`, `playingOnce`, `currentAnim`, `update/render/play/playOnce/dispose`) de las tres clases de assets de boss a una clase abstracta. Las subclases sólo definen cómo cargan/dibujan sus frames.

**Riesgo:** Medio. Tres clases tocadas, cada una con su propio modelo de assets (spritesheet, layered, procedural). Hay que respetar las diferencias reales.

**Esfuerzo:** ~3 h.

---

## Diagnóstico

| Clase | LOC | Modelo de assets |
|---|---|---|
| [BossAssets](../src/main/java/com/rpg/ui/combat/BossAssets.java) | 121 | Spritesheet auto-split, 3 animaciones (idle/hurt/attack) |
| [KennyBossAssets](../src/main/java/com/rpg/ui/combat/KennyBossAssets.java) | 171 | Layers body+head, secuencias hardcoded `int[]` |
| [EclipseAssets](../src/main/java/com/rpg/ui/combat/EclipseAssets.java) | 182 | Procedural vía `Pixmap`, 11 texturas en runtime |

**Lo que tienen en común:**
- Campos `stateTime`, `playingOnce`, `currentAnim` (o equivalente).
- Métodos `update(delta)`, `play(String)`, `playOnce(String)`, `dispose()`.
- Lógica de "si una animación one-shot termina, volver a idle".

**Lo que NO tienen en común:**
- Origen de los frames (disk / generated).
- Composición (single layer / two layers).
- Política de `Animation.PlayMode`.

---

## Refactor propuesto

### Paso 1 — Crear `BaseBossAssets`

[src/main/java/com/rpg/ui/combat/BaseBossAssets.java](../src/main/java/com/rpg/ui/combat/) — abstract, implementa `BossRenderer`:

```java
abstract class BaseBossAssets implements BossRenderer {
    protected float stateTime;
    protected String currentAnim;
    protected boolean playingOnce;
    protected String fallbackAnim = "idle";

    public final void update(float delta) {
        stateTime += delta;
        if (playingOnce && isCurrentAnimationFinished()) {
            playingOnce = false;
            play(fallbackAnim);
        }
    }

    public final void play(String name) {
        if (!name.equals(currentAnim)) {
            currentAnim = name;
            stateTime = 0f;
            playingOnce = false;
        }
    }

    public final void playOnce(String name) {
        currentAnim = name;
        stateTime = 0f;
        playingOnce = true;
    }

    // Hooks de subclase
    protected abstract boolean isCurrentAnimationFinished();
    public abstract void render(SpriteBatch batch, float x, float y, float w, float h);
    public abstract void dispose();
}
```

### Paso 2 — Migrar `BossAssets`

- Borrar campos / métodos comunes que ahora viven en la base.
- Implementar `isCurrentAnimationFinished()` consultando el `Animation` actual.
- Mantener su lógica específica de carga de spritesheet.

### Paso 3 — Migrar `KennyBossAssets`

- Mismo procedimiento. La lógica body+head queda en `render()`.
- `isCurrentAnimationFinished()` mira si la secuencia hardcoded terminó.

### Paso 4 — Migrar `EclipseAssets`

- Mantener generación procedural en su constructor.
- `isCurrentAnimationFinished()` y `render()` según su modelo de frames.

---

## Verificación

- Smoke test con cada jefe: idle se reproduce en loop, hurt vuelve a idle al terminar, attack idem.
- No debe haber regresiones visuales: comparar antes/después.

## Decisiones a confirmar antes de empezar

- ¿`fallbackAnim` siempre es `"idle"` o algún boss usa otro? — revisar las tres clases.
- ¿Algún boss usa una animación cíclica distinta de `LOOP`? — si sí, parametrizar.

---

## Checklist de cierre

- [ ] `BaseBossAssets` creada e integrada.
- [ ] `BossAssets`, `KennyBossAssets`, `EclipseAssets` migradas, sin código duplicado del ciclo de vida.
- [ ] Sin cambios en `BossRenderer` (la interfaz pública se mantiene).
- [ ] Smoke test: los 3 jefes animan correctamente (idle/hurt/attack).
- [ ] `mvn compile` verde.
- [ ] Commit: `refactor: extract BaseBossAssets to share animation lifecycle (DRY)`.

**Impacto esperado:** −60/−80 LOC, futuras animaciones de boss requieren ~40% menos código.

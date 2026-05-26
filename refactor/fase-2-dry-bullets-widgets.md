# Fase 2 — DRY en patterns y widgets

**Objetivo:** Eliminar duplicación crítica en `CoinPattern`/`CoinDropPattern` y en la navegación de widgets de menú.

**Riesgo:** Bajo-Medio. Se introduce una clase base y un utilitario, pero el comportamiento de cada pattern/widget debe quedar idéntico — testear visualmente.

**Esfuerzo:** ~2 h.

---

## 2.1 — Deduplicar `CoinPattern` ↔ `CoinDropPattern` (CRÍTICA)

### Diagnóstico

Los dos archivos comparten ~95% de su código:

| Aspecto | [CoinPattern](../src/main/java/com/rpg/ui/combat/patterns/CoinPattern.java) | [CoinDropPattern](../src/main/java/com/rpg/ui/combat/patterns/CoinDropPattern.java) |
|---|---|---|
| `FRAME_PATHS` | Idéntico | Idéntico |
| Carga de texturas | Idéntico | Idéntico |
| `update()` loop | Idéntico | + limpieza fuera de bounds |
| `renderSprites()` | Idéntico | Idéntico |
| `dispose()` / `hasFrames()` | Idéntico | Idéntico |
| `SPAWN_INTERVAL` | 0.7 | 0.55 |
| `DURATION` | 10 | 10 |
| Spawn | 4 direcciones (laterales) | Vertical desde top |
| Rebote | (x=true, y=true) | (x=false, y=false) |

### Refactor propuesto

**Crear** [src/main/java/com/rpg/ui/combat/patterns/BaseCoinPattern.java](../src/main/java/com/rpg/ui/combat/patterns/) (abstract):

```java
abstract class BaseCoinPattern implements BulletPattern {
    protected static final ArrayList<String> FRAME_PATHS = ...;
    protected final ArrayList<CoinBullet> bullets = new ArrayList<>();
    protected final ArrayList<Texture> frames = new ArrayList<>();
    protected float elapsed, sinceSpawn;

    // hooks que las subclases definen
    protected abstract float spawnInterval();
    protected abstract float duration();
    protected abstract void spawnCoin(Soul soul, CombatBox box);
    protected abstract boolean shouldCullOutOfBounds();

    // implementación compartida
    public void update(...)    { /* loop común */ }
    public void renderSprites(...) { /* común */ }
    public void dispose()      { /* común */ }
    public boolean isFinished(){ return elapsed >= duration(); }
}
```

**Convertir** `CoinPattern` y `CoinDropPattern` en subclases delgadas (~30 LOC cada una) que sólo definan los hooks.

### Verificación

- Visual: lanzar el boss que usa cada patrón y confirmar que las balas se mueven igual.
- Tests: si existen, ejecutarlos.

### Impacto

- −120 LOC.
- Si en el futuro se añade `CoinSpiralPattern`, sólo hay que implementar 3 métodos.

---

## 2.2 — Extraer utilitario `GridNav` para navegación de menús

### Diagnóstico

Tres widgets implementan el mismo algoritmo de navegación 2D:

- [ActionMenu.navigate()](../src/main/java/com/rpg/ui/widgets/ActionMenu.java)
- [ActListWidget.navigate()](../src/main/java/com/rpg/ui/widgets/ActListWidget.java)
- [ItemListWidget.navigate()](../src/main/java/com/rpg/ui/widgets/ItemListWidget.java)

Cada uno hace:
```java
col = Math.floorMod(col + dx, COLS);
row = Math.floorMod(row + dy, maxRow + 1);
next = Math.min(row * COLS + col, items.size() - 1);
```

### Refactor propuesto

**Crear** `com.rpg.ui.widgets.GridNav` (clase final con métodos estáticos):

```java
public final class GridNav {
    private GridNav() {}

    /** Devuelve el nuevo índice tras moverse (dx, dy) en una grilla wrap-around. */
    public static int move(int currentIndex, int dx, int dy, int cols, int total) {
        int col = Math.floorMod(currentIndex % cols + dx, cols);
        int rows = (total + cols - 1) / cols;
        int row = Math.floorMod(currentIndex / cols + dy, rows);
        return Math.min(row * cols + col, total - 1);
    }
}
```

**Reemplazar** las tres implementaciones por `selectedIndex = GridNav.move(selectedIndex, dx, dy, COLS, items.size());`.

### Verificación

Probar manualmente:
- Menú principal de combate (FIGHT/ACT/ITEM/MERCY) navega correcto en 2x2.
- Lista de ACT navega en columna.
- Lista de ITEM navega y respeta el límite cuando hay items impares.

### Impacto

- −30 LOC.
- Comportamiento de navegación garantizadamente idéntico entre menús.

---

## Checklist de cierre

- [ ] `BaseCoinPattern` creada, `CoinPattern` y `CoinDropPattern` convertidas a subclases.
- [ ] `GridNav` creada, tres widgets refactorizados.
- [ ] Verificación visual: ambos patrones de moneda y los tres menús se comportan igual.
- [ ] `mvn compile` verde.
- [ ] Commit: `refactor: deduplicate coin patterns and menu navigation (DRY)`.

**Impacto esperado:** −150 LOC, comportamiento idéntico.

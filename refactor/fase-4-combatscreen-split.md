# Fase 4 — Dividir `CombatScreen` (KISS)

**Objetivo:** Romper la megaclase [CombatScreen.java](../src/main/java/com/rpg/ui/screens/CombatScreen.java) (~414 LOC, 6+ responsabilidades) en colaboradores enfocados. Mantener `CombatScreen` como **coordinador** delgado.

**Riesgo:** Alto. Es el corazón del gameplay. **Hacer en commits pequeños** y verificar después de cada extracción.

**Esfuerzo:** ~4 h.

---

## Diagnóstico de responsabilidades en `CombatScreen`

Actualmente acumula:

1. **Routing de input** del jugador (5 métodos dispersos).
2. **Transiciones de estado** del combate (4 métodos).
3. **Update/render del bullet hell** (3 métodos).
4. **Creación de patrones** — `switch` con 8 casos en `createPattern(String, Soul)`.
5. **Rendering UI** (7 secciones: HP bar, menú, lista de actos, lista de items, diálogo, boss, fondo).
6. **Detección de colisiones e i-frames**.

Métodos largos a partir:

- `createPattern()` — 23 líneas, 8 cases (líneas ~317-340).
- `updateBulletHell()` — 26 líneas, mezcla colisión + i-frames + muerte (líneas ~344-370).

---

## Refactor propuesto (orden de extracción)

### Paso 1 — Extraer `PatternFactory` (más fácil, más aislado)

**Crear** [src/main/java/com/rpg/ui/combat/patterns/PatternFactory.java](../src/main/java/com/rpg/ui/combat/patterns/):

```java
public final class PatternFactory {
    private PatternFactory() {}

    public static BulletPattern create(String id, Soul soul) {
        switch (id) {
            case "knight_rat":      return new KnightRatPattern(soul);
            case "cardbox_field":   return new CardboxFieldPattern(soul);
            case "orbital_ring":    return new OrbitalRingPattern(soul);
            case "van_traffic":     return new VanTrafficPattern(soul);
            case "coin":            return new CoinPattern(soul);
            case "coin_drop":       return new CoinDropPattern(soul);
            // ...
            default: throw new IllegalArgumentException("Unknown pattern: " + id);
        }
    }
}
```

**Sustituir** la llamada en `CombatScreen` por `PatternFactory.create(id, soul)` y borrar el switch.

**Commit:** `refactor: extract PatternFactory from CombatScreen`.

### Paso 2 — Extraer `BulletHellController`

Encapsula update + render + colisiones + i-frames + detección de muerte del bullet hell.

**Crear** [src/main/java/com/rpg/ui/combat/BulletHellController.java](../src/main/java/com/rpg/ui/combat/):

```java
public final class BulletHellController {
    private final Soul soul;
    private final CombatBox box;
    private BulletPattern current;
    private float invulnTimer;

    public BulletHellController(Soul soul, CombatBox box) { ... }

    public void startPattern(BulletPattern p)        { current = p; current.start(...); }
    public boolean isFinished()                      { return current != null && current.isFinished(); }
    public DamageEvent update(float delta)           { /* mueve bullets, detecta hits */ }
    public void renderShapes(ShapeRenderer sr)       { ... }
    public void renderSprites(SpriteBatch sb)        { ... }
    public void dispose()                            { if (current != null) current.dispose(); }
}
```

Donde `DamageEvent` es un record/clase simple: `(int damageDealt, boolean dead)`.

**Sustituir** en `CombatScreen` los campos `currentPattern`, `invulnTimer` etc. por un único `BulletHellController bh;`.

**Commit:** `refactor: extract BulletHellController from CombatScreen`.

### Paso 3 — Tabla de routing de input

Reemplazar los 5–6 métodos `if (state == X) handleX()` por un map:

```java
private final Map<CombatPhase, InputHandler> handlers = Map.of(
    CombatPhase.MENU,         this::handleMenuInput,
    CombatPhase.ACT_LIST,     this::handleActListInput,
    CombatPhase.ITEM_LIST,    this::handleItemListInput,
    CombatPhase.BULLET_HELL,  this::handleBulletHellInput,
    CombatPhase.DIALOGUE,     this::handleDialogueInput
);
```

Cada handler queda pequeño (10-20 LOC) y `keyDown(key)` se reduce a `handlers.get(currentPhase).handle(key);`.

**Commit:** `refactor: route combat input via handler map`.

### Paso 4 — (Opcional) Extraer `CombatHud`

Si tras los pasos anteriores `CombatScreen` sigue por encima de ~200 LOC, mover el rendering de HUD (HP bar, menú, listas, diálogo) a una clase `CombatHud` que reciba el estado y dibuje.

---

## Estado objetivo

```
CombatScreen (coordinador, ~150 LOC)
├── BulletHellController     — bullets, colisión, i-frames
├── PatternFactory           — string id → BulletPattern
├── CombatHud (opcional)     — render de UI
└── handlers map             — routing input por fase
```

Cada colaborador es testeable de forma aislada.

---

## Verificación

Test manual exhaustivo (la única manera de validar este refactor):

- [ ] Combate completo contra Kenny: FIGHT, ACT, ITEM, MERCY, todas las fases de balas, derrota y victoria.
- [ ] Combate completo contra Eclipse.
- [ ] Game over funciona, transición de pantalla.
- [ ] Victoria + post-boss screen.
- [ ] i-frames se aplican correctamente (probar dejarse pegar varias veces).

## Checklist de cierre

- [ ] `PatternFactory` extraído (commit 1).
- [ ] `BulletHellController` extraído (commit 2).
- [ ] Routing de input por tabla (commit 3).
- [ ] `CombatHud` extraído si aplica (commit 4).
- [ ] `CombatScreen` ≤ 200 LOC.
- [ ] Test manual completo verde.
- [ ] `mvn compile` verde.

**Impacto esperado:** `CombatScreen` pasa de 414 a ~150 LOC; cada colaborador es <120 LOC y testeable.

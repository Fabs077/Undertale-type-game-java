# MANUAL DEL JUEGO — Proyecto RPG (Estilo Undertale)

---

## 1. CÓMO INICIAR EL JUEGO

### Opción A — Ejecutar desde consola
```bat
run.bat
```
Compila y lanza la ventana del juego (1280×720).

### Opción B — Solo el motor de consola (tests)
```bat
run.bat console
```
Ejecuta los 120 tests del motor lógico y muestra el resultado en consola.

### Opción C — Maven directo
```
mvn compile exec:java
```

---

## 2. PANTALLAS Y FLUJO

```
Menú Principal
    │
    ├─ PLAY       → Pantalla de Diálogo (Fase 1 intro)
    │                       │
    │                       └─ [Z] → Pantalla de Combate
    │
    ├─ LOAD GAME  → Pantalla de Slots de Guardado
    │
    └─ EXIT       → Cierra el juego
```

---

## 3. CONTROLES

| Tecla       | Acción                                     |
|-------------|--------------------------------------------|
| Flecha ↑    | Mover cursor arriba / mover Alma arriba    |
| Flecha ↓    | Mover cursor abajo / mover Alma abajo      |
| Flecha ←    | Mover cursor izquierda / Alma izquierda    |
| Flecha →    | Mover cursor derecha / Alma derecha        |
| **Z**       | Confirmar selección / avanzar diálogo      |
| **X**       | Cancelar / volver al menú anterior         |
| **Escape**  | Abrir menú de pausa (Guardar & Salir)      |

---

## 4. MECÁNICAS DE COMBATE

Cada turno el jugador elige una de cuatro acciones en el menú inferior:

```
[ FIGHT ]  [ ACT ]  [ ITEM ]  [ MERCY ]
```

### FIGHT (Atacar)
- El jugador ataca al jefe con su ataque base (10 + bonus de arma).
- El daño se calcula: `max(1, ataque_total × multiplicador_QTE)`.
- Tras el ataque siempre viene el turno del enemigo (Bullet Hell).
- Si el jefe cae a 0 HP → victoria.

### ACT (Interactuar)
- Abre un submenú con 4 opciones: HABLAR, INSULTAR, ELOGIAR, BROMEAR.
- Cada ACT incrementa el contador interno del jefe (necesitas 3 para poder perdonarlo).
- El jefe responde con una línea de diálogo.
- Después del ACT siempre viene el turno del enemigo.

### ITEM (Ítem)
- Abre el inventario del jugador (cuadrícula 2×2).
- Selecciona un ítem con las flechas y confirma con Z.
- **Poción Menor**: restaura 20 HP (ítem de un solo uso).
- Después de usar un ítem siempre viene el turno del enemigo.

### MERCY (Misericordia)
- Intenta perdonar al jefe.
- **Condición**: debes haber realizado al menos 3 ACTs primero.
- Si se puede perdonar → el combate termina en paz (ruta pacífica).
- Si aún no se puede → el jefe se niega y ataca (Bullet Hell).

---

## 5. BULLET HELL (Turno del Enemigo)

Después de cada acción del jugador (excepto victoria o perdón), el jefe contraataca.

### Cómo funciona
1. La caja de combate se expande.
2. Aparece el **Alma** (corazón rojo) en el centro.
3. El jefe dispara balas en patrón radial (12 balas desde el centro).
4. Mueve el Alma con las **flechas** para esquivar.
5. El Bullet Hell dura **5 segundos**.

### Daño de las balas
- Cada bala que toca el Alma inflige: `max(1, ataque_del_jefe − tu_defensa)`.
- Con el jugador inicial (defensa 5) y el jefe base (ataque ~10): **5 HP por bala**.
- El daño se refleja en tiempo real en la barra de HP.

### Barra de HP
```
LV 1   HP   [████████░░]  80 / 100
```
- Si el HP llega a 0 → Game Over.

---

## 6. EL JEFE ACTUAL — "Sombra"

| Stat          | Valor base |
|---------------|------------|
| HP            | 80         |
| Ataque        | ~10        |
| Fase          | 1          |
| ACTs para perdón | 3       |

**Diálogos del jefe:**
- "¡No pasarás!"
- "Te arrepentirás..."
- "Interesante estrategia."

**Loot al derrotarlo:** Poción de Fase 1 (restaura 30 HP).

---

## 7. ESTADÍSTICAS DEL JUGADOR

| Stat          | Valor inicial |
|---------------|---------------|
| HP            | 100           |
| Ataque base   | 10            |
| Defensa base  | 5             |
| Inventario    | 1× Poción Menor (20 HP) |

---

## 8. RUTAS NARRATIVAS

El historial de decisiones determina la ruta al final de las 5 fases:

| Ruta       | Condición                              |
|------------|----------------------------------------|
| PACIFISTA  | Todos los jefes perdonados (ACT + MERCY) |
| NEUTRAL    | Mezcla de combates y perdones          |
| GENOCIDA   | Todos los jefes derrotados en combate  |

La ruta afecta el diálogo de StoryGenerator entre fases.

---

## 9. SISTEMA DE GUARDADO

- **3 slots** de guardado disponibles (accesibles desde LOAD GAME en el menú).
- Los archivos se guardan en: `saves/slot_1.json`, `saves/slot_2.json`, `saves/slot_3.json`.
- Durante el combate: **Escape** → "SAVE & EXIT" guarda y regresa al menú principal.

---

## 10. FLUJO COMPLETO DE UN TURNO

```
[Menú de acción]
       │
       ├─ FIGHT / ACT / ITEM / MERCY
       │
       ↓
[Diálogo con resultado de la acción]
  "Presiona Z para continuar"
       │
       ↓
[Bullet Hell — esquiva las balas con las flechas]
  (5 segundos)
       │
       ↓
[Vuelve al menú de acción]
       │
       ├─ Si HP del jefe = 0 → VICTORIA
       ├─ Si jefe perdonado → VICTORIA (ruta pacífica)
       └─ Si HP del jugador = 0 → GAME OVER
```

---

## 11. FASES DEL JUEGO

El juego tiene **5 fases**. Cada fase presenta un nuevo jefe con stats escalados:

| Fase | Multiplicador HP | Multiplicador Ataque |
|------|-----------------|---------------------|
| 1    | ×1.25           | ×1.15               |
| 2    | ×1.50           | ×1.30               |
| 3    | ×1.75           | ×1.45               |
| 4    | ×2.00           | ×1.60               |
| 5    | ×2.25           | ×1.75               |

Antes de cada combate aparece una pantalla de diálogo narrativo (intro de fase).

---

## 12. ESTADO ACTUAL DEL JUEGO

### Lo que funciona
- Menú principal → navegar con flechas, confirmar con Z
- PLAY inicia el juego correctamente (Fase 1)
- Intro de diálogo antes del combate
- Combate completo: FIGHT, ACT (submenú), ITEM (inventario), MERCY
- Barra de HP actualizada en tiempo real
- Bullet Hell visual con detección de colisiones y daño real
- Game Over si el HP llega a 0
- Victoria al derrotar al jefe o perdonarlo

### Pendiente / Placeholders
- Pantallas de Victory y Game Over son placeholders (regresan al menú)
- Los sprites del jefe son una cuadrícula verde (placeholder)
- No hay música ni efectos de sonido
- El sistema de guardado está conectado a UI pero sin persistencia al disco
- Las fases 2-5 no tienen jefes registrados (requiere BossFactory poblado)
- ItemListWidget y ActListWidget muestran datos de ejemplo, no inventario real del juego

---

## 13. ARQUITECTURA INTERNA (referencia técnica)

```
Motor (com.rpg.engine)          UI (com.rpg.ui)
──────────────────────          ────────────────
Player, Boss, Enemy             CombatScreen
CombatManager                   DialogueScreen
FightAction / ActAction /       MainMenuScreen
ItemAction / MercyAction        SaveSlotScreen
PlayerMenuState                 CombatController  ← puente
EnemyBulletHellState            Soul, Bullet
BossFactory, PhaseManager       BulletPattern
HistoryManager, StoryGenerator  HpBar, ActionMenu
```

El **CombatController** es el único puente entre la UI y el motor. La UI nunca accede directamente a `CombatManager`.

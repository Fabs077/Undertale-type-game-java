# ProyectoPrograII — Motor RPG estilo Undertale

## Visión general
Motor de juego **puramente lógico** en Java para un RPG por turnos inspirado en Undertale: combate FIGHT/ACT/ITEM/MERCY, generación procedimental de jefes a lo largo de 5 fases, sistema de botín polimórfico y rutas narrativas (pacífica / neutral / genocida) derivadas del historial de decisiones.

El objetivo intermedio es un **MVP lógico excelente** validable con consola y JUnit. La UI se construye en **libGDX 1.12.1** (backend LWJGL3), en el paquete `com.rpg.ui` separado del motor. **Cero imports** de `com.badlogic.gdx.*` en el código del motor (`com.rpg.engine.*`).

## Stack
- **Lenguaje:** Java (recomendado JDK 17+).
- **Tests:** JUnit 5.
- **Persistencia:** JSON en memoria (Jackson o Gson, encapsulado en una sola capa de serialización).
- **UI:** libGDX 1.12.1 + LWJGL3 — paquete `com.rpg.ui`, separado del motor lógico.

## Arquitectura

### Paquetes
```
com.rpg.engine
├── core
│   ├── interfaces        Persistable, CombatAction, CombatState
│   └── exceptions        SaveCorruptionException, ResourceNotFoundException
├── entities              Character (abs), Enemy (abs), Boss, Player
├── items                 Item (abs), Equipment (abs), Weapon, Armor, Consumable
├── combat
│   ├── actions           FightAction, ActAction, ItemAction, MercyAction
│   ├── states            PlayerMenuState, PlayerQteState, EnemyBulletHellState
│   └── CombatManager, ActionResult
└── procedural
    ├── BossFactory
    ├── PhaseManager      (Persistable)
    ├── HistoryManager    (Persistable) + DecisionRecord
    └── StoryGenerator    + Route enum
```

### Patrones de diseño y cómo dialogan
- **Command** (`CombatAction`): cada acción del jugador se cosifica. `execute(CombatManager)` retorna un `ActionResult` — la acción **nunca** llama `changeState`.
- **State** (`CombatState`): ciclo de vida `onEnter` / `update` / `handleAction` / `onExit`. El State filtra qué Commands son válidos y, leyendo el `ActionResult`, decide la transición.
- **Factory + Prototype** (`BossFactory`): mantiene un `Map<Integer, List<Boss>>` de prototipos; al pedir un jefe, **clona** el prototipo (vía `Boss.copy()`) y delega el escalado al propio jefe (`Boss.scaleToPhase(int)`). Nunca devuelve la instancia del pool.
- **Persistable**: aplicado solo a los dueños de estado de larga vida — `Player`, `PhaseManager`, `HistoryManager`. Las entidades de combate son ephemerales, no persisten.

### Reglas de encapsulamiento (antifragilidad)
- `Character.hp` se modifica **solo** vía `takeDamage` / `heal`, ambos con clamp.
- `CombatManager.changeState` es el **único** punto de transición de estado.
- Excepciones checked (`SaveCorruptionException`, `ResourceNotFoundException`) se lanzan **solo en bordes** (persistencia, factory).
- Validaciones de invariantes en el método dueño (constructor, setter, `loadData`).
- Copias defensivas en colecciones que entran al constructor; vistas inmutables (`Collections.unmodifiableList`) en getters de colecciones.

## Workflow de implementación

**Implementar por capas, esperando confirmación entre cada una.** No saltar adelante.

1. Capa 1 — interfaces (`Persistable`, `CombatAction`, `CombatState`) + excepciones.
2. Capa 2 — entidades (`Character`, `Enemy`, `Boss`, `Player`).
3. Capa 3 — ítems (`Item`, `Equipment`, `Weapon`, `Armor`, `Consumable`).
4. Capa 4 — combate (`ActionResult`, acciones, estados, `CombatManager`).
5. Capa 5 — procedural (`BossFactory`, `PhaseManager`, `HistoryManager`, `StoryGenerator`).

El plan vivo con la responsabilidad de cada método está en
`C:\Users\f4b10\.claude\plans\act-a-como-un-arquitecto-nifty-bengio.md`.

## Verificación
- JUnit 5 por capa: `CharacterTest`, `PlayerTest`, `BossTest`, `BossFactoryTest` (con `Random` de seed fija), `CombatManagerTest`, `HistoryManagerTest` + `StoryGeneratorTest`, `PersistableRoundtripTest`.
- Driver de consola desechable (`Main.java`) para inspección manual de un combate completo.
- Smoke run de 5 fases con RNG seedeado.

## Convenciones
- Sin UI / sin `System.out` dentro del motor (excepto el driver de consola explícito).
- Sin librerías de UI/gráficos en `com.rpg.engine.*`.
- Comentarios solo donde el "porqué" no sea evidente del nombre.
- Nombres en inglés para el código; mensajes/diálogos pueden ser en español.

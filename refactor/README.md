# Plan de Refactor — ProyectoPrograII

Plan dividido en **5 fases** para limpiar y ordenar el código aplicando **KISS, DRY y YAGNI**, sin romper funcionalidad. Cada fase es independiente y verificable; ejecutar en orden recomendado (riesgo creciente).

## Principios guía

- **KISS** — Keep It Simple, Stupid: simplificar métodos largos, megaclases y switch gigantes.
- **DRY** — Don't Repeat Yourself: extraer lógica duplicada a base classes / utilidades.
- **YAGNI** — You Aren't Gonna Need It: eliminar código muerto, parámetros sin uso y abstracciones especulativas.

## Estado actual (auditoría)

| Métrica | Valor |
|---|---|
| Archivos `.java` | 79 |
| Líneas totales | ~6.700 |
| Código muerto detectado | ~380 líneas |
| Duplicación detectada | ~200 líneas |
| Megaclase principal | [CombatScreen.java](../src/main/java/com/rpg/ui/screens/CombatScreen.java) (414 LOC) |
| Violaciones regla de capas | 0 ✓ (engine 100% libre de libGDX) |

## Fases

| # | Archivo | Foco | Riesgo | Esfuerzo |
|---|---------|------|--------|----------|
| 1 | [fase-1-limpieza.md](fase-1-limpieza.md) | Eliminar código muerto y no usado | Bajo | ~30 min |
| 2 | [fase-2-dry-bullets-widgets.md](fase-2-dry-bullets-widgets.md) | Deduplicar CoinPattern / navigate() | Bajo-Medio | ~2 h |
| 3 | [fase-3-assets-base-class.md](fase-3-assets-base-class.md) | Base class para `*BossAssets` | Medio | ~3 h |
| 4 | [fase-4-combatscreen-split.md](fase-4-combatscreen-split.md) | Dividir megaclase `CombatScreen` | Alto | ~4 h |
| 5 | [fase-5-organizacion-naming.md](fase-5-organizacion-naming.md) | Reorganizar paquetes y naming | Bajo | ~1 h |

**Total estimado:** 10–12 h de trabajo enfocado.

## Reglas de ejecución

1. **Una fase a la vez.** Confirmar verde antes de pasar a la siguiente.
2. **Tests primero.** Antes de cada fase, ejecutar `ConsoleTests` y abrir el juego para verificar estado base.
3. **Commit por fase.** Cada fase = 1 commit (o varios, pero coherentes).
4. **No mezclar refactor con features.** Si aparece un bug ajeno, abrir issue/nota — no arreglar en el mismo commit.
5. **Respetar la regla de capas:** `com.rpg.engine.*` no puede importar `com.badlogic.gdx.*`. Verificar con grep antes de cerrar fase.

## Verificación post-fase

Después de cada fase, ejecutar:

```powershell
# Verificar que engine no contamine con libGDX
Select-String -Path src\main\java\com\rpg\engine\**\*.java -Pattern "com.badlogic.gdx"

# Compilar
mvn compile

# Smoke test manual: lanzar DesktopLauncher, jugar 1 combate completo
```

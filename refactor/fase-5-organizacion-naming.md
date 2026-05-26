# Fase 5 — Organización de paquetes y naming

**Objetivo:** Reorganizar paquetes para que la estructura refleje el dominio, y unificar naming inconsistente. Cambios puramente cosméticos: ningún cambio de comportamiento.

**Riesgo:** Bajo (pero alto en conflictos de merge si hay ramas vivas — coordinar).

**Esfuerzo:** ~1 h.

---

## 5.1 — Mover bullets a su propio sub-paquete

### Diagnóstico

`com.rpg.ui.combat` mezcla:
- Bullets concretos (`CardboxBullet`, `KnightRatBullet`, `CoinBullet`, `VanBullet`, `OrbitalBullet`, `Bullet`).
- Interfaces y assets (`BulletPattern`, `BossAssets`, `BossRenderer`, `CombatBox`, `Soul`).

`com.rpg.ui.combat.patterns` ya separa los patrones; los bullets deberían tener su equivalente.

### Acción

Crear `com.rpg.ui.combat.bullets` y mover allí:

- [Bullet.java](../src/main/java/com/rpg/ui/combat/Bullet.java)
- [CardboxBullet.java](../src/main/java/com/rpg/ui/combat/CardboxBullet.java)
- [KnightRatBullet.java](../src/main/java/com/rpg/ui/combat/KnightRatBullet.java)
- [CoinBullet.java](../src/main/java/com/rpg/ui/combat/CoinBullet.java)
- [VanBullet.java](../src/main/java/com/rpg/ui/combat/VanBullet.java)
- [OrbitalBullet.java](../src/main/java/com/rpg/ui/combat/OrbitalBullet.java)

Usar refactor del IDE (no `git mv` manual) para que actualice los imports automáticamente.

### Estado resultante

```
com.rpg.ui.combat/
├── bullets/        — clases concretas de balas
├── patterns/       — BulletPattern + implementaciones
├── BossAssets, KennyBossAssets, EclipseAssets, BaseBossAssets
├── BossRenderer
├── CombatBox, Soul
```

## 5.2 — Renombrar `EclipseAssets` → `EclipseBossAssets`

### Diagnóstico

Inconsistencia:
- `BossAssets` (genérico, sin prefijo).
- `KennyBossAssets` (patrón `<Nombre>BossAssets`).
- `EclipseAssets` (rompe el patrón).

### Acción

Refactor del IDE: `EclipseAssets` → `EclipseBossAssets`.

Verificar que el archivo correspondiente y todos los usos quedan actualizados.

## 5.3 — Verificar imports tras los movimientos

Ejecutar:

```powershell
Select-String -Path src\main\java\**\*.java -Pattern "import com.rpg.ui.combat\." | Select-Object Path, Line
```

y revisar que no haya imports rotos.

Verificación crítica de la regla de capas:

```powershell
Select-String -Path src\main\java\com\rpg\engine\**\*.java -Pattern "com.badlogic.gdx"
```

Debe devolver **0 líneas**.

---

## 5.4 — (Opcional) Documentación viva

Actualizar [CLAUDE.md](../CLAUDE.md) sección "Paquetes" para reflejar el nuevo layout:

```
com.rpg.ui
├── combat
│   ├── bullets   ← NUEVO
│   ├── patterns
│   ├── BaseBossAssets, BossAssets, KennyBossAssets, EclipseBossAssets ← renombrado
│   └── ...
```

---

## Checklist de cierre

- [ ] 6 archivos de bullets movidos a `com.rpg.ui.combat.bullets`.
- [ ] `EclipseAssets` renombrado a `EclipseBossAssets`.
- [ ] Todos los imports actualizados (verificado con grep).
- [ ] Regla de capas se mantiene: `engine` no importa `gdx`.
- [ ] `mvn compile` verde.
- [ ] Smoke test: el juego arranca y un combate corre normal.
- [ ] CLAUDE.md actualizado.
- [ ] Commit: `chore: reorganize combat bullets package and unify asset naming`.

**Impacto esperado:** Estructura más clara, naming consistente; 0 cambio funcional.

# Fase 1 — Limpieza de código muerto (YAGNI)

**Objetivo:** Eliminar archivos, clases y métodos que no se referencian desde ningún sitio. Reducir ruido sin tocar comportamiento.

**Riesgo:** Bajo. Sólo se borra código probadamente no usado (verificado con grep en todo `src/main/java`).

**Esfuerzo:** ~30 min.

---

## 1.1 — Eliminar `RadialSpreadPattern.java`

- **Archivo:** [src/main/java/com/rpg/ui/combat/patterns/RadialSpreadPattern.java](../src/main/java/com/rpg/ui/combat/patterns/RadialSpreadPattern.java)
- **Estado:** ~90 líneas, nunca instanciado. No aparece en el `switch` de `CombatScreen.createPattern(...)`.
- **Acción:** `git rm` del archivo.
- **Verificación:** `grep -r "RadialSpreadPattern" src/` → 0 resultados después.

## 1.2 — Mover `ConsoleTests.java` fuera de `main`

- **Archivo:** [src/main/java/com/rpg/engine/ConsoleTests.java](../src/main/java/com/rpg/engine/ConsoleTests.java) (~280 líneas)
- **Estado:** Driver de consola para inspección manual. CLAUDE.md lo describe como "desechable". No se referencia desde producción.
- **Decisión a tomar:**
  - **Opción A (recomendada(Usaremos esta)):** Mover a `src/test/java/com/rpg/engine/ConsoleTests.java` para mantenerlo disponible pero fuera del jar de runtime.
  - **Opción B:** Borrarlo si ya hay tests JUnit equivalentes (verificar primero).
- **Verificación:** Re-ejecutar `mvn compile` y `java com.rpg.ui.DesktopLauncher` sigue funcionando.

## 1.3 — Eliminar `ItemListWidget.getSelectedName()`

- **Archivo:** [src/main/java/com/rpg/ui/widgets/ItemListWidget.java:61](../src/main/java/com/rpg/ui/widgets/ItemListWidget.java#L61)
- **Estado:** Método público, 0 callers. `CombatScreen` usa sólo `getSelectedIndex()`.
- **Acción:** Borrar el método.

## 1.4 — Barrido de imports no usados

Después de los borrados anteriores, ejecutar limpieza de imports en todo el módulo:

- IDE: **Ctrl+Shift+O** (Eclipse) / **Ctrl+Alt+O** (IntelliJ) sobre cada archivo modificado.
- Alternativa CLI: configurar `maven-source-plugin` o usar `google-java-format`.

## 1.5 — Auditoría rápida de campos privados sin uso

Revisar manualmente con el IDE (warnings "unused field") en estos archivos sospechosos:

- [CombatScreen.java](../src/main/java/com/rpg/ui/screens/CombatScreen.java)
- [CombatManager.java](../src/main/java/com/rpg/engine/combat/CombatManager.java)
- [Boss.java](../src/main/java/com/rpg/engine/entities/Boss.java)

Sólo borrar lo que el IDE marque inequívocamente como no usado.

---

## Checklist de cierre

- [ ] `RadialSpreadPattern.java` eliminado.
- [ ] `ConsoleTests.java` movido a `src/test/java/` o eliminado.
- [ ] `ItemListWidget.getSelectedName()` eliminado.
- [ ] Imports limpiados.
- [ ] `mvn compile` verde.
- [ ] Smoke test: lanzar el juego, completar 1 combate.
- [ ] Commit: `chore: remove dead code (RadialSpreadPattern, ConsoleTests, unused methods)`.

**Impacto esperado:** −380 LOC, sin cambio funcional.

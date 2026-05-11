package com.rpg.engine.combat.actions;

import com.rpg.engine.combat.ActionResult;
import com.rpg.engine.combat.CombatManager;
import com.rpg.engine.core.interfaces.CombatAction;
import com.rpg.engine.entities.Boss;

/**
 * Acción MERCY: el jugador intenta perdonar al boss.
 *
 * Solo tiene éxito si boss.canBeSpared() == true (el jugador realizó suficientes ACTs).
 * Si el boss no acepta la misericordia, el turno pasa igualmente al boss
 * (el jugador "perdió" su turno intentando perdonar sin éxito).
 *
 * En caso de éxito, marca boss.setSpared(true) y devuelve combatEnded=true.
 * CombatManager.handleVictoryLootDrop() registrará wasSpared=true en HistoryManager,
 * lo que influirá en la ruta narrativa hacia PACIFIC.
 */
public class MercyAction implements CombatAction {

    @Override
    public ActionResult execute(CombatManager ctx) {
        Boss boss = ctx.getCurrentBoss();

        if (boss.canBeSpared()) {
            boss.setSpared(true);
            return ActionResult.combatEnd(
                String.format("¡%s acepta tu misericordia! El combate termina en paz.", boss.getName())
            );
        }

        return ActionResult.miss(
            String.format("%s no está listo para ser perdonado aún. ¡Interactúa más con él (ACT)!",
                boss.getName())
        );
    }

    @Override
    public String getName() { return "Mercy"; }
}

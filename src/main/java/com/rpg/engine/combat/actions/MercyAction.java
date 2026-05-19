package com.rpg.engine.combat.actions;

import com.rpg.engine.combat.ActionResult;
import com.rpg.engine.combat.CombatManager;
import com.rpg.engine.core.interfaces.CombatAction;
import com.rpg.engine.entities.Boss;

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

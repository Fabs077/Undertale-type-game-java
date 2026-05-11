package com.rpg.engine.combat.actions;

import com.rpg.engine.combat.ActionResult;
import com.rpg.engine.combat.CombatManager;
import com.rpg.engine.core.interfaces.CombatAction;
import com.rpg.engine.entities.Boss;
import com.rpg.engine.entities.Player;

/**
 * Acción FIGHT: el jugador ataca al boss.
 *
 * Daño = max(1, totalAttack × qteMultiplier).
 * El multiplicador QTE lo establece PlayerQteState antes de que esta acción ejecute;
 * si no hay QTE activo, el Manager devuelve 1.0 y el daño es el ataque base.
 * Tras leer el multiplicador lo resetea a 1.0 para que no persista en el siguiente turno.
 *
 * Esta acción NUNCA llama changeState() — le devuelve el ActionResult al State
 * y éste decide si transicionar a EnemyBulletHellState.
 */
public class FightAction implements CombatAction {

    @Override
    public ActionResult execute(CombatManager ctx) {
        Player player = ctx.getPlayer();
        Boss   boss   = ctx.getCurrentBoss();

        double multiplier = ctx.getQteMultiplier();
        int    damage     = Math.max(1, (int) Math.round(player.getTotalAttack() * multiplier));
        ctx.resetQteMultiplier();

        boss.takeDamage(damage);

        if (!boss.isAlive()) {
            return ActionResult.of(damage, 0,
                String.format("¡Atacas a %s por %d de daño! ¡Lo has derrotado!", boss.getName(), damage),
                true, true);
        }

        return ActionResult.of(damage, 0,
            String.format("Atacas a %s por %d de daño. (HP: %d/%d)",
                boss.getName(), damage, boss.getHp(), boss.getMaxHp()),
            true, false);
    }

    @Override
    public String getName() { return "Fight"; }
}

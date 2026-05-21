package com.rpg.engine.combat.states;

import com.rpg.engine.combat.ActionResult;
import com.rpg.engine.combat.CombatManager;
import com.rpg.engine.core.interfaces.CombatAction;
import com.rpg.engine.core.interfaces.CombatState;

/**
 * Estado del turno del boss: marca que el bullet-hell está en curso.
 *
 * Ciclo de vida:
 *   onEnter → pide el id del patrón al boss y lo guarda. El daño lo aplica la UI
 *             (CombatController.applyBulletHit()) bala a bala durante la secuencia real.
 *   update  → llamado por notifyBulletHellComplete() cuando la UI termina el patrón;
 *             transiciona de vuelta a PlayerMenuState.
 *   handleAction → no-op: el jugador no elige acciones mientras el boss ataca.
 *   onExit  → no-op.
 */
public class EnemyBulletHellState implements CombatState {

    private String activePattern;  // id del patrón en curso; la UI lo lee para instanciar el patrón correcto

    @Override
    public void onEnter(CombatManager ctx) {
        activePattern = ctx.getCurrentBoss().executeBulletHellPattern();
    }

    @Override
    public void update(CombatManager ctx) {
        // Llamado por CombatController.notifyBulletHellComplete() una vez que la UI termina.
        // Si el player murió durante el bullet-hell no transicionamos: isCombatOver() lo detecta.
        if (ctx.getPlayer().isAlive()) {
            ctx.changeState(new PlayerMenuState());
        }
    }

    @Override
    public ActionResult handleAction(CombatAction action, CombatManager ctx) {
        return ActionResult.noEffect(""); // no player actions during boss attack
    }

    @Override
    public void onExit(CombatManager ctx) { }

    @Override
    public String getName() { return "EnemyBulletHell"; }

    /** @return id del patrón bullet-hell activo; la UI lo usa para instanciar el patrón correcto */
    public String getActivePattern() { return activePattern; }
}

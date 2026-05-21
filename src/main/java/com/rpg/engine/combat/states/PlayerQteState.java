package com.rpg.engine.combat.states;

import com.rpg.engine.combat.ActionResult;
import com.rpg.engine.combat.CombatManager;
import com.rpg.engine.combat.actions.FightAction;
import com.rpg.engine.core.interfaces.CombatAction;
import com.rpg.engine.core.interfaces.CombatState;

/**
 * Estado de Quick Time Event (QTE) durante el ataque del jugador.
 *
 * Flujo:
 *   PlayerMenuState recibe FIGHT → transiciona a PlayerQteState.
 *   PlayerQteState abre una ventana de QTE_WINDOW ticks.
 *   Si el jugador envía FightAction durante la ventana → multiplicador 1.5×.
 *   Si el timer llega a 0 sin input → multiplicador base (1.0), igual transiciona.
 *
 * En ambos casos el siguiente estado es EnemyBulletHellState.
 *
 * En FXGL el timer se mapea a frames de animación; en tests el driver llama
 * tick() o handleAction(new FightAction()) para simular el input.
 *
 * Nota: solo acepta FightAction — cualquier otra acción se descarta.
 */
public class PlayerQteState implements CombatState {

    private static final double PERFECT_MULTIPLIER = 1.5;
    private static final int    QTE_WINDOW         = 5;   // ticks disponibles para reaccionar

    private int     timerTicks;
    private boolean qteResolved;

    @Override
    public void onEnter(CombatManager ctx) {
        timerTicks  = QTE_WINDOW;
        qteResolved = false;
    }

    @Override
    public void update(CombatManager ctx) {
        if (qteResolved) return;
        timerTicks--;
        if (timerTicks <= 0) {
            // Tiempo agotado: ataque sin bonus
            qteResolved = true;
            resolveAttack(ctx);
        }
    }

    /**
     * Solo reacciona a FightAction. El resto de acciones se ignoran durante el QTE.
     * Si es FightAction aplica el multiplicador de QTE perfecto y ejecuta el ataque.
     */
    @Override
    public ActionResult handleAction(CombatAction action, CombatManager ctx) {
        if (qteResolved || !(action instanceof FightAction)) return ActionResult.noEffect("");

        qteResolved = true;
        ctx.setQteMultiplier(PERFECT_MULTIPLIER);
        resolveAttack(ctx);
        return ActionResult.noEffect("QTE resuelto.");
    }

    private void resolveAttack(CombatManager ctx) {
        ActionResult result = new FightAction().execute(ctx);
        if (result.isCombatEnded()) return;
        if (result.isTurnEnded()) ctx.changeState(new EnemyBulletHellState());
    }

    @Override
    public void onExit(CombatManager ctx) {
        ctx.resetQteMultiplier();
    }

    @Override
    public String getName() { return "PlayerQTE"; }
}

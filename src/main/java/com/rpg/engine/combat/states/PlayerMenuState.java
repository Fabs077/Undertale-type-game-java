package com.rpg.engine.combat.states;

import com.rpg.engine.combat.ActionResult;
import com.rpg.engine.combat.CombatManager;
import com.rpg.engine.core.interfaces.CombatAction;
import com.rpg.engine.core.interfaces.CombatState;

/**
 * Estado de selección de acción del jugador: FIGHT / ACT / ITEM / MERCY.
 *
 * Es el estado "raíz" del ciclo de combate. Acepta cualquiera de las cuatro acciones
 * y reacciona al ActionResult que devuelve la acción ejecutada:
 *
 *   combatEnded = true  → el combate terminó (boss muerto o perdonado).
 *                         El bucle de juego detecta isCombatOver() y llama handleVictoryLootDrop().
 *   turnEnded   = true  → el turno del jugador terminó; transiciona a EnemyBulletHellState.
 *
 * update() es no-op: este estado espera input externo (el jugador o el driver de tests).
 */
public class PlayerMenuState implements CombatState {

    @Override
    public void onEnter(CombatManager ctx) {
        // FXGL: mostrar el menú de acciones
        // Backend: no-op — el driver o el test elige la siguiente acción
    }

    @Override
    public void update(CombatManager ctx) {
        // no-op — el menú espera input, no avanza por su cuenta
    }

    @Override
    public void handleAction(CombatAction action, CombatManager ctx) {
        if (ctx.isCombatOver()) return;

        ActionResult result = action.execute(ctx);

        if (result.isCombatEnded()) {
            // Nada más que hacer aquí — el bucle externo llama handleVictoryLootDrop()
            return;
        }

        if (result.isTurnEnded()) {
            ctx.changeState(new EnemyBulletHellState());
        }
    }

    @Override
    public void onExit(CombatManager ctx) { }

    @Override
    public String getName() { return "PlayerMenu"; }
}

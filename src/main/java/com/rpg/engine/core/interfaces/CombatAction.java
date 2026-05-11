package com.rpg.engine.core.interfaces;

import com.rpg.engine.combat.ActionResult;
import com.rpg.engine.combat.CombatManager;

/**
 * Patrón Command aplicado al combate.
 *
 * Cada acción del jugador (FIGHT, ACT, ITEM, MERCY) se convierte en un objeto
 * que implementa esta interfaz. Esto permite:
 *   - Desacoplar quién decide la acción (el menú/input) de quién la ejecuta.
 *   - Que el CombatState filtre qué acciones son válidas en cada momento.
 *   - Registrar o deshacer acciones en el futuro.
 *
 * Flujo Command + State:
 *   CombatManager.executeAction(action)
 *     → currentState.handleAction(action, ctx)    ← el State filtra
 *       → action.execute(ctx)                      ← el Command actúa
 *         → devuelve ActionResult                  ← el State reacciona y transiciona
 *
 * IMPORTANTE: execute() NUNCA llama ctx.changeState() directamente.
 * La transición de estado es responsabilidad exclusiva del CombatState que recibe el ActionResult.
 */
public interface CombatAction {

    /**
     * Ejecuta la acción en el contexto del combate actual.
     * Lee y muta el estado a través de la API pública de CombatManager.
     *
     * @param ctx el gestor de combate con acceso a Player, Boss y estado actual
     * @return ActionResult describiendo qué ocurrió (daño, mensaje, si el turno terminó, etc.)
     */
    ActionResult execute(CombatManager ctx);

    /**
     * Nombre identificador de la acción para logs y futura UI.
     *
     * @return nombre legible, ej. "Fight", "Mercy", "Use Potion"
     */
    String getName();
}

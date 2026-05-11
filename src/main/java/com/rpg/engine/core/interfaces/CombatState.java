package com.rpg.engine.core.interfaces;

import com.rpg.engine.combat.CombatManager;

/**
 * Patrón State aplicado al combate por turnos.
 *
 * Los estados concretos son:
 *   - PlayerMenuState     : el jugador elige FIGHT / ACT / ITEM / MERCY
 *   - PlayerQteState      : ventana de Quick Time Event durante un ataque
 *   - EnemyBulletHellState: el boss ejecuta su patrón de proyectiles
 *
 * Ciclo de vida de un estado (4 hooks):
 *
 *   [Estado anterior] ──onExit()──> cambio ──onEnter()──> [Estado nuevo]
 *                                                              │
 *                                                          update() ← ticks del bucle de juego
 *                                                              │
 *                                                       handleAction() ← input del jugador
 *
 * Contrato:
 *   - onEnter/onExit se llaman exactamente UNA vez por transición.
 *   - update() se llama cada tick del bucle lógico (o manualmente en tests).
 *   - handleAction() filtra si la acción es válida en este estado;
 *     si lo es, llama action.execute(ctx) y reacciona al ActionResult.
 */
public interface CombatState {

    /**
     * Inicializa el estado al entrar: arranca timers, siembra patrones, resetea contadores.
     *
     * @param ctx el gestor de combate que provee acceso a Player y Boss
     */
    void onEnter(CombatManager ctx);

    /**
     * Tick lógico del estado. Llamado repetidamente por el bucle de juego.
     * Ejemplos: decrementar timer de QTE, avanzar proyectiles del bullet-hell.
     *
     * @param ctx el gestor de combate
     */
    void update(CombatManager ctx);

    /**
     * Filtra y despacha una acción del jugador.
     * Si la acción es válida en este estado, la ejecuta con action.execute(ctx) y
     * reacciona al ActionResult (potencialmente transicionando de estado).
     * Si no es válida (ej. intentar FIGHT durante el turno del boss), la descarta.
     *
     * @param action la acción propuesta por el jugador
     * @param ctx    el gestor de combate
     */
    void handleAction(CombatAction action, CombatManager ctx);

    /**
     * Limpieza al salir del estado: cancela timers, libera referencias temporales.
     *
     * @param ctx el gestor de combate
     */
    void onExit(CombatManager ctx);

    /**
     * Nombre del estado para depuración y logging.
     *
     * @return nombre legible, ej. "PlayerMenu", "EnemyBulletHell"
     */
    String getName();
}

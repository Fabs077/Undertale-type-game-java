package com.rpg.engine.combat;

import com.rpg.engine.core.interfaces.CombatAction;
import com.rpg.engine.core.interfaces.CombatState;
import com.rpg.engine.entities.Boss;
import com.rpg.engine.entities.Player;
import com.rpg.engine.items.Item;
import com.rpg.engine.procedural.HistoryManager;

/**
 * Gestor central del combate por turnos.
 *
 * Tres invariantes que nadie fuera de esta clase debe romper:
 *   1. changeState() es el ÚNICO punto de transición — ningún State ni Action lo llama directamente.
 *   2. executeAction() delega al State activo — el Manager no decide nada sobre la acción.
 *   3. handleVictoryLootDrop() es idempotente — puede llamarse varias veces sin duplicar loot.
 *
 * Flujo típico de un turno:
 *   executeAction(FightAction)
 *     → currentState.handleAction(fight, this)   [PlayerMenuState]
 *       → fight.execute(this)                    → ActionResult
 *       → si turnEnded: changeState(EnemyBulletHellState)
 *           → boss aplica daño al player
 *           → changeState(PlayerMenuState)   ← de vuelta al menú
 */
public class CombatManager {

    private final Player player;
    private final Boss currentBoss;
    private final HistoryManager historyManager;

    private CombatState currentState;
    private double qteMultiplier; // FightAction lo lee; PlayerQteState lo setea
    private boolean lootHandled;  // garantiza idempotencia de handleVictoryLootDrop()

    public CombatManager(Player player, Boss boss,
                         CombatState initialState, HistoryManager historyManager) {
        this.player          = player;
        this.currentBoss     = boss;
        this.historyManager  = historyManager;
        this.qteMultiplier   = 1.0;
        this.lootHandled     = false;
        this.currentState    = initialState;
        initialState.onEnter(this);
    }

    // -------------------------------------------------------------------------
    // API principal
    // -------------------------------------------------------------------------

    /**
     * Punto de entrada para las acciones del jugador.
     * Delega al State actual — éste decide si la acción es válida en este momento.
     */
    public void executeAction(CombatAction action) {
        currentState.handleAction(action, this);
    }

    /**
     * Único punto de transición de estado del combate.
     * Llama onExit() sobre el estado saliente y onEnter() sobre el entrante.
     */
    public void changeState(CombatState newState) {
        currentState.onExit(this);
        currentState = newState;
        currentState.onEnter(this);
    }

    /**
     * Avanza un tick lógico del estado actual.
     * El bucle de juego (o el driver de tests) lo llama repetidamente.
     */
    public void tick() {
        currentState.update(this);
    }

    /**
     * Recoge el loot del boss y lo añade al inventario del player.
     * Registra la decisión en el historial para que StoryGenerator calcule la ruta.
     * Idempotente: llamadas repetidas no duplican loot ni registros.
     */
    public void handleVictoryLootDrop() {
        if (lootHandled) return;
        Item loot = currentBoss.dropLoot();
        if (loot != null) player.addLoot(loot);
        historyManager.recordDecision(currentBoss, currentBoss.isSpared());
        lootHandled = true;
    }

    /**
     * El combate termina cuando el boss está muerto, fue perdonado, o el player murió.
     */
    public boolean isCombatOver() {
        return !currentBoss.isAlive() || currentBoss.isSpared() || !player.isAlive();
    }

    // -------------------------------------------------------------------------
    // QTE (Quick Time Event)
    // -------------------------------------------------------------------------

    /** @return multiplicador de daño activo (1.0 por defecto, > 1.0 si QTE exitoso) */
    public double getQteMultiplier() { return qteMultiplier; }

    /** Llamado por PlayerQteState cuando el jugador reacciona a tiempo. */
    public void setQteMultiplier(double multiplier) {
        this.qteMultiplier = Math.max(1.0, multiplier);
    }

    /** FightAction lo llama justo después de leer el multiplicador. */
    public void resetQteMultiplier() { this.qteMultiplier = 1.0; }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Player getPlayer()             { return player; }
    public Boss getCurrentBoss()          { return currentBoss; }
    public CombatState getCurrentState()  { return currentState; }
    public HistoryManager getHistoryManager() { return historyManager; }
}

package com.rpg.engine.combat;

import com.rpg.engine.core.interfaces.CombatAction;
import com.rpg.engine.core.interfaces.CombatState;
import com.rpg.engine.entities.Boss;
import com.rpg.engine.entities.Player;
import com.rpg.engine.items.Item;
import com.rpg.engine.procedural.HistoryManager;

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

    public void executeAction(CombatAction action) {
        currentState.handleAction(action, this);
    }

    public void changeState(CombatState newState) {
        currentState.onExit(this);
        currentState = newState;
        currentState.onEnter(this);
    }

    public void tick() {
        currentState.update(this);
    }


    public void handleVictoryLootDrop() {
        if (lootHandled) return;
        Item loot = currentBoss.dropLoot();
        if (loot != null) player.addLoot(loot);
        historyManager.recordDecision(currentBoss, currentBoss.isSpared());
        lootHandled = true;
    }

    public boolean isCombatOver() {
        return !currentBoss.isAlive() || currentBoss.isSpared() || !player.isAlive();
    }

    // -------------------------------------------------------------------------
    // QTE (Quick Time Event)
    // -------------------------------------------------------------------------

    public double getQteMultiplier() {
    	return qteMultiplier; 
    	}

    public void setQteMultiplier(double multiplier) {
        this.qteMultiplier = Math.max(1.0, multiplier);
    }

    public void resetQteMultiplier() {
    	this.qteMultiplier = 1.0;
    	}

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Player getPlayer()             { 
    	return player;
    	}
    public Boss getCurrentBoss()          { 
    	return currentBoss;
    	}
    public CombatState getCurrentState()  { 
    	return currentState;
    	}
    public HistoryManager getHistoryManager() { 
    	return historyManager;
    	}
}

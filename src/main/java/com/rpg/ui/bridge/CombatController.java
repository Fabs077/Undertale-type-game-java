package com.rpg.ui.bridge;

import com.rpg.engine.combat.ActionResult;
import com.rpg.engine.combat.CombatManager;
import com.rpg.engine.combat.actions.ActAction;
import com.rpg.engine.combat.actions.FightAction;
import com.rpg.engine.combat.actions.ItemAction;
import com.rpg.engine.combat.actions.MercyAction;
import com.rpg.engine.combat.states.EnemyBulletHellState;
import com.rpg.engine.combat.states.PlayerMenuState;
import com.rpg.engine.entities.Boss;
import com.rpg.engine.entities.Player;
import com.rpg.engine.items.Consumable;
import com.rpg.engine.items.Item;
import com.rpg.engine.procedural.HistoryManager;

import java.util.ArrayList;
import java.util.List;

public final class CombatController {

    private final Player         player;
    private final Boss           boss;
    private final HistoryManager historyManager;
    private final CombatManager  manager;
    private final int            phase;

    /** Partida nueva con valores por defecto. */
    public CombatController() {
        this(new Player("Jugador", 100, 10, 5), new HistoryManager(), 1);
    }

    /** Partida cargada desde un save. */
    public CombatController(Player player, HistoryManager history, int phase) {
        this.player         = player;
        this.historyManager = history;
        this.phase          = phase;

        player.addLoot(new Consumable("pocion_menor", "Poción Menor", "Restaura 20 HP.", 20));

        boss = new Boss("Sombra", 80, 1.0, 1,
            List.of("¡No pasarás!", "Te arrepentirás...", "Interesante estrategia."));

        manager = new CombatManager(player, boss, new PlayerMenuState(), historyManager);
    }

    public ActionResult executeFight() {
        return manager.executeAction(new FightAction());
    }

    public ActionResult executeAct(String actType) {
        return manager.executeAction(new ActAction(actType));
    }

    public ActionResult executeItem(int index) {
        ArrayList<Item> inv = player.getInventory();
        if (inv.isEmpty()) {
            return ActionResult.noEffect("¡No tienes ítems en el inventario!");
        }
        int safeIdx = Math.min(index, inv.size() - 1);
        return manager.executeAction(new ItemAction(inv.get(safeIdx)));
    }

    public ActionResult executeMercy() {
        return manager.executeAction(new MercyAction());
    }

    /** Aplica un golpe de bala al jugador y devuelve el daño infligido. */
    public int applyBulletHit() {
        int damage = Math.max(1, boss.computeAttackPower() - player.getTotalDefense());
        player.takeDamage(damage);
        return damage;
    }

    /**
     * @return id del patrón bullet-hell que el boss eligió; null si el engine no está en ese estado.
     */
    public String getActivePatternId() {
        if (manager.getCurrentState() instanceof EnemyBulletHellState e) {
            return e.getActivePattern();
        }
        return null;
    }

    /**
     * Llamado por la UI cuando el bullet-hell termina.
     * Hace tick al engine para que EnemyBulletHellState transicione de vuelta a PlayerMenuState.
     */
    public void notifyBulletHellComplete() {
        manager.tick();
    }

    public int getPlayerHp()                  { return player.getHp(); }
    public int getPlayerMaxHp()               { return player.getMaxHp(); }
    public String getBossName()               { return boss.getName(); }
    public String getBossSpriteId()           { return boss.getSpriteId(); }
    public boolean isPlayerAlive()            { return player.isAlive(); }
    public boolean isCombatOver()             { return manager.isCombatOver(); }

    public Player         getPlayer()         { return player; }
    public HistoryManager getHistoryManager() { return historyManager; }
    public int            getPhase()          { return phase; }
}

package com.rpg.ui.bridge;

import com.rpg.engine.combat.ActionResult;
import com.rpg.engine.combat.CombatManager;
import com.rpg.engine.combat.actions.ActAction;
import com.rpg.engine.combat.actions.FightAction;
import com.rpg.engine.combat.actions.ItemAction;
import com.rpg.engine.combat.actions.MercyAction;
import com.rpg.engine.combat.states.PlayerMenuState;
import com.rpg.engine.entities.Boss;
import com.rpg.engine.entities.Player;
import com.rpg.engine.items.Consumable;
import com.rpg.engine.items.Item;
import com.rpg.engine.procedural.HistoryManager;

import java.util.ArrayList;
import java.util.List;

public final class CombatController {

    private final Player player;
    private final Boss boss;
    private final CombatManager manager;

    public CombatController() {
        player = new Player("Jugador", 100, 10, 5);
        player.addLoot(new Consumable("pocion_menor", "Poción Menor", "Restaura 20 HP.", 20));

        boss = new Boss("Sombra", 80, 1.0, 1,
            List.of("¡No pasarás!", "Te arrepentirás...", "Interesante estrategia."));

        manager = new CombatManager(player, boss, new PlayerMenuState(), new HistoryManager());
    }

    public ActionResult executeFight() {
        return new FightAction().execute(manager);
    }

    public ActionResult executeAct(String actType) {
        return new ActAction(actType).execute(manager);
    }

    public ActionResult executeItem(int index) {
        ArrayList<Item> inv = player.getInventory();
        if (inv.isEmpty()) {
            return ActionResult.noEffect("¡No tienes ítems en el inventario!");
        }
        int safeIdx = Math.min(index, inv.size() - 1);
        return new ItemAction(inv.get(safeIdx)).execute(manager);
    }

    public ActionResult executeMercy() {
        return new MercyAction().execute(manager);
    }

    /** Aplica un golpe de bala al jugador y devuelve el daño infligido. */
    public int applyBulletHit() {
        int damage = Math.max(1, boss.computeAttackPower() - player.getTotalDefense());
        player.takeDamage(damage);
        return damage;
    }

    public int getPlayerHp()       { return player.getHp(); }
    public int getPlayerMaxHp()    { return player.getMaxHp(); }
    public String getBossName()    { return boss.getName(); }
    public boolean isPlayerAlive() { return player.isAlive(); }
    public boolean isCombatOver()  { return manager.isCombatOver(); }
}

package com.rpg.engine.combat.actions;

import com.rpg.engine.combat.ActionResult;
import com.rpg.engine.combat.CombatManager;
import com.rpg.engine.core.interfaces.CombatAction;
import com.rpg.engine.items.Item;

public class ItemAction implements CombatAction {

    private final Item item;

    public ItemAction(Item item) {
        if (item == null) throw new IllegalArgumentException("item cannot be null");
        this.item = item;
    }

    @Override
    public ActionResult execute(CombatManager ctx) {
        int hpBefore = ctx.getPlayer().getHp();
        ctx.getPlayer().useItem(item);
        int healed = ctx.getPlayer().getHp() - hpBefore;

        String message = healed > 0
            ? String.format("Usas %s y recuperas %d HP. (HP: %d/%d)",
                item.getName(), healed,
                ctx.getPlayer().getHp(), ctx.getPlayer().getMaxHp())
            : "Usas " + item.getName() + ".";

        return ActionResult.noEffect(message);
    }

    @Override
    public String getName() { return "Item [" + item.getName() + "]"; }
}

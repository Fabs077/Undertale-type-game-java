package com.rpg.engine.core.interfaces;

import com.rpg.engine.combat.ActionResult;
import com.rpg.engine.combat.CombatManager;

public interface CombatState {

    void onEnter(CombatManager ctx);
    void update(CombatManager ctx);

    /** Processes a player action and returns its result. States that reject all actions return {@code ActionResult.noEffect("")}. */
    ActionResult handleAction(CombatAction action, CombatManager ctx);

    void onExit(CombatManager ctx);
    String getName();
}

package com.rpg.engine.core.interfaces;

import com.rpg.engine.combat.ActionResult;
import com.rpg.engine.combat.CombatManager;

public interface CombatAction {

    ActionResult execute(CombatManager ctx);

    String getName();
}

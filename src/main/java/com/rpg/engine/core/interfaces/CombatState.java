package com.rpg.engine.core.interfaces;

import com.rpg.engine.combat.CombatManager;


public interface CombatState {


    public void onEnter(CombatManager ctx);
    public void update(CombatManager ctx);
    public void handleAction(CombatAction action, CombatManager ctx);
    public void onExit(CombatManager ctx);

    String getName();
}

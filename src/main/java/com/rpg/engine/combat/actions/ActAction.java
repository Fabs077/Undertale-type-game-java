package com.rpg.engine.combat.actions;

import com.rpg.engine.combat.ActionResult;
import com.rpg.engine.combat.CombatManager;
import com.rpg.engine.core.interfaces.CombatAction;

/**
 * Acción ACT: el jugador interactúa con el boss sin atacarlo.
 *
 * Cada uso incrementa el contador actsPerformed del Boss.
 * Cuando ese contador supera el umbral, canBeSpared() devuelve true
 * y MercyAction puede terminar el combate pacíficamente.
 *
 * El tipo de ACT ("hablar", "halagar", "bromear") es narrativo — el motor
 * lo registra igual, pero StoryGenerator puede usar el tipo para generar diálogos distintos.
 */
public class ActAction implements CombatAction {

    private final String actType;

    public ActAction(String actType) {
        this.actType = (actType != null && !actType.isBlank()) ? actType : "hablar";
    }

    @Override
    public ActionResult execute(CombatManager ctx) {
        ctx.getCurrentBoss().registerActAttempt();
        int acts  = ctx.getCurrentBoss().getActsPerformed();
        int total = 3; // umbral base de canBeSpared() en Boss

        String bossLine = ctx.getCurrentBoss().nextDialogue();
        String message = String.format(
            "Decides %s con %s. (%d/%d para poder perdonar)%s",
            actType, ctx.getCurrentBoss().getName(), acts, total,
            bossLine.isBlank() ? "" : "\n  > " + bossLine
        );

        return ActionResult.noEffect(message);
    }

    @Override
    public String getName() { return "Act [" + actType + "]"; }
}

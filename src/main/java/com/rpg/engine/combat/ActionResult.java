package com.rpg.engine.combat;


public final class ActionResult {

    private final int damageDealt;   // daño infligido al enemigo en este turno
    private final int damageTaken;   // daño recibido por el jugador en este turno
    private final String message;    // descripción narrativa del resultado
    private final boolean turnEnded; // true → el turno del jugador terminó, pasa al boss
    private final boolean combatEnded; // true → el combate terminó (victoria o derrota)

    private ActionResult(int damageDealt, int damageTaken,
                         String message, boolean turnEnded, boolean combatEnded) {
        this.damageDealt  = damageDealt;
        this.damageTaken  = damageTaken;
        this.message      = message;
        this.turnEnded    = turnEnded;
        this.combatEnded  = combatEnded;
    }

    // --- Métodos de fábrica ---

    public static ActionResult of(int damageDealt, int damageTaken,
                                   String message, boolean turnEnded, boolean combatEnded) {
        return new ActionResult(damageDealt, damageTaken, message, turnEnded, combatEnded);
    }

    public static ActionResult miss(String message) {
        return new ActionResult(0, 0, message, true, false);
    }

    public static ActionResult combatEnd(String message) {
        return new ActionResult(0, 0, message, true, true);
    }

    public static ActionResult noEffect(String message) {
        return new ActionResult(0, 0, message, true, false);
    }

    // --- Getters ---

    public int getDamageDealt()   { return damageDealt; }
    public int getDamageTaken()   { return damageTaken; }
    public String getMessage()    { return message; }
    public boolean isTurnEnded()  { return turnEnded; }
    public boolean isCombatEnded(){ return combatEnded; }

    @Override
    public String toString() {
        return String.format(
            "ActionResult{dmgDealt=%d, dmgTaken=%d, msg='%s', turnEnded=%b, combatEnded=%b}",
            damageDealt, damageTaken, message, turnEnded, combatEnded
        );
    }
}

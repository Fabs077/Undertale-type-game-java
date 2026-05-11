package com.rpg.engine.combat.states;

import com.rpg.engine.combat.CombatManager;
import com.rpg.engine.core.interfaces.CombatAction;
import com.rpg.engine.core.interfaces.CombatState;

/**
 * Estado del turno del boss: ejecuta su patrón bullet-hell y aplica daño al player.
 *
 * Ciclo de vida:
 *   onEnter → pide el patrón al boss y lo guarda. NO aplica daño (el daño se resuelve
 *             en update para que executeAction() retorne antes del ataque del boss).
 *   update  → aplica el daño del boss al player y transiciona a PlayerMenuState.
 *             (En FXGL este método avanzaría frame a frame la animación del patrón.)
 *   handleAction → no-op: el jugador no elige acciones durante el ataque del boss.
 *   onExit  → no-op.
 *
 * Daño aplicado = max(0, boss.computeAttackPower() − player.getTotalDefense()).
 * Si el daño mitigado es 0 el player esquivó completamente (por alta defensa).
 * Si el player muere, el bucle externo detecta isCombatOver() y termina la partida.
 */
public class EnemyBulletHellState implements CombatState {

    private boolean attackResolved; // true una vez que update() aplicó el daño
    private String  activePattern;  // id del patrón en curso; FXGL lo lee para la animación

    @Override
    public void onEnter(CombatManager ctx) {
        attackResolved = false;
        activePattern  = ctx.getCurrentBoss().executeBulletHellPattern();
        // FXGL: iniciar animación del patrón aquí — el daño se aplica en update()
        // para que executeAction() retorne antes de que el boss ataque.
    }

    @Override
    public void update(CombatManager ctx) {
        if (attackResolved) return; // guarda contra llamadas dobles

        int raw      = ctx.getCurrentBoss().computeAttackPower();
        int mitigated = Math.max(0, raw - ctx.getPlayer().getTotalDefense());
        if (mitigated > 0) {
            ctx.getPlayer().takeDamage(mitigated);
        }
        attackResolved = true;

        // Si el player murió no transicionamos: el bucle externo detecta isCombatOver()
        if (!ctx.getPlayer().isAlive()) return;

        ctx.changeState(new PlayerMenuState());
    }

    @Override
    public void handleAction(CombatAction action, CombatManager ctx) {
        // El jugador no elige acciones durante el ataque del boss.
        // Una futura DodgeAction podría aceptarse aquí para reducir el daño.
    }

    @Override
    public void onExit(CombatManager ctx) { }

    @Override
    public String getName() { return "EnemyBulletHell"; }

    /** @return id del patrón bullet-hell activo; FXGL lo usa para elegir la animación */
    public String getActivePattern() { return activePattern; }
}

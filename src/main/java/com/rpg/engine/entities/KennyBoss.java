package com.rpg.engine.entities;

import java.util.List;
import java.util.Random;

/**
 * Jefe de Fase 1 — antagonista inicial. Combate en dos fases internas al estilo "Sans":
 *
 *   FASE 1 (Red Soul):  KnightRat y Cardbox en alternancia. Cuando el contador de evasión
 *                       llega a 10 empiezan a aparecer turnos mixtos (rata+monedas o
 *                       furgonetas+monedas). A 5 esquives restantes se añade un 3er tipo.
 *   FASE 2 (Blue Soul): VanTraffic — el jugador debe saltar para esquivar las furgonetas.
 *
 * La transición a Fase 2 se activa si se cumple CUALQUIERA de estas condiciones:
 *   - El jugador ha sobrevivido {@value #TURNS_TO_PHASE2} turnos de bullet-hell, O
 *   - El HP de Kenny cae por debajo del {@value #HP_PHASE2_RATIO}% de su máximo.
 *
 * Evasión: Kenny esquiva las primeras {@value #EVASION_START} veces que el jugador usa FIGHT.
 *          Cuando el contador llega a 0 está fatigado y los ataques conectan normalmente.
 */
public class KennyBoss extends Boss {

    private static final int   EVASION_START   = 15;
    private static final int   TURNS_TO_PHASE2 = 5;
    private static final float HP_PHASE2_RATIO = 0.50f;


    private final Random rng = new Random();

    private int     evasionCount    = EVASION_START;
    private int     turnCount       = 0;   // bullet hells completados por el jugador
    private int     kennyPhase      = 1;   // fase interna del combate (1 o 2)
    private boolean phase2Triggered = false;

    public KennyBoss(int phaseLevel) {
        super("Kenny", 80, 1.0, phaseLevel, List.of(
            "¡No pasarás!",
            "Te arrepentirás de esto...",
            "¿Es eso todo lo que tienes?",
            "¡Eres bastante resistente!",
            "Muy bien... ¡sin piedad desde ahora!"
        ));
    }

    // ── Evasion ────────────────────────────────────────────────────────────

    /**
     * Si quedan esquives, decrementa el contador y retorna true (ataque cancelado).
     * En turnCount==0, el jugador ve "MISS" y Kenny contra-ataca de todos modos.
     */
    @Override
    public boolean tryEvade() {
        if (evasionCount > 0) {
            evasionCount--;
            return true;
        }
        return false;
    }

    // ── Phase management ───────────────────────────────────────────────────

    /**
     * Llamado por CombatController cada vez que el jugador sobrevive un bullet-hell.
     * Puede activar la transición a Fase 2 si se cumple alguna condición.
     */
    public void notifyTurnComplete() {
        turnCount++;
        if (!phase2Triggered && shouldTransitionToPhase2()) {
            kennyPhase      = 2;
            phase2Triggered = true;
        }
    }

    private boolean shouldTransitionToPhase2() {
        float hpRatio = (getMaxHp() > 0) ? (float) getHp() / getMaxHp() : 0f;
        return hpRatio < HP_PHASE2_RATIO || turnCount >= TURNS_TO_PHASE2;
    }

    // ── Bullet hell pattern ─────────────────────────────────────────────────

    /**
     * Escalada de patrones según evasionCount:
     *  > 10 → 1 tipo (alternancia simple, sin gravedad)
     * <= 10 → 2 tipos; aleatorio gravedad o sin gravedad
     * <=  5 → 3 tipos; aleatorio gravedad o sin gravedad
     * Fase 2 permanente → siempre con gravedad, mismo escalado.
     */
    @Override
    public String executeBulletHellPattern() {
        boolean useGravity = (kennyPhase >= 2) || (evasionCount <= 10 && rng.nextBoolean());

        if (evasionCount <= 5) return useGravity ? "kenny_g3" : "kenny_ng3";
        if (evasionCount <= 10) return useGravity ? "kenny_g2" : "kenny_ng2";

        // evasionCount > 10: solo ratas (cajas solo aparecen en mezclas a partir de <=10)
        return "kenny_rat";
    }

    // ── Boss contract ───────────────────────────────────────────────────────

    @Override
    public String getSpriteId() { return "BossKenny"; }

    @Override
    public Boss copy() { return new KennyBoss(getPhaseLevel()); }

    // ── Getters ─────────────────────────────────────────────────────────────

    public boolean isInPhase2()  { return kennyPhase >= 2; }
    public int getEvasionCount() { return evasionCount; }
    public int getTurnCount()    { return turnCount; }
}

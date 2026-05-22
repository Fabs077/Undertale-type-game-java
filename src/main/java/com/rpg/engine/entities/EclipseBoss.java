package com.rpg.engine.entities;

import java.util.List;

/**
 * Jefe de la Fase 2 — entidad del vacío cósmico.
 *
 * Patrón bullet-hell: "orbital_ring"
 *   Cuatro anillos concéntricos de balas que orbitan el centro de la arena
 *   a velocidades y direcciones distintas, generando un laberinto giratorio.
 *
 * Condición de MERCY distinta: requiere 3 ACTs Y tener HP <= 50%.
 */
public class EclipseBoss extends Boss {

    public EclipseBoss(int phaseLevel) {
        super("Eclipse", 130, 1.4, phaseLevel, List.of(
            "¿Qué buscas aquí, mortal?",
            "La oscuridad es el destino de todas las cosas.",
            "Nada escapa a la gravedad del vacío.",
            "Si verdaderamente eres libre... pruébalo.",
            "El silencio entre las estrellas... ese soy yo."
        ));
    }

    /** Eclipse puede perdonarse solo si además tiene <= 50% HP. */
    @Override
    public boolean canBeSpared() {
        return super.canBeSpared() && getHp() <= getMaxHp() / 2;
    }

    @Override
    public String executeBulletHellPattern() { return "orbital_ring"; }

    @Override
    public String getSpriteId() { return "BossEclipse"; }

    @Override
    public Boss copy() { return new EclipseBoss(getPhaseLevel()); }
}

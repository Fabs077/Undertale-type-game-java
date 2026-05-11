package com.rpg.engine.entities;

/**
 * Clase abstracta para todos los enemigos del juego.
 *
 * Añade el concepto de dificultad escalable: baseDifficultyModifier actúa como
 * multiplicador que afecta cuánto daño hace el enemigo y cómo escalan sus stats.
 *
 * 1.0 = dificultad base, 2.0 = el doble de poderoso, etc.
 *
 * computeAttackPower() es abstracto para que cada enemigo defina su fórmula
 * de daño sin que FightAction necesite saber qué tipo de enemigo es.
 */
public abstract class Enemy extends Character {

    protected double baseDifficultyModifier;

    protected Enemy(String name, int maxHp, double baseDifficultyModifier) {
        super(name, maxHp);
        if (baseDifficultyModifier <= 0)
            throw new IllegalArgumentException(
                "baseDifficultyModifier must be > 0, got: " + baseDifficultyModifier);
        this.baseDifficultyModifier = baseDifficultyModifier;
    }

    /**
     * Calcula el poder de ataque base del enemigo para este turno.
     * FightAction lo usa para determinar el daño a aplicar al Player,
     * descontando la defensa del Player (getTotalDefense()).
     *
     * Cada subclase define su propia fórmula.
     *
     * @return potencia de ataque del enemigo (entero, siempre ≥ 0)
     */
    public abstract int computeAttackPower();

    /** @return el modificador de dificultad base de este enemigo */
    public double getBaseDifficultyModifier() {
        return baseDifficultyModifier;
    }
}

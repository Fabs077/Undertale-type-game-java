package com.rpg.engine.entities;

/**
 * Raíz abstracta de todos los personajes del juego (jugador y enemigos).
 *
 * Encapsulamiento estricto: hp NUNCA se modifica directamente desde fuera.
 * Toda modificación de HP pasa por takeDamage() o heal(), que garantizan
 * los invariantes del sistema (0 ≤ hp ≤ maxHp) sin que el caller deba recordarlo.
 *
 * La mitigación de daño (defensa) NO ocurre aquí — Character recibe HP crudo.
 * FightAction calcula el daño mitigado antes de llamar takeDamage().
 */
public abstract class Character {

    protected String name;
    protected int hp;
    protected int maxHp;

    protected Character(String name, int maxHp) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name cannot be blank");
        if (maxHp <= 0)
            throw new IllegalArgumentException("maxHp must be > 0, got: " + maxHp);

        this.name  = name;
        this.maxHp = maxHp;
        this.hp    = maxHp; // todo personaje nace con HP completo
    }

    /**
     * Aplica daño crudo al personaje. El HP nunca baja de 0.
     * Valores ≤ 0 se ignoran para evitar "daño negativo" como heal encubierto.
     *
     * @param amount cantidad de daño a aplicar (debe ser > 0 para tener efecto)
     */
    public void takeDamage(int amount) {
        if (amount <= 0) return;
        this.hp = Math.max(0, this.hp - amount);
    }

    /**
     * Cura al personaje. El HP nunca supera maxHp.
     * Valores ≤ 0 se ignoran.
     *
     * @param amount cantidad de HP a restaurar
     */
    public void heal(int amount) {
        if (amount <= 0) return;
        this.hp = Math.min(this.maxHp, this.hp + amount);
    }

    /**
     * Punto único de verdad sobre si el personaje está vivo.
     * Nadie compara hp > 0 directamente fuera de esta clase.
     *
     * @return true si hp > 0
     */
    public boolean isAlive() {
        return this.hp > 0;
    }

    // --- Getters (no hay setters públicos para hp) ---

    /**
     * Cambia el nombre del personaje en tiempo de ejecución.
     * Útil para interacciones narrativas: un boss que revela su nombre verdadero,
     * una transformación, un alias que cambia según la ruta.
     *
     * @param name el nuevo nombre (no puede ser vacío)
     */
    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name cannot be blank");
        this.name = name;
    }

    public String getName()  {
        return name;
    }
    public int getHp()       { 
        return hp; 
    }
    public int getMaxHp()    { 
        return maxHp; 
    }

    @Override
    public String toString() {
        return String.format("%s[name=%s, hp=%d/%d]",
                getClass().getSimpleName(), name, hp, maxHp);
    }
}

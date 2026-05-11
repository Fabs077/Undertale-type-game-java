package com.rpg.engine.items;

/**
 * Equipamiento: ítems que modifican las estadísticas del jugador de forma permanente.
 * No se consumen al usarse — quedan en el slot de equipo del Player.
 *
 * Subclases:
 *   Weapon → incrementa el ataque total del Player
 *   Armor  → incrementa la defensa total del Player
 *
 * statBonus es inmutable: una espada +10 siempre da +10, no cambia.
 */
public abstract class Equipment extends Item {

    protected final int statBonus; // bonus de estadística que aporta al equiparse

    protected Equipment(String id, String name, String description, int statBonus) {
        super(id, name, description);
        if (statBonus < 0) throw new IllegalArgumentException("statBonus must be >= 0, got: " + statBonus);
        this.statBonus = statBonus;
    }

    /**
     * El equipamiento no se agota: permanece en el slot de equipo del Player
     * y se desplaza al inventario solo cuando otro equipo lo reemplaza.
     *
     * @return false siempre
     */
    @Override
    public boolean isSingleUse() {
        return false;
    }

    /** @return bonus de estadística (ataque para Weapon, defensa para Armor) */
    public int getStatBonus() {
        return statBonus;
    }
}

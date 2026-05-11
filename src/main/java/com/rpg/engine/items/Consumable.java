package com.rpg.engine.items;

import com.rpg.engine.entities.Character;

/**
 * Ítem consumible: se usa una vez y se agota del inventario.
 *
 * Efecto: cura healAmount de HP al objetivo.
 * La eliminación del inventario la hace Player.useItem() consultando isSingleUse(),
 * no el Consumable mismo — el ítem no "sabe" que vive en un inventario.
 *
 * Para curar en combate: ItemAction construye un ItemAction(pocion) y llama
 * player.useItem(pocion), que internamente llama pocion.use(player) → player.heal(healAmount).
 */
public class Consumable extends Item {

    private final int healAmount; // HP restaurado al usarse (siempre > 0)

    public Consumable(String id, String name, String description, int healAmount) {
        super(id, name, description);
        if (healAmount <= 0)
            throw new IllegalArgumentException("healAmount must be > 0, got: " + healAmount);
        this.healAmount = healAmount;
    }

    /**
     * Cura al objetivo por healAmount de HP.
     * Funciona sobre cualquier Character (Player o, potencialmente, un aliado futuro).
     *
     * @param target el personaje a curar
     */
    @Override
    public void use(Character target) {
        target.heal(healAmount);
    }

    /**
     * Los consumibles se agotan tras un uso.
     *
     * @return true siempre
     */
    @Override
    public boolean isSingleUse() {
        return true;
    }

    /** @return cantidad de HP que restaura este consumible */
    public int getHealAmount() {
        return healAmount;
    }
}

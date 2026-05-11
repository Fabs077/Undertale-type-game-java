package com.rpg.engine.items;

import com.rpg.engine.entities.Character;
import com.rpg.engine.entities.Player;

/**
 * Armadura: equipamiento que incrementa la defensa total del Player.
 * Comportamiento análogo a Weapon pero sobre el slot de armor del Player.
 */
public class Armor extends Equipment {

    public Armor(String id, String name, String description, int defenseBonus) {
        super(id, name, description, defenseBonus);
    }

    /**
     * Equipa esta armadura en el Player, desplazando la anterior al inventario.
     *
     * @param target el personaje objetivo (solo tiene efecto si es Player)
     */
    @Override
    public void use(Character target) {
        if (!(target instanceof Player p)) return;

        Armor previous = p.equipArmor(this);
        if (previous != null) {
            p.addLoot(previous);
        }
    }
}

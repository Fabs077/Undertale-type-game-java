package com.rpg.engine.items;

import com.rpg.engine.entities.Character;
import com.rpg.engine.entities.Player;

/**
 * Arma: equipamiento que incrementa el ataque total del Player.
 *
 * Al "usarse" (Player.useItem(weapon)), se llama use(player) que:
 *   1. Llama player.equipWeapon(this) — retorna la anterior.
 *   2. Si había una anterior, la regresa al inventario del Player.
 *
 * Si el objetivo no es un Player, no-op silencioso (las armas no se
 * "usan" sobre enemigos directamente; el daño lo calcula FightAction).
 */
public class Weapon extends Equipment {

    public Weapon(String id, String name, String description, int attackBonus) {
        super(id, name, description, attackBonus);
    }

    /**
     * Equipa esta arma en el Player, desplazando la anterior al inventario.
     *
     * @param target el personaje objetivo (solo tiene efecto si es Player)
     */
    @Override
    public void use(Character target) {
        if (!(target instanceof Player p)) return;

        Weapon previous = p.equipWeapon(this);
        if (previous != null) {
            // el arma desplazada vuelve al inventario para no perderse
            p.addLoot(previous);
        }
    }
}

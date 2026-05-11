package com.rpg.engine.items;

import com.rpg.engine.entities.Character;

import java.util.Objects;

/**
 * Raíz de la jerarquía de ítems. Clase abstracta — no se instancia directamente.
 *
 * Subclases concretas:
 *   Equipment (abstract) → Weapon, Armor
 *   Consumable
 *
 * El polimorfismo aquí: use(Character) tiene una implementación distinta en cada subclase,
 * pero el caller (Player.useItem) no necesita saber con qué tipo de ítem está tratando.
 *
 * Inmutabilidad parcial: id, name y description son final (identidad del ítem).
 * El estado mutable (cantidad de usos, etc.) va en subclases si se necesita.
 */
public abstract class Item {

    protected final String id;          // identificador único, usado en persistencia
    protected final String name;        // nombre visible
    protected final String description; // descripción narrativa

    protected Item(String id, String name, String description) {
        this.id          = Objects.requireNonNull(id,          "id cannot be null");
        this.name        = Objects.requireNonNull(name,        "name cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
    }

    /**
     * Aplica el efecto del ítem sobre el objetivo.
     * Cada subclase define su propio comportamiento:
     *   Consumable → cura HP
     *   Weapon     → se equipa en el Player
     *   Armor      → se equipa en el Player
     *
     * @param target el personaje sobre el que se usa el ítem
     */
    public abstract void use(Character target);

    /**
     * Indica si el ítem debe retirarse del inventario tras ser usado.
     * true  → Consumable (se agota)
     * false → Equipment (queda equipado / reutilizable)
     *
     * Player.useItem() usa este método para decidir si llamar removeFromInventory().
     *
     * @return true si el ítem es de un solo uso
     */
    public abstract boolean isSingleUse();

    // --- Getters ---

    public String getId()          { return id; }
    public String getName()        { return name; }
    public String getDescription() { return description; }

    // --- Identidad por ID ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item item)) return false;
        return id.equals(item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s[id=%s, name=%s]", getClass().getSimpleName(), id, name);
    }
}

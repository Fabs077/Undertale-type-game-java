package com.rpg.engine.entities;

import com.rpg.engine.core.exceptions.SaveCorruptionException;
import com.rpg.engine.core.interfaces.Persistable;
import com.rpg.engine.core.util.JsonUtil;
import com.rpg.engine.items.Armor;
import com.rpg.engine.items.Item;
import com.rpg.engine.items.Weapon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * El jugador. Único personaje controlado por el humano.
 *
 * Tres responsabilidades principales:
 *
 *   1. STATS TOTALES:
 *      getTotalAttack()  = baseAttack  + weapon.statBonus  (0 si sin arma)
 *      getTotalDefense() = baseDefense + armor.statBonus   (0 si sin armadura)
 *      FightAction y EnemyBulletHellState leen estos valores, nunca los campos directamente.
 *
 *   2. INVENTARIO Y EQUIPO:
 *      - equipWeapon() / equipArmor() devuelven el equipo anterior para que el caller
 *        decida qué hacer con él (normalmente vuelve al inventario).
 *      - useItem() aplica el efecto del ítem y lo retira si isSingleUse().
 *      - addLoot() es el único punto de entrada al inventario desde fuera.
 *
 *   3. PERSISTENCIA:
 *      - saveData() serializa todos los campos a JSON (vía JsonUtil).
 *      - loadData() reconstruye los stats básicos. La restauración de ítems
 *        (equippedWeaponId, equippedArmorId, inventoryIds) se completa en Capa 5
 *        cuando exista un ItemRegistry que mapee IDs a instancias concretas.
 */
public class Player extends Character implements Persistable {

    private int baseAttack;
    private int baseDefense;
    private Weapon equippedWeapon; // null si no lleva arma
    private Armor  equippedArmor;  // null si no lleva armadura
    private final List<Item> inventory;

    public Player(String name, int maxHp, int baseAttack, int baseDefense) {
        super(name, maxHp);
        if (baseAttack  < 0) throw new IllegalArgumentException("baseAttack must be >= 0");
        if (baseDefense < 0) throw new IllegalArgumentException("baseDefense must be >= 0");
        this.baseAttack  = baseAttack;
        this.baseDefense = baseDefense;
        this.inventory   = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Equipamiento
    // -------------------------------------------------------------------------

    /**
     * Equipa un arma nueva y devuelve la anterior (o null si no había).
     * El caller decide qué hacer con el arma anterior — normalmente Player.addLoot().
     * Este patrón evita perder silenciosamente el equipo desplazado.
     *
     * @param w el arma a equipar
     * @return el arma previamente equipada, o null
     */
    public Weapon equipWeapon(Weapon w) {
        Weapon previous   = this.equippedWeapon;
        this.equippedWeapon = w;
        return previous;
    }

    /**
     * Equipa una armadura nueva y devuelve la anterior (o null si no había).
     *
     * @param a la armadura a equipar
     * @return la armadura previamente equipada, o null
     */
    public Armor equipArmor(Armor a) {
        Armor previous   = this.equippedArmor;
        this.equippedArmor = a;
        return previous;
    }

    // -------------------------------------------------------------------------
    // Stats totales — único punto de cálculo
    // -------------------------------------------------------------------------

    /**
     * Ataque total = ataque base + bonus del arma equipada.
     * FightAction lee este valor para calcular el daño al Boss.
     */
    public int getTotalAttack() {
        return baseAttack + (equippedWeapon == null ? 0 : equippedWeapon.getStatBonus());
    }

    /**
     * Defensa total = defensa base + bonus de la armadura equipada.
     * EnemyBulletHellState la descuenta del daño del Boss antes de aplicarlo al Player.
     */
    public int getTotalDefense() {
        return baseDefense + (equippedArmor == null ? 0 : equippedArmor.getStatBonus());
    }

    // -------------------------------------------------------------------------
    // Inventario
    // -------------------------------------------------------------------------

    /**
     * Añade un ítem al inventario. No-op si el ítem es null.
     * Es el único punto de entrada al inventario desde el exterior.
     * CombatManager.handleVictoryLootDrop() lo llama con el loot del Boss.
     *
     * @param i el ítem a añadir
     */
    public void addLoot(Item i) {
        if (i != null) inventory.add(i);
    }

    /**
     * Elimina una instancia del ítem del inventario.
     *
     * @param i el ítem a eliminar
     * @return true si estaba en el inventario y fue eliminado
     */
    public boolean removeFromInventory(Item i) {
        return inventory.remove(i);
    }

    /**
     * Usa un ítem del inventario:
     *   1. Verifica que el ítem está en el inventario (no-op si no está).
     *   2. Llama item.use(this) — el polimorfismo decide el efecto concreto.
     *   3. Si el ítem es de un solo uso (Consumable), lo retira del inventario.
     *
     * @param i el ítem a usar
     */
    public void useItem(Item i) {
        if (!inventory.contains(i)) return;
        i.use(this);
        if (i.isSingleUse()) removeFromInventory(i);
    }

    /**
     * @return vista inmutable del inventario (nadie puede modificarlo desde afuera)
     */
    public List<Item> getInventory() {
        return Collections.unmodifiableList(inventory);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Weapon getEquippedWeapon() { return equippedWeapon; }
    public Armor  getEquippedArmor()  { return equippedArmor; }
    public int getBaseAttack()        { return baseAttack; }
    public int getBaseDefense()       { return baseDefense; }

    // -------------------------------------------------------------------------
    // Persistable — serialización JSON
    // -------------------------------------------------------------------------

    /**
     * Serializa el estado completo del Player a JSON.
     * Los ítems se guardan solo por su ID; la reconstrucción de objetos Item
     * la hace SaveService + ItemRegistry en Capa 5.
     *
     * @return String JSON con todos los campos del Player
     */
    @Override
    public String saveData() {
        PlayerSaveDto dto = new PlayerSaveDto(
            name,
            hp,
            maxHp,
            baseAttack,
            baseDefense,
            equippedWeapon != null ? equippedWeapon.getId() : null,
            equippedArmor  != null ? equippedArmor.getId()  : null,
            inventory.stream().map(Item::getId).toList()
        );
        return JsonUtil.toJson(dto);
    }

    /**
     * Reconstruye el estado básico del Player desde JSON.
     *
     * Invariantes validadas antes de mutar cualquier campo (atomicidad):
     *   - maxHp > 0
     *   - 0 ≤ hp ≤ maxHp
     *   - name no vacío
     *
     * Los slots de equipo e inventario se limpian aquí y se restaurarán en Capa 5
     * cuando SaveService pueda resolver IDs a instancias de Item.
     *
     * @param data String JSON generado por saveData()
     * @throws SaveCorruptionException si el JSON es inválido o viola las invariantes
     */
    @Override
    public void loadData(String data) throws SaveCorruptionException {
        try {
            PlayerSaveDto dto = JsonUtil.fromJson(data, PlayerSaveDto.class);

            if (dto == null)
                throw new SaveCorruptionException("Player save data parsed as null");
            if (dto.name() == null || dto.name().isBlank())
                throw new SaveCorruptionException("name cannot be blank");
            if (dto.maxHp() <= 0)
                throw new SaveCorruptionException("maxHp must be > 0, got: " + dto.maxHp());
            if (dto.hp() < 0 || dto.hp() > dto.maxHp())
                throw new SaveCorruptionException("hp out of bounds: " + dto.hp() + "/" + dto.maxHp());

            // validaciones superadas — mutamos los campos (atomicidad)
            this.name        = dto.name();
            this.maxHp       = dto.maxHp();
            this.hp          = dto.hp();
            this.baseAttack  = dto.baseAttack();
            this.baseDefense = dto.baseDefense();

            // slots e inventario limpiados — SaveService los restaura en Capa 5
            this.equippedWeapon = null;
            this.equippedArmor  = null;
            this.inventory.clear();

        } catch (SaveCorruptionException e) {
            throw e; // relanzar sin envolver
        } catch (Exception e) {
            throw new SaveCorruptionException("Failed to parse Player JSON: " + e.getMessage(), e);
        }
    }

    /**
     * DTO interno para serialización JSON.
     * Record de Java 17: inmutable, equals/hashCode/toString gratis.
     * Gson lo serializa/deserializa sin configuración extra.
     */
    private record PlayerSaveDto(
        String name,
        int hp,
        int maxHp,
        int baseAttack,
        int baseDefense,
        String equippedWeaponId,
        String equippedArmorId,
        List<String> inventoryIds
    ) { }
}

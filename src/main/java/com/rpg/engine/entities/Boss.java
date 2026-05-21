package com.rpg.engine.entities;

import com.rpg.engine.items.Consumable;
import com.rpg.engine.items.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Jefe de fase — el antagonista principal de cada encuentro.
 *
 * Extiende Enemy con tres sistemas propios:
 *
 *   1. MERCY / ACT:
 *      - actsPerformed se incrementa con cada ActAction del jugador.
 *      - canBeSpared() devuelve true cuando se alcanza el umbral.
 *      - Las subclases pueden sobrescribir canBeSpared() con condiciones distintas
 *        (ej. también requerir HP bajo cierto porcentaje).
 *
 *   2. DIÁLOGO:
 *      - dialogues es la lista de líneas narrativas del jefe.
 *      - nextDialogue() rota sobre ella, permitiendo textos cíclicos o progresivos.
 *
 *   3. ESCALADO (Patrón Prototype):
 *      - scaleToPhase() ajusta HP y modificador de dificultad según la fase.
 *      - copy() devuelve un clon con estado fresco — BossFactory NUNCA devuelve
 *        la instancia del pool directamente, siempre un clon.
 *
 * dropLoot() y executeBulletHellPattern() son mocks en la clase base.
 * Las subclases concretas los sobrescriben con comportamiento específico.
 */
public class Boss extends Enemy {

    private static final int ACTS_TO_SPARE = 3; // threshold mínimo de ACTs para poder perdonar

    private ArrayList<String> dialogues;
    private boolean isSpared;
    private int phaseLevel;
    private int actsPerformed; // incrementado por registerActAttempt()
    private int dialogueIndex; // cursor de rotación para nextDialogue()

    public Boss(String name, int maxHp, double baseDifficultyModifier,
                int phaseLevel, List<String> dialogues) {
        super(name, maxHp, baseDifficultyModifier);
        this.phaseLevel    = phaseLevel;
        this.dialogues     = new ArrayList<>(dialogues); // copia defensiva
        this.isSpared      = false;
        this.actsPerformed = 0;
        this.dialogueIndex = 0;
    }

    // -------------------------------------------------------------------------
    // Loot
    // -------------------------------------------------------------------------

    /**
     * Decide qué ítem suelta este jefe al terminar el encuentro (por derrota o mercy).
     * La implementación base devuelve null — las subclases concretas proveen el ítem real.
     * CombatManager.handleVictoryLootDrop() ignora el null si no hay loot.
     *
     * @return un Item concreto, o null si este jefe no deja botín
     */
    public Item dropLoot() {
        int healAmount = 20 + phaseLevel * 10;
        return new Consumable(
            "potion_phase_" + phaseLevel,
            "Poción de Fase " + phaseLevel,
            "Restaura " + healAmount + " HP.",
            healAmount
        );
    }

    // -------------------------------------------------------------------------
    // Mercy / ACT
    // -------------------------------------------------------------------------

    /**
     * Registra un intento de ACT del jugador.
     * Llamado por ActAction.execute() en Capa 4.
     */
    public void registerActAttempt() {
        actsPerformed++;
    }

    /**
     * Determina si el jefe puede ser perdonado en este momento.
     * Condición base: el jugador ha realizado suficientes ACTs.
     * Las subclases pueden añadir condiciones (ej. HP < 20%).
     *
     * @return true si MercyAction puede activarse con éxito
     */
    public boolean canBeSpared() {
        return actsPerformed >= ACTS_TO_SPARE;
    }

    /**
     * Marca al jefe como perdonado.
     * Solo debe llamarse desde MercyAction.execute() cuando canBeSpared() es true.
     *
     * @param spared true para marcar como perdonado
     */
    public void setSpared(boolean spared) {
        this.isSpared = spared;
    }

    // -------------------------------------------------------------------------
    // Bullet Hell (mock — Capa 4+ implementa la lógica real)
    // -------------------------------------------------------------------------

    /**
     * Devuelve el identificador del patrón bullet-hell que ejecutará este boss este turno.
     * Por ahora es un String; en una iteración futura devolverá un BulletHellPattern (Strategy).
     *
     * EnemyBulletHellState.onEnter() consume este valor para configurar el ataque.
     *
     * @return nombre del patrón, ej. "radial_spread_phase_2"
     */
    public String executeBulletHellPattern() {
        return "basic_pattern_phase_" + phaseLevel;
    }

    // -------------------------------------------------------------------------
    // Diálogo
    // -------------------------------------------------------------------------

    /**
     * Devuelve la siguiente línea de diálogo en rotación.
     * Si no hay diálogos configurados, devuelve cadena vacía.
     *
     * @return línea de diálogo del jefe
     */
    public String nextDialogue() {
        if (dialogues.isEmpty()) return "";
        String line = dialogues.get(dialogueIndex % dialogues.size());
        dialogueIndex++;
        return line;
    }

    /** Añade una línea de diálogo al repertorio del jefe. */
    public void addDialogue(String line) {
        if (line != null && !line.isBlank()) dialogues.add(line);
    }

    // -------------------------------------------------------------------------
    // Escalado — Patrón Prototype
    // -------------------------------------------------------------------------

    /**
     * Escala los stats de este Boss para la fase indicada.
     * Invocado por BossFactory DESPUÉS de clonar el prototipo.
     *
     * Fórmulas:
     *   maxHp              *= 1 + 0.25 * phaseLevel
     *   baseDifficultyMod  *= 1 + 0.15 * phaseLevel
     *
     * El HP se resincroniza a maxHp para que el Boss empiece con vida completa.
     *
     * @param phaseLevel nivel de fase (1–5)
     */
    public void scaleToPhase(int phaseLevel) {
        double hpMult  = 1.0 + 0.25 * phaseLevel;
        double modMult = 1.0 + 0.15 * phaseLevel;
        this.maxHp                 = (int) Math.round(this.maxHp * hpMult);
        this.hp                    = this.maxHp;
        this.baseDifficultyModifier = this.baseDifficultyModifier * modMult;
        this.phaseLevel            = phaseLevel;
    }

    /**
     * Devuelve una copia fresca de este Boss con estado inicial (hp = maxHp, isSpared = false).
     * BossFactory llama este método para no contaminar el prototipo del pool con estado mutable.
     *
     * Las subclases con campos extra DEBEN sobrescribir este método.
     *
     * @return nuevo Boss con los mismos valores base, sin estado de combate
     */
    public Boss copy() {
        return new Boss(name, maxHp, baseDifficultyModifier, phaseLevel, dialogues);
    }

    // -------------------------------------------------------------------------
    // Enemy contract
    // -------------------------------------------------------------------------

    /**
     * Poder de ataque base del Boss: 10 × baseDifficultyModifier (redondeado).
     * EnemyBulletHellState usará este valor para calcular el daño al Player
     * después de aplicar Player.getTotalDefense().
     */
    @Override
    public int computeAttackPower() {
        return (int) Math.round(10.0 * baseDifficultyModifier);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public boolean isSpared()                  { return isSpared; }
    public int getPhaseLevel()                 { return phaseLevel; }
    public int getActsPerformed()              { return actsPerformed; }
    public ArrayList<String> getDialogues()     { return new ArrayList<>(dialogues); }

    /** Carpeta de sprites: sprites/bosses/{spriteId}/ — derivado del nombre del boss. */
    public String getSpriteId() {
        return name.toLowerCase().replace(" ", "_");
    }
}

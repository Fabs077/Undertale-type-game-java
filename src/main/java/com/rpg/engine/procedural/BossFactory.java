package com.rpg.engine.procedural;

import com.rpg.engine.core.exceptions.ResourceNotFoundException;
import com.rpg.engine.entities.Boss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Fábrica de jefes — Patrón Factory + Prototype.
 *
 * Mantiene un mapa de pools de prototipos por nivel de fase.
 * Al generar un jefe:
 *   1. Selecciona aleatoriamente un prototipo del pool correspondiente.
 *   2. Clona el prototipo vía Boss.copy() — NUNCA devuelve la instancia del pool,
 *      porque los Boss tienen estado mutable (hp, isSpared, actsPerformed).
 *   3. Escala los stats del clon al nivel de fase solicitado vía Boss.scaleToPhase().
 *
 * El RNG se inyecta por constructor para que los tests puedan usar semilla fija
 * y obtener resultados deterministas (reproducibilidad).
 *
 * Uso típico:
 *   BossFactory factory = new BossFactory(new Random(42));
 *   factory.registerBoss(1, new Boss("Froggit", 100, 1.0, 1, List.of("...")));
 *   Boss boss = factory.generateRandomBoss(1); // clon escalado a fase 1
 */
public class BossFactory {

    private final Map<Integer, List<Boss>> bossPools = new HashMap<>();
    private final Random rng;

    public BossFactory(Random rng) {
        this.rng = rng;
    }

    // -------------------------------------------------------------------------
    // Registro de prototipos
    // -------------------------------------------------------------------------

    /**
     * Añade un prototipo de Boss al pool de la fase indicada.
     * El prototipo permanece en el pool sin modificarse; generateRandomBoss() trabaja
     * siempre con copias.
     *
     * @param phaseLevel fase a la que pertenece este prototipo (≥ 1)
     * @param prototype  instancia base con los stats sin escalar
     */
    public void registerBoss(int phaseLevel, Boss prototype) {
        if (phaseLevel < 1)   throw new IllegalArgumentException("phaseLevel must be >= 1, got: " + phaseLevel);
        if (prototype == null) throw new IllegalArgumentException("prototype cannot be null");
        bossPools.computeIfAbsent(phaseLevel, k -> new ArrayList<>()).add(prototype);
    }

    // -------------------------------------------------------------------------
    // Generación
    // -------------------------------------------------------------------------

    /**
     * Genera un Boss listo para combate clonando un prototipo aleatorio del pool
     * y escalándolo a la fase solicitada.
     *
     * @param phaseLevel fase para la que se necesita el jefe (1–5)
     * @return un Boss nuevo con stats escalados, completamente independiente del prototipo
     * @throws ResourceNotFoundException si no hay prototipos registrados para esa fase
     */
    public Boss generateRandomBoss(int phaseLevel) throws ResourceNotFoundException {
        List<Boss> pool = bossPools.get(phaseLevel);
        if (pool == null || pool.isEmpty()) {
            throw new ResourceNotFoundException("boss pool for phase " + phaseLevel);
        }
        Boss prototype = pool.get(rng.nextInt(pool.size()));
        Boss clone     = prototype.copy();   // copia fresca: hp = maxHp, isSpared = false
        clone.scaleToPhase(phaseLevel);      // escala stats al nivel de fase
        return clone;
    }

    // -------------------------------------------------------------------------
    // Diagnóstico / tests
    // -------------------------------------------------------------------------

    /**
     * @param phaseLevel la fase a consultar
     * @return número de prototipos registrados para esa fase (0 si la fase no existe)
     */
    public int poolSize(int phaseLevel) {
        List<Boss> pool = bossPools.get(phaseLevel);
        return pool == null ? 0 : pool.size();
    }
}

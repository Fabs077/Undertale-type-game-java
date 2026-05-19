package com.rpg.engine.procedural;

import com.rpg.engine.core.exceptions.ResourceNotFoundException;
import com.rpg.engine.entities.Boss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class BossFactory {

    private final Map<Integer, ArrayList<Boss>> bossPools = new HashMap<>();
    private final Random rng;

    public BossFactory(Random rng) {
        this.rng = rng;
    }


    public void registerBoss(int phaseLevel, Boss prototype) {
        if (phaseLevel < 1)   throw new IllegalArgumentException("phaseLevel must be >= 1, got: " + phaseLevel);
        if (prototype == null) throw new IllegalArgumentException("prototype cannot be null");
        bossPools.computeIfAbsent(phaseLevel, k -> new ArrayList<>()).add(prototype);
    }

    public Boss generateRandomBoss(int phaseLevel) throws ResourceNotFoundException {
        ArrayList<Boss> pool = bossPools.get(phaseLevel);
        if (pool == null || pool.isEmpty()) {
            throw new ResourceNotFoundException("boss pool for phase " + phaseLevel);
        }
        Boss prototype = pool.get(rng.nextInt(pool.size()));
        Boss clone     = prototype.copy();   // copia fresca: hp = maxHp, isSpared = false
        clone.scaleToPhase(phaseLevel);      // escala stats al nivel de fase
        return clone;
    }

    public int poolSize(int phaseLevel) {
        ArrayList<Boss> pool = bossPools.get(phaseLevel);
        return pool == null ? 0 : pool.size();
    }
}

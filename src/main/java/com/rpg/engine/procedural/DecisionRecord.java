package com.rpg.engine.procedural;

/**
 * Registro inmutable de una decisión tomada al final de un combate.
 * Almacenado por HistoryManager y consultado por StoryGenerator.
 *
 * Inmutabilidad: todos los campos son final; no hay setters.
 */
public record DecisionRecord(
    String bossName,
    int phaseLevel,
    boolean wasSpared,
    long timestamp
) { }

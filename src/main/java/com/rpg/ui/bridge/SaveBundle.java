package com.rpg.ui.bridge;

/** Snapshot de todo el estado persistible de una partida. */
public record SaveBundle(String playerJson, String historyJson, int phase) {}

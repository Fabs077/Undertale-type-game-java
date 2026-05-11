package com.rpg.engine.procedural;

/**
 * Ruta narrativa de la partida, derivada del historial de decisiones.
 *
 * PACIFIC  — el jugador perdonó a todos los jefes (todos los combates terminaron en MERCY).
 * NEUTRAL  — mezcla de decisiones (algunos derrotados, algunos perdonados, o aún sin historial).
 * GENOCIDE — el jugador eliminó a todos los jefes en combate.
 *
 * StoryGenerator.getCurrentRoute() recalcula la ruta en cada consulta
 * leyendo el estado actual de HistoryManager, sin almacenar la ruta como estado propio.
 */
public enum Route {
    PACIFIC,
    NEUTRAL,
    GENOCIDE
}

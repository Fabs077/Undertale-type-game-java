package com.rpg.engine.procedural;

import com.rpg.engine.core.exceptions.SaveCorruptionException;
import com.rpg.engine.core.interfaces.Persistable;
import com.rpg.engine.core.util.JsonUtil;
import com.rpg.engine.entities.Boss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registra cronológicamente las decisiones tomadas al final de cada combate.
 *
 * StoryGenerator lo consulta para determinar la ruta narrativa:
 *   - todos perdonados → PACIFIC
 *   - todos eliminados → GENOCIDE
 *   - mix             → NEUTRAL
 *
 * La serialización usa DTOs internos (no los records de dominio) para desacoplar
 * el formato JSON de la estructura interna. Si DecisionRecord cambia, el formato
 * guardado puede mantenerse estable ajustando solo el DTO.
 */
public class HistoryManager implements Persistable {

    private final List<DecisionRecord> records = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Registro de decisiones
    // -------------------------------------------------------------------------

    /**
     * Añade una entrada al historial al final de un combate.
     * Llamado por CombatManager.handleVictoryLootDrop().
     *
     * @param boss      el jefe cuyo combate acaba de terminar
     * @param wasSpared true si el jugador usó MERCY exitosamente
     */
    public void recordDecision(Boss boss, boolean wasSpared) {
        records.add(new DecisionRecord(
            boss.getName(),
            boss.getPhaseLevel(),
            wasSpared,
            System.currentTimeMillis()
        ));
    }

    /** @return vista inmutable de todos los registros en orden cronológico */
    public List<DecisionRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }

    /** @return número de jefes perdonados hasta ahora */
    public int getSparedCount() {
        return (int) records.stream().filter(DecisionRecord::wasSpared).count();
    }

    /** @return número de jefes derrotados en combate hasta ahora */
    public int getKilledCount() {
        return (int) records.stream().filter(r -> !r.wasSpared()).count();
    }

    // -------------------------------------------------------------------------
    // Persistable — serialización JSON
    // -------------------------------------------------------------------------

    /**
     * Serializa el historial completo a JSON.
     * Usa un DTO intermedio para aislar el formato guardado de la estructura interna.
     */
    @Override
    public String saveData() {
        List<EntryDto> dtos = records.stream()
            .map(r -> new EntryDto(r.bossName(), r.phaseLevel(), r.wasSpared(), r.timestamp()))
            .toList();
        return JsonUtil.toJson(new HistorySaveDto(dtos));
    }

    /**
     * Reconstruye el historial desde JSON.
     * Si la lista está vacía o es null, el historial queda vacío (caso inicio de partida).
     *
     * @throws SaveCorruptionException si el JSON es inválido
     */
    @Override
    public void loadData(String data) throws SaveCorruptionException {
        try {
            HistorySaveDto dto = JsonUtil.fromJson(data, HistorySaveDto.class);
            if (dto == null) throw new SaveCorruptionException("HistoryManager save data is null");
            records.clear();
            if (dto.entries() != null) {
                for (EntryDto e : dto.entries()) {
                    records.add(new DecisionRecord(e.bossName(), e.phaseLevel(), e.wasSpared(), e.timestamp()));
                }
            }
        } catch (SaveCorruptionException e) {
            throw e;
        } catch (Exception e) {
            throw new SaveCorruptionException("Failed to parse HistoryManager: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // DTOs privados de serialización
    // -------------------------------------------------------------------------

    private record HistorySaveDto(List<EntryDto> entries) { }

    private record EntryDto(String bossName, int phaseLevel, boolean wasSpared, long timestamp) { }
}

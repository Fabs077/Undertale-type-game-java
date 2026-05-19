package com.rpg.engine.procedural;

import com.rpg.engine.core.exceptions.SaveCorruptionException;
import com.rpg.engine.core.interfaces.Persistable;
import com.rpg.engine.core.util.JsonUtil;
import com.rpg.engine.entities.Boss;

import java.util.ArrayList;


public class HistoryManager implements Persistable {

    private final ArrayList<DecisionRecord> records = new ArrayList<>();

    public void recordDecision(Boss boss, boolean wasSpared) {
        records.add(new DecisionRecord(
            boss.getName(),
            boss.getPhaseLevel(),
            wasSpared,
            System.currentTimeMillis()
        ));
    }

    /** @return copia del historial en orden cronológico */
    public ArrayList<DecisionRecord> getRecords() {
        return new ArrayList<>(records);
    }

    /** @return número de jefes perdonados hasta ahora */
    public int getSparedCount() {
        return (int) records.stream().filter(DecisionRecord::wasSpared).count();
    }

    /** @return número de jefes derrotados en combate hasta ahora */
    public int getKilledCount() {
        return (int) records.stream().filter(r -> !r.wasSpared()).count();
    }

    @Override
    public String saveData() {
        ArrayList<EntryDto> dtos = new ArrayList<>();
        for (DecisionRecord r : records) {
            dtos.add(new EntryDto(r.bossName(), r.phaseLevel(), r.wasSpared(), r.timestamp()));
        }
        return JsonUtil.toJson(new HistorySaveDto(dtos));
    }

    @Override
    public void loadData(String data) throws SaveCorruptionException {
        try {
            HistorySaveDto dto = JsonUtil.fromJson(data, HistorySaveDto.class);
            if (dto == null) throw new SaveCorruptionException(SaveCorruptionException.UNKNOWN_SLOT, "HistoryManager save data is null");
            records.clear();
            if (dto.entries() != null) {
                for (EntryDto e : dto.entries()) {
                    records.add(new DecisionRecord(e.bossName(), e.phaseLevel(), e.wasSpared(), e.timestamp()));
                }
            }
        } catch (SaveCorruptionException e) {
            throw e;
        } catch (Exception e) {
            throw new SaveCorruptionException(SaveCorruptionException.UNKNOWN_SLOT, "Failed to parse HistoryManager: " + e.getMessage(), e);
        }
    }

    private record HistorySaveDto(ArrayList<EntryDto> entries) { }

    private record EntryDto(String bossName, int phaseLevel, boolean wasSpared, long timestamp) { }
}

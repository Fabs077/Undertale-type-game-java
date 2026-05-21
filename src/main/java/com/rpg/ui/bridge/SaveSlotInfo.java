package com.rpg.ui.bridge;

/** Metadata liviana de un slot, usada para mostrar tarjetas sin descomprimir el .sav completo. */
public record SaveSlotInfo(
        int     slot,
        String  playerName,
        int     phase,
        String  timestamp,
        boolean empty,
        boolean corrupted) {

    public static SaveSlotInfo empty(int slot) {
        return new SaveSlotInfo(slot, "", 0, "", true, false);
    }

    public static SaveSlotInfo corrupted(int slot) {
        return new SaveSlotInfo(slot, "", 0, "", false, true);
    }
}

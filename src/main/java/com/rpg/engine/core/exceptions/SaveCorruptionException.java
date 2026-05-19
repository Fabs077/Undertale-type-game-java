package com.rpg.engine.core.exceptions;

/**
 * Lanzada cuando Persistable.loadData() recibe datos JSON inválidos o
 * que violan las invariantes del objeto (HP negativo, fase > 5, etc.).
 *
 * Es checked (extends Exception) para forzar manejo explícito: quien llame
 * a loadData() debe decidir qué hacer si el save está corrupto, en lugar de
 * que el error pase desapercibido como una RuntimeException.
 */
public class SaveCorruptionException extends Exception {

    /** Usado por capas de engine que no conocen el slot de archivo (e.g. PhaseManager, Player). */
    public static final int UNKNOWN_SLOT = -1;

    private final int slot;

    public SaveCorruptionException(int slot, String message) {
        super(message);
        this.slot = slot;
    }

    public SaveCorruptionException(int slot, String message, Throwable cause) {
        super(message, cause);
        this.slot = slot;
    }

    public int getSlot() { return slot; }
}

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

    public SaveCorruptionException(String message) {
        super(message);
    }

    public SaveCorruptionException(String message, Throwable cause) {
        super(message, cause);
    }
}

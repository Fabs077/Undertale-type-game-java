package com.rpg.ui.bridge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rpg.engine.core.exceptions.SaveCorruptionException;

public final class SaveSlotManager {

    public String loadRawJson(int slot) throws SaveCorruptionException {
        FileHandle file = Gdx.files.local("saves/slot_" + slot + ".json");
        try {
            if (!file.exists()) {
                throw new SaveCorruptionException(slot, "archivo no encontrado");
            }
            String raw = file.readString("UTF-8");
            String trimmed = raw.trim();
            if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
                throw new SaveCorruptionException(slot, "JSON inválido (no es un objeto)");
            }
            return raw;
        } catch (SaveCorruptionException e) {
            throw e;
        } catch (Exception e) {
            throw new SaveCorruptionException(slot, "error de lectura – " + e.getMessage(), e);
        }
    }
}

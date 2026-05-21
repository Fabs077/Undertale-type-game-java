package com.rpg.ui.bridge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rpg.engine.core.exceptions.SaveCorruptionException;
import com.rpg.engine.core.util.JsonUtil;
import com.rpg.engine.entities.Player;
import com.rpg.engine.procedural.HistoryManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Capa de I/O de partidas.
 *
 * Formato en disco:
 *   saves/slot_N.sav        — JSON comprimido con GZIP (SaveBundle completo)
 *   saves/slot_N_meta.json  — JSON plano con metadata liviana para la UI
 *
 * El motor produce/consume Strings JSON puros; esta clase sólo hace I/O y compresión.
 */
public final class SaveSlotManager {

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Metadata mínima guardada en el archivo _meta.json para mostrar en las tarjetas. */
    private record SlotMeta(String playerName, int phase, String timestamp) {}

    // ── Guardar ──────────────────────────────────────────────────────────────

    /**
     * Serializa el estado del juego, lo comprime con GZIP y lo escribe en
     * {@code saves/slot_N.sav}.  También escribe un archivo de metadata liviana.
     *
     * @throws SaveCorruptionException si la escritura falla
     */
    public void saveToSlot(int slot, Player player, HistoryManager history, int phase)
            throws SaveCorruptionException {
        try {
            SaveBundle bundle = new SaveBundle(
                    player.saveData(),
                    history.saveData(),
                    phase);

            byte[] compressed = gzip(JsonUtil.toJson(bundle));
            Gdx.files.local("saves/slot_" + slot + ".sav").writeBytes(compressed, false);

            SlotMeta meta = new SlotMeta(
                    player.getName(),
                    phase,
                    LocalDateTime.now().format(TS_FMT));
            Gdx.files.local("saves/slot_" + slot + "_meta.json")
                    .writeString(JsonUtil.toJson(meta), false, "UTF-8");

        } catch (IOException e) {
            throw new SaveCorruptionException(slot, "error al comprimir/escribir: " + e.getMessage(), e);
        }
    }

    // ── Cargar ───────────────────────────────────────────────────────────────

    /**
     * Lee {@code saves/slot_N.sav}, descomprime y devuelve el {@link SaveBundle}.
     *
     * @throws SaveCorruptionException si el archivo no existe, está corrupto o falla la descompresión
     */
    public SaveBundle loadBundle(int slot) throws SaveCorruptionException {
        FileHandle file = Gdx.files.local("saves/slot_" + slot + ".sav");
        try {
            if (!file.exists())
                throw new SaveCorruptionException(slot, "archivo de partida no encontrado");

            String json = ungzip(file.readBytes());
            SaveBundle bundle = JsonUtil.fromJson(json, SaveBundle.class);
            if (bundle == null)
                throw new SaveCorruptionException(slot, "bundle nulo tras deserialización");
            return bundle;

        } catch (SaveCorruptionException e) {
            throw e;
        } catch (Exception e) {
            throw new SaveCorruptionException(slot, "error al descomprimir: " + e.getMessage(), e);
        }
    }

    // ── Metadata para UI ─────────────────────────────────────────────────────

    /**
     * Devuelve la metadata de un slot sin descomprimir el save completo.
     * Nunca lanza excepción: devuelve {@link SaveSlotInfo#empty} o {@link SaveSlotInfo#corrupted}.
     */
    public SaveSlotInfo getSlotInfo(int slot) {
        FileHandle sav  = Gdx.files.local("saves/slot_" + slot + ".sav");
        FileHandle meta = Gdx.files.local("saves/slot_" + slot + "_meta.json");

        if (!sav.exists() && !meta.exists()) return SaveSlotInfo.empty(slot);

        if (meta.exists()) {
            try {
                SlotMeta m = JsonUtil.fromJson(meta.readString("UTF-8"), SlotMeta.class);
                if (m == null) return SaveSlotInfo.corrupted(slot);
                return new SaveSlotInfo(slot, m.playerName(), m.phase(), m.timestamp(), false, false);
            } catch (Exception e) {
                return SaveSlotInfo.corrupted(slot);
            }
        }

        // .sav existe pero sin meta: marcar corrupto (meta debería siempre existir junto al .sav)
        return SaveSlotInfo.corrupted(slot);
    }

    // ── GZIP helpers ─────────────────────────────────────────────────────────

    private static byte[] gzip(String text) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(buf)) {
            gz.write(text.getBytes(StandardCharsets.UTF_8));
        }
        return buf.toByteArray();
    }

    private static String ungzip(byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(data))) {
            byte[] buf = new byte[4096];
            int read;
            while ((read = gz.read(buf)) != -1) out.write(buf, 0, read);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}

package com.rpg.engine.procedural;

import com.rpg.engine.core.exceptions.ResourceNotFoundException;
import com.rpg.engine.core.exceptions.SaveCorruptionException;
import com.rpg.engine.core.interfaces.Persistable;
import com.rpg.engine.core.util.JsonUtil;
import com.rpg.engine.entities.Boss;

/**
 * Controla el progreso de la partida a través de las 5 fases.
 *
 * Es el "director" de la secuencia de combates: cada vez que el jugador supera
 * un encuentro, el bucle de juego llama advancePhase() para obtener el siguiente boss.
 *
 * Estado persistido: únicamente currentPhase.
 * El Boss actual NO se persiste porque es efímero — al recargar, el bucle de juego
 * regenera el boss de la fase actual vía BossFactory. Si se quisiera guardar
 * mid-combat (HP actual del boss), se extendería el DTO en una iteración futura.
 *
 * Invariante: 0 ≤ currentPhase ≤ MAX_PHASES.
 * currentPhase == 0 significa que aún no ha comenzado ningún combate.
 */
public class PhaseManager implements Persistable {

    private static final int MAX_PHASES = 5;

    private int  currentPhase; // 0 = inicio, 1–5 = fase activa
    private Boss currentBoss;  // null hasta el primer advancePhase()

    private final BossFactory factory;

    public PhaseManager(BossFactory factory) {
        this.factory      = factory;
        this.currentPhase = 0;
        this.currentBoss  = null;
    }

    // -------------------------------------------------------------------------
    // Control de fases
    // -------------------------------------------------------------------------

    /**
     * Avanza a la siguiente fase y genera el boss correspondiente.
     *
     * @return el Boss generado para la nueva fase, o null si ya se completaron las 5 fases
     * @throws ResourceNotFoundException si BossFactory no tiene prototipos para esa fase
     */
    public Boss advancePhase() throws ResourceNotFoundException {
        if (currentPhase >= MAX_PHASES) return null;
        currentPhase++;
        currentBoss = factory.generateRandomBoss(currentPhase);
        return currentBoss;
    }

    /** @return true cuando ya se han completado las 5 fases */
    public boolean isFinalPhase() {
        return currentPhase == MAX_PHASES;
    }

    /** @return fase actual (0 = inicio, 1–5 = fase en curso o completada) */
    public int getCurrentPhase() { return currentPhase; }

    /** @return el Boss generado para la fase actual, o null si no ha empezado */
    public Boss getCurrentBoss()  { return currentBoss; }

    // -------------------------------------------------------------------------
    // Persistable
    // -------------------------------------------------------------------------

    /**
     * Solo persiste currentPhase. El boss se regenera al cargar
     * vía BossFactory (siempre empieza con HP completo y estado limpio).
     */
    @Override
    public String saveData() {
        return JsonUtil.toJson(new PhaseManagerSaveDto(currentPhase));
    }

    /**
     * Restaura currentPhase. currentBoss queda null — el bucle de juego
     * debe llamar advancePhase() o preparar el boss explícitamente tras cargar.
     *
     * @throws SaveCorruptionException si currentPhase está fuera de [0, MAX_PHASES]
     */
    @Override
    public void loadData(String data) throws SaveCorruptionException {
        try {
            PhaseManagerSaveDto dto = JsonUtil.fromJson(data, PhaseManagerSaveDto.class);
            if (dto == null)
                throw new SaveCorruptionException(SaveCorruptionException.UNKNOWN_SLOT, "PhaseManager save data is null");
            if (dto.currentPhase() < 0 || dto.currentPhase() > MAX_PHASES)
                throw new SaveCorruptionException(
                    SaveCorruptionException.UNKNOWN_SLOT,
                    "currentPhase out of bounds [0," + MAX_PHASES + "]: " + dto.currentPhase());

            this.currentPhase = dto.currentPhase();
            this.currentBoss  = null; // regenerado por el bucle de juego
        } catch (SaveCorruptionException e) {
            throw e;
        } catch (Exception e) {
            throw new SaveCorruptionException(SaveCorruptionException.UNKNOWN_SLOT, "Failed to parse PhaseManager: " + e.getMessage(), e);
        }
    }

    private record PhaseManagerSaveDto(int currentPhase) { }
}

package com.rpg.engine.procedural;

import com.rpg.engine.entities.Boss;

public class StoryGenerator {

    private final HistoryManager history;

    public StoryGenerator(HistoryManager history) {
        this.history = history;
    }


    public Route getCurrentRoute() {
        int total  = history.getRecords().size();
        if (total == 0) return Route.NEUTRAL;

        int spared = history.getSparedCount();
        int killed = history.getKilledCount();

        if (spared == total) return Route.PACIFIC;
        if (killed == total) return Route.GENOCIDE;
        return Route.NEUTRAL;
    }


    public String generateNarrativeForPhase(int phase) {
        return switch (getCurrentRoute()) {
            case PACIFIC -> pacificNarrative(phase);
            case GENOCIDE -> genocideNarrative(phase);
            case NEUTRAL -> neutralNarrative(phase);
        };
    }

    public String generateBossIntroDialogue(Boss boss) {
        return switch (getCurrentRoute()) {
            case PACIFIC   -> boss.getName() + " te observa con cautela. "
                              + "Quizás también a él puedas llegar sin violencia.";
            case GENOCIDE  -> boss.getName() + " te mira con terror. "
                              + "Sabe lo que les has hecho a los demás.";
            case NEUTRAL   -> boss.getName() + " aparece ante ti. "
                              + "El destino de este encuentro aún está por escribirse.";
        };
    }


    private String pacificNarrative(int phase) {
        return switch (phase) {
            case 1 -> "Fase 1 [PACÍFICA] — Tus primeros pasos están guiados por la empatía. "
                    + "El mundo te observa con esperanza.";
            case 2 -> "Fase 2 [PACÍFICA] — Tu compasión empieza a resonar. "
                    + "Algunos enemigos dudan antes de atacar.";
            case 3 -> "Fase 3 [PACÍFICA] — A mitad del camino, tu reputación te precede. "
                    + "Las criaturas susurran tu nombre con respeto.";
            case 4 -> "Fase 4 [PACÍFICA] — Casi al final. La paz que has forjado "
                    + "pesa tanto como cualquier victoria en combate.";
            case 5 -> "Fase 5 [PACÍFICA] — El último obstáculo. ¿Tendrás el valor "
                    + "de extender la misericordia una vez más?";
            default -> "Fase " + phase + " [PACÍFICA] — La paz te acompaña.";
        };
    }

    private String genocideNarrative(int phase) {
        return switch (phase) {
            case 1 -> "Fase 1 [GENOCIDIO] — Dejas un rastro de destrucción desde el principio. "
                    + "Algo oscuro despierta en este mundo.";
            case 2 -> "Fase 2 [GENOCIDIO] — El miedo se extiende. "
                    + "Los enemigos huyen antes de que llegues.";
            case 3 -> "Fase 3 [GENOCIDIO] — A mitad del camino hacia la aniquilación. "
                    + "El silencio llena los lugares donde antes había vida.";
            case 4 -> "Fase 4 [GENOCIDIO] — Casi nadie queda. "
                    + "Tu poder es absoluto y aterrador.";
            case 5 -> "Fase 5 [GENOCIDIO] — El final de todo. "
                    + "¿Qué habrá después de que no quede nadie?";
            default -> "Fase " + phase + " [GENOCIDIO] — Nada puede detenerte.";
        };
    }

    private String neutralNarrative(int phase) {
        return switch (phase) {
            case 1 -> "Fase 1 [NEUTRAL] — El camino se abre ante ti. "
                    + "Aún no sabes quién serás al final.";
            case 2 -> "Fase 2 [NEUTRAL] — Tus decisiones no tienen un patrón claro. "
                    + "El mundo no sabe qué esperar de ti.";
            case 3 -> "Fase 3 [NEUTRAL] — A mitad de la travesía, la ambigüedad "
                    + "te define tanto como cualquier elección.";
            case 4 -> "Fase 4 [NEUTRAL] — Las consecuencias de tus actos "
                    + "mezclan esperanza y tragedia.";
            case 5 -> "Fase 5 [NEUTRAL] — El último paso. Sea lo que sea que hagas, "
                    + "este momento lo decidirá todo.";
            default -> "Fase " + phase + " [NEUTRAL] — El camino no está definido aún.";
        };
    }
}

package com.rpg.engine.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Fachada sobre Gson que centraliza la serialización JSON del motor.
 *
 * Por qué existe esta clase y no se usa Gson directamente en cada entidad:
 *   - Si en el futuro se cambia de Gson a Jackson u otra librería, el cambio
 *     se hace aquí y no hay que tocar Player, PhaseManager, HistoryManager, etc.
 *   - Los tests pueden verificar el formato JSON pasando por este mismo punto.
 *
 * Las entidades (Player, PhaseManager, HistoryManager) llaman a JsonUtil.toJson()
 * y JsonUtil.fromJson() desde sus métodos saveData() / loadData().
 * La I/O de disco (leer/escribir archivos) la hace SaveService en Capa 5.
 */
public final class JsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()   // incluye campos null en el JSON (importante para IDs de equipo opcionales)
            .setPrettyPrinting()
            .create();

    private JsonUtil() { /* clase utilitaria, no instanciar */ }

    /**
     * Serializa cualquier objeto a su representación JSON.
     *
     * @param obj el objeto a serializar
     * @return String JSON
     */
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    /**
     * Deserializa un String JSON a la clase indicada.
     *
     * @param json  el String JSON
     * @param clazz la clase destino
     * @param <T>   tipo del objeto resultante
     * @return instancia reconstruida
     * @throws JsonSyntaxException si el JSON es sintácticamente inválido
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonSyntaxException {
        return GSON.fromJson(json, clazz);
    }
}

package com.rpg.engine.core.interfaces;

import com.rpg.engine.core.exceptions.SaveCorruptionException;

/**
 * Contrato de serialización en memoria.
 *
 * Las clases que implementan esta interfaz son dueñas de estado de larga vida
 * (Player, PhaseManager, HistoryManager). El combate en sí es efímero y NO implementa esto.
 *
 * La I/O real (leer/escribir archivos) la hace SaveService en Capa 5.
 * Estas clases sólo conocen cómo describirse como String JSON y reconstruirse desde él.
 */
public interface Persistable {

    /**
     * Serializa el estado relevante del objeto a un String en formato JSON.
     * No toca disco. Nunca lanza excepción: un objeto siempre puede describirse a sí mismo.
     *
     * @return JSON representando el estado actual del objeto
     */
    String saveData();

    /**
     * Reconstruye el estado del objeto a partir de un String JSON previamente
     * generado por saveData(). Principio de atomicidad: si falla alguna validación,
     * lanza la excepción SIN dejar al objeto en estado parcialmente cargado.
     *
     * @param data String JSON con el estado guardado
     * @throws SaveCorruptionException si el JSON es ilegible o viola las invariantes del objeto
     */
    void loadData(String data) throws SaveCorruptionException;
}

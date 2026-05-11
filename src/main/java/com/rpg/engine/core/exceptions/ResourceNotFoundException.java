package com.rpg.engine.core.exceptions;

/**
 * Lanzada cuando se solicita un recurso que no existe en el sistema.
 *
 * Usos principales:
 *   - BossFactory.generateRandomBoss(): pool de jefes no registrado para esa fase.
 *   - ItemRegistry (Capa 5): ítem referenciado por ID no encontrado al cargar un save.
 *
 * Es checked para obligar al caller a manejar explícitamente el caso de recurso ausente,
 * lo que hace el sistema más antifrágil: nunca se obtiene un null silencioso.
 */
public class ResourceNotFoundException extends Exception {

    private final String resourceId;

    public ResourceNotFoundException(String resourceId) {
        super("Resource not found: '" + resourceId + "'");
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String resourceId, Throwable cause) {
        super("Resource not found: '" + resourceId + "'", cause);
        this.resourceId = resourceId;
    }

    /** @return el identificador del recurso que no fue encontrado */
    public String getResourceId() {
        return resourceId;
    }
}

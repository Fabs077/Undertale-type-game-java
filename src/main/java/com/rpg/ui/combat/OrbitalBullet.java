package com.rpg.ui.combat;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Bala que orbita un punto fijo a velocidad angular constante.
 *
 * La posición se recalcula desde coordenadas polares en cada frame,
 * ignorando el campo velocity heredado de Bullet (se mantiene en (0,0)).
 *
 * El hitbox se sincroniza manualmente con la nueva posición porque
 * syncHitbox() es privado en la clase base; se reproduce aquí con el
 * mismo tamaño (HALF_SIZE = SIZE/2 = 4f).
 */
public class OrbitalBullet extends Bullet {

    private static final float HALF_SIZE = 4f;

    private float angle;
    private final float angularSpeed;
    private final float radius;
    private final float cx;
    private final float cy;

    /**
     * @param cx           centro X de la órbita (píxeles de pantalla)
     * @param cy           centro Y de la órbita
     * @param radius       radio orbital en píxeles
     * @param startAngle   ángulo inicial en radianes
     * @param angularSpeed velocidad angular en rad/s (negativo = antihorario)
     */
    public OrbitalBullet(float cx, float cy, float radius,
                         float startAngle, float angularSpeed) {
        super(
            cx + (float) Math.cos(startAngle) * radius,
            cy + (float) Math.sin(startAngle) * radius,
            0f, 0f
        );
        this.cx           = cx;
        this.cy           = cy;
        this.radius       = radius;
        this.angle        = startAngle;
        this.angularSpeed = angularSpeed;
    }

    @Override
    public void update(float delta) {
        angle += angularSpeed * delta;
        position.x = cx + (float) Math.cos(angle) * radius;
        position.y = cy + (float) Math.sin(angle) * radius;
        hitbox.setPosition(position.x - HALF_SIZE, position.y - HALF_SIZE);
    }

    /** Nodo orbital: círculo índigo profundo con núcleo brillante. */
    @Override
    public void render(ShapeRenderer shapes) {
        shapes.setColor(0.15f, 0.25f, 0.80f, 1f);
        shapes.circle(position.x, position.y, 5f);
        shapes.setColor(0.70f, 0.85f, 1.00f, 1f);
        shapes.circle(position.x, position.y, 2.5f);
    }
}

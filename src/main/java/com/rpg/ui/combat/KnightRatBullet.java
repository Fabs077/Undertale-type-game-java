package com.rpg.ui.combat;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Bala tipo rata guerrera con estado TELEGRAPH antes de la embestida.
 * El hitbox permanece inactivo durante el telegraph; el Pattern dibuja
 * el sprite y los overlays de aviso.
 */
public class KnightRatBullet extends Bullet {

    private static final float HITBOX_W           = 56f;
    private static final float HITBOX_H           = 36f;
    static final         float TELEGRAPH_DURATION = 0.7f;

    private final float   intendedVx;
    private final boolean facingRight;
    private final float   trajectoryY;  // Y del corredor para dibujar el aviso
    private final float   corridorX;    // borde izquierdo del corredor de aviso
    private final float   corridorW;    // ancho del corredor de aviso

    private float   telegraphTimer;
    private boolean active;
    private float   stateTime;

    public KnightRatBullet(float x, float y, float vx,
                           float corridorX, float corridorW) {
        super(x, y, 0f, 0f);
        this.intendedVx     = vx;
        this.facingRight    = vx > 0f;
        this.trajectoryY    = y;
        this.corridorX      = corridorX;
        this.corridorW      = corridorW;
        this.telegraphTimer = TELEGRAPH_DURATION;
        this.active         = false;
        hitbox.set(-9999f, -9999f, 0f, 0f);
    }

    @Override
    public void update(float delta) {
        stateTime += delta;
        if (telegraphTimer > 0f) {
            telegraphTimer -= delta;
            if (telegraphTimer <= 0f) {
                velocity.set(intendedVx, 0f);
                active = true;
            }
        }
        super.update(delta);
        if (active) {
            hitbox.set(position.x - HITBOX_W / 2f, position.y - HITBOX_H / 2f, HITBOX_W, HITBOX_H);
        } else {
            hitbox.set(-9999f, -9999f, 0f, 0f);
        }
    }

    @Override
    public void render(ShapeRenderer shapes) { /* drawn as sprite by KnightRatPattern */ }

    public boolean isTelegraph()    { return telegraphTimer > 0f; }
    public boolean isActive()       { return active; }
    public float   getStateTime()   { return stateTime; }
    public boolean isFacingRight()  { return facingRight; }
    public float   getTrajectoryY() { return trajectoryY; }
    public float   getCorridorX()   { return corridorX; }
    public float   getCorridorW()   { return corridorW; }
}

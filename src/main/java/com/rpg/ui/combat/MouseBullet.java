package com.rpg.ui.combat;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Bullet rendered as the Kenny mouse sprite. Shape render is suppressed — the pattern draws it. */
public class MouseBullet extends Bullet {

    private static final float HITBOX_SIZE = 48f;

    private final float   intendedVx;
    private final boolean facingRight;
    private float         delayTimer;

    /**
     * @param startDelay seconds before the bullet starts moving (it appears stationary first)
     */
    public MouseBullet(float x, float y, float vx, float startDelay) {
        super(x, y, 0f, 0f);
        this.intendedVx  = vx;
        this.facingRight = vx > 0f;
        this.delayTimer  = startDelay;
    }

    @Override
    public void update(float delta) {
        if (delayTimer > 0f) {
            delayTimer -= delta;
            if (delayTimer <= 0f) velocity.set(intendedVx, 0f);
        }
        super.update(delta);
        // super.syncHitbox() resets to 8 px; override with the larger mouse hitbox
        hitbox.set(position.x - HITBOX_SIZE / 2f, position.y - HITBOX_SIZE / 2f, HITBOX_SIZE, HITBOX_SIZE);
    }

    @Override
    public void render(ShapeRenderer shapes) { /* drawn as sprite by KennyMousePattern */ }

    public boolean isFacingRight() { return facingRight; }
}

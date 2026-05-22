package com.rpg.ui.combat;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Moneda proyectil con rebote opcional contra las paredes de la arena. */
public class CoinBullet extends Bullet {

    public static final float W = 20f;

    private final float innerX, innerY, innerW, innerH;
    private final boolean bounceX, bounceY;
    private float stateTime;

    public CoinBullet(float x, float y, float vx, float vy,
                      float innerX, float innerY, float innerW, float innerH,
                      boolean bounceX, boolean bounceY) {
        super(x, y, vx, vy);
        this.innerX = innerX; this.innerY = innerY;
        this.innerW = innerW; this.innerH = innerH;
        this.bounceX = bounceX;
        this.bounceY = bounceY;
        syncCoinHitbox();
    }

    @Override
    public void update(float delta) {
        stateTime += delta;
        super.update(delta);

        if (bounceX) {
            if (position.x - W / 2f < innerX) {
                position.x = innerX + W / 2f;
                velocity.x = Math.abs(velocity.x);
            } else if (position.x + W / 2f > innerX + innerW) {
                position.x = innerX + innerW - W / 2f;
                velocity.x = -Math.abs(velocity.x);
            }
        }
        if (bounceY) {
            if (position.y - W / 2f < innerY) {
                position.y = innerY + W / 2f;
                velocity.y = Math.abs(velocity.y);
            } else if (position.y + W / 2f > innerY + innerH) {
                position.y = innerY + innerH - W / 2f;
                velocity.y = -Math.abs(velocity.y);
            }
        }

        syncCoinHitbox();
    }

    @Override
    public void render(ShapeRenderer shapes) { /* drawn as sprite by CoinPattern / CoinDropPattern */ }

    public float getStateTime() { return stateTime; }

    private void syncCoinHitbox() {
        hitbox.set(position.x - W / 2f, position.y - W / 2f, W, W);
    }
}

package com.rpg.ui.combat;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Bala tipo furgoneta que cruza la arena horizontalmente a la altura del suelo. */
public class VanBullet extends Bullet {

    public static final float W = 72f;
    public static final float H = 40f;

    private final boolean facingRight;
    private float stateTime;

    public VanBullet(float x, float y, float vx) {
        super(x, y, vx, 0f);
        this.facingRight = vx > 0f;
        hitbox.set(x - W / 2f, y - H / 2f, W, H);
    }

    @Override
    public void update(float delta) {
        stateTime += delta;
        super.update(delta);
        hitbox.set(position.x - W / 2f, position.y - H / 2f, W, H);
    }

    @Override
    public void render(ShapeRenderer shapes) { /* drawn as sprite by VanTrafficPattern */ }

    public boolean isFacingRight() { return facingRight; }
    public float   getStateTime()  { return stateTime; }
    public float   getW()          { return W; }
    public float   getH()          { return H; }
}

package com.rpg.ui.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Soul {

    private static final float VISUAL          = 16f;
    private static final float HITBOX          = 12f;
    private static final float SPEED           = 200f;
    private static final float I_FRAME_DURATION = 1.5f;  // seconds of invincibility after a hit

    private float x, y;
    private float invincibleTimer = 0f;
    public final Rectangle hitbox = new Rectangle();

    public Soul(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        syncHitbox();
    }

    public void update(float delta, CombatBox box) {
        if (invincibleTimer > 0f) invincibleTimer -= delta;

        if (Gdx.input.isKeyPressed(Keys.LEFT)  || Gdx.input.isKeyPressed(Keys.A)) x -= SPEED * delta;
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) x += SPEED * delta;
        if (Gdx.input.isKeyPressed(Keys.UP)    || Gdx.input.isKeyPressed(Keys.W)) y += SPEED * delta;
        if (Gdx.input.isKeyPressed(Keys.DOWN)  || Gdx.input.isKeyPressed(Keys.S)) y -= SPEED * delta;

        float half = VISUAL / 2f;
        x = MathUtils.clamp(x, box.getInnerX() + half, box.getInnerX() + box.getInnerWidth()  - half);
        y = MathUtils.clamp(y, box.getInnerY() + half, box.getInnerY() + box.getInnerHeight() - half);
        syncHitbox();
    }

    /** Starts invincibility frames — call immediately after registering a hit. */
    public void onHit() {
        invincibleTimer = I_FRAME_DURATION;
    }

    public boolean isInvincible() {
        return invincibleTimer > 0f;
    }

    /** Call inside a ShapeRenderer.begin(Filled)/end block. */
    public void render(ShapeRenderer shapes) {
        // Blink every 0.1 s during i-frames (skip render on even 0.1 s intervals)
        if (invincibleTimer > 0f && (int)(invincibleTimer * 10) % 2 == 0) return;
        shapes.setColor(Color.RED);
        shapes.rect(x - VISUAL / 2f, y - VISUAL / 2f, VISUAL, VISUAL);
    }

    private void syncHitbox() {
        hitbox.set(x - HITBOX / 2f, y - HITBOX / 2f, HITBOX, HITBOX);
    }
}

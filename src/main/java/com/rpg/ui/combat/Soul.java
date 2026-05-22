package com.rpg.ui.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Soul {

    public enum SoulMode { RED, BLUE }

    private static final float VISUAL           = 16f;
    private static final float HITBOX           = 12f;
    private static final float SPEED            = 200f;
    private static final float I_FRAME_DURATION = 1.5f;

    // Blue Soul physics
    private static final float GRAVITY    = 600f;
    private static final float JUMP_FORCE = 360f;

    private float x, y;
    private float velocityY      = 0f;
    private float invincibleTimer = 0f;
    private SoulMode mode        = SoulMode.RED;

    public final Rectangle hitbox = new Rectangle();

    public Soul(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        syncHitbox();
    }

    public void setMode(SoulMode mode) {
        this.mode      = mode;
        this.velocityY = 0f;
    }

    public void update(float delta, CombatBox box) {
        if (invincibleTimer > 0f) invincibleTimer -= delta;

        if (mode == SoulMode.RED) {
            updateRed(delta, box);
        } else {
            updateBlue(delta, box);
        }

        syncHitbox();
    }

    private void updateRed(float delta, CombatBox box) {
        if (Gdx.input.isKeyPressed(Keys.LEFT)  || Gdx.input.isKeyPressed(Keys.A)) x -= SPEED * delta;
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) x += SPEED * delta;
        if (Gdx.input.isKeyPressed(Keys.UP)    || Gdx.input.isKeyPressed(Keys.W)) y += SPEED * delta;
        if (Gdx.input.isKeyPressed(Keys.DOWN)  || Gdx.input.isKeyPressed(Keys.S)) y -= SPEED * delta;

        clamp(box);
    }

    private void updateBlue(float delta, CombatBox box) {
        // Horizontal movement
        if (Gdx.input.isKeyPressed(Keys.LEFT)  || Gdx.input.isKeyPressed(Keys.A)) x -= SPEED * delta;
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) x += SPEED * delta;

        // Gravity
        velocityY -= GRAVITY * delta;
        y         += velocityY * delta;

        // Ground collision: bottom wall of the inner box
        float groundCenterY = box.getInnerY() + VISUAL / 2f;
        if (y <= groundCenterY) {
            y         = groundCenterY;
            velocityY = 0f;
        }

        // Jump — only when grounded; uses justPressed so holding won't chain-jump
        boolean grounded = (y <= groundCenterY + 1f);
        if (grounded && (Gdx.input.isKeyJustPressed(Keys.UP)
                      || Gdx.input.isKeyJustPressed(Keys.W)
                      || Gdx.input.isKeyJustPressed(Keys.SPACE)
                      || Gdx.input.isKeyJustPressed(Keys.Z))) {
            velocityY = JUMP_FORCE;
        }

        // Ceiling clamp (no sticking)
        float ceilCenterY = box.getInnerY() + box.getInnerHeight() - VISUAL / 2f;
        if (y >= ceilCenterY) {
            y         = ceilCenterY;
            velocityY = Math.min(velocityY, 0f);
        }

        // Horizontal clamp
        float half = VISUAL / 2f;
        x = MathUtils.clamp(x, box.getInnerX() + half, box.getInnerX() + box.getInnerWidth() - half);
    }

    private void clamp(CombatBox box) {
        float half = VISUAL / 2f;
        x = MathUtils.clamp(x, box.getInnerX() + half, box.getInnerX() + box.getInnerWidth()  - half);
        y = MathUtils.clamp(y, box.getInnerY() + half, box.getInnerY() + box.getInnerHeight() - half);
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
        if (invincibleTimer > 0f && (int)(invincibleTimer * 10) % 2 == 0) return;
        shapes.setColor(mode == SoulMode.BLUE ? Color.BLUE : Color.RED);
        shapes.rect(x - VISUAL / 2f, y - VISUAL / 2f, VISUAL, VISUAL);
    }

    public SoulMode getMode() { return mode; }

    private void syncHitbox() {
        hitbox.set(x - HITBOX / 2f, y - HITBOX / 2f, HITBOX, HITBOX);
    }
}

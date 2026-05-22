package com.rpg.ui.combat;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Bala estática con forma de caja de cartón. El Pattern dibuja el sprite. */
public class CardboxBullet extends Bullet {

    private final int   variantFrame;
    private final float w, h;

    public CardboxBullet(float x, float y, float w, float h, int variantFrame) {
        super(x, y, 0f, 0f);
        this.w = w; this.h = h;
        this.variantFrame = variantFrame;
        hitbox.set(x - w / 2f, y - h / 2f, w, h);
    }

    @Override
    public void update(float delta) {
        // Bala estática — sin movimiento, el hitbox queda fijo desde el constructor
    }

    @Override
    public void render(ShapeRenderer shapes) { /* drawn as sprite by CardboxFieldPattern */ }

    public int   getVariant() { return variantFrame; }
    public float getW()       { return w; }
    public float getH()       { return h; }
}

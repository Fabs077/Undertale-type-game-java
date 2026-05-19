package com.rpg.ui.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Soul {

    private static final float VISUAL = 16f;
    private static final float HITBOX = 12f;
    private static final float SPEED  = 200f;

    private float x, y;
    public final Rectangle hitbox = new Rectangle();

    public Soul(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        syncHitbox();
    }

    public void update(float delta, CombatBox box) {
        if (Gdx.input.isKeyPressed(Keys.LEFT))  x -= SPEED * delta;
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) x += SPEED * delta;
        if (Gdx.input.isKeyPressed(Keys.UP))    y += SPEED * delta;
        if (Gdx.input.isKeyPressed(Keys.DOWN))  y -= SPEED * delta;

        float half = VISUAL / 2f;
        x = MathUtils.clamp(x, box.getInnerX() + half, box.getInnerX() + box.getInnerWidth()  - half);
        y = MathUtils.clamp(y, box.getInnerY() + half, box.getInnerY() + box.getInnerHeight() - half);
        syncHitbox();
    }

    /** Call inside a ShapeRenderer.begin(Filled)/end block. */
    public void render(ShapeRenderer shapes) {
        shapes.setColor(Color.RED);
        shapes.rect(x - VISUAL / 2f, y - VISUAL / 2f, VISUAL, VISUAL);
    }

    private void syncHitbox() {
        hitbox.set(x - HITBOX / 2f, y - HITBOX / 2f, HITBOX, HITBOX);
    }
}

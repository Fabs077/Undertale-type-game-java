package com.rpg.ui.combat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Bullet {

    private static final float SIZE = 8f;

    public final Vector2   position;
    public final Vector2   velocity;
    public final Rectangle hitbox = new Rectangle();

    public Bullet(float x, float y, float vx, float vy) {
        position = new Vector2(x, y);
        velocity = new Vector2(vx, vy);
        syncHitbox();
    }

    public void update(float delta) {
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        syncHitbox();
    }

    /** Call inside a ShapeRenderer.begin(Filled)/end block. */
    public void render(ShapeRenderer shapes) {
        shapes.setColor(Color.WHITE);
        shapes.rect(position.x - SIZE / 2f, position.y - SIZE / 2f, SIZE, SIZE);
    }

    private void syncHitbox() {
        hitbox.set(position.x - SIZE / 2f, position.y - SIZE / 2f, SIZE, SIZE);
    }
}

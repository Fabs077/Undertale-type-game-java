package com.rpg.ui.combat;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

public interface BulletPattern {
    void start(CombatBox box);
    void update(float delta);
    boolean isFinished();
    List<Bullet> getActiveBullets();

    /** Patterns that use sprite bullets override this to draw them. */
    default void renderSprites(SpriteBatch batch) {}

    /** Patterns that load textures override this to free them. */
    default void dispose() {}
}

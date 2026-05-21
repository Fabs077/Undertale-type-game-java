package com.rpg.ui.combat;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface BossRenderer {
    void update(float delta);
    void render(SpriteBatch batch, float x, float y, float w, float h);
    void play(String name);
    void playOnce(String name);
    boolean isLoaded();
    void dispose();
}

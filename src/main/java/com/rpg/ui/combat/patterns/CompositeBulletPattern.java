package com.rpg.ui.combat.patterns;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rpg.ui.combat.Bullet;
import com.rpg.ui.combat.BulletPattern;
import com.rpg.ui.combat.CombatBox;

import java.util.ArrayList;
import java.util.List;

/** Runs multiple BulletPatterns simultaneously, merging their bullet lists. */
public class CompositeBulletPattern implements BulletPattern {

    private final BulletPattern[] parts;

    public CompositeBulletPattern(BulletPattern... parts) {
        this.parts = parts;
    }

    @Override
    public void start(CombatBox box) {
        for (BulletPattern p : parts) p.start(box);
    }

    @Override
    public void update(float delta) {
        for (BulletPattern p : parts) p.update(delta);
    }

    @Override
    public boolean isFinished() {
        for (BulletPattern p : parts) if (!p.isFinished()) return false;
        return true;
    }

    @Override
    public List<Bullet> getActiveBullets() {
        List<Bullet> all = new ArrayList<>();
        for (BulletPattern p : parts) all.addAll(p.getActiveBullets());
        return all;
    }

    @Override
    public void renderShapes(ShapeRenderer shapes) {
        for (BulletPattern p : parts) p.renderShapes(shapes);
    }

    @Override
    public void renderSprites(SpriteBatch batch) {
        for (BulletPattern p : parts) p.renderSprites(batch);
    }

    @Override
    public void dispose() {
        for (BulletPattern p : parts) p.dispose();
    }
}

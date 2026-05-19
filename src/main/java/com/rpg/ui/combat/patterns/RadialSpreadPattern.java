package com.rpg.ui.combat.patterns;

import com.rpg.ui.combat.Bullet;
import com.rpg.ui.combat.BulletPattern;
import com.rpg.ui.combat.CombatBox;

import java.util.ArrayList;
import java.util.List;

public class RadialSpreadPattern implements BulletPattern {

    private static final int   BULLET_COUNT = 12;
    private static final float BULLET_SPEED = 130f;
    private static final float DURATION     = 5f;

    private final List<Bullet> bullets = new ArrayList<>();
    private float elapsed = 0f;

    @Override
    public void start(CombatBox box) {
        bullets.clear();
        elapsed = 0f;
        float cx = box.getInnerX() + box.getInnerWidth()  / 2f;
        float cy = box.getInnerY() + box.getInnerHeight() / 2f;
        for (int i = 0; i < BULLET_COUNT; i++) {
            double angle = 2 * Math.PI * i / BULLET_COUNT;
            float vx = (float)(Math.cos(angle) * BULLET_SPEED);
            float vy = (float)(Math.sin(angle) * BULLET_SPEED);
            bullets.add(new Bullet(cx, cy, vx, vy));
        }
    }

    @Override
    public void update(float delta) {
        elapsed += delta;
        for (Bullet b : bullets) b.update(delta);
    }

    @Override
    public boolean isFinished() { return elapsed >= DURATION; }

    @Override
    public List<Bullet> getActiveBullets() { return bullets; }
}

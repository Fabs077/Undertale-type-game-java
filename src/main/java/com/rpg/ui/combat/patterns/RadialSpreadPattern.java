package com.rpg.ui.combat.patterns;

import com.rpg.ui.combat.Bullet;
import com.rpg.ui.combat.BulletPattern;
import com.rpg.ui.combat.CombatBox;

import java.util.ArrayList;
import java.util.List;

public class RadialSpreadPattern implements BulletPattern {

    private static final int   BULLET_COUNT    = 12;
    private static final float BULLET_SPEED    = 130f;
    private static final float DURATION        = 5f;
    private static final float SPAWN_INTERVAL  = 0.3f;  // seconds between each bullet spawn

    private record SpawnData(float x, float y, float vx, float vy) {}

    private final List<Bullet>    bullets       = new ArrayList<>();
    private final List<SpawnData> pendingSpawns = new ArrayList<>();
    private float elapsed    = 0f;
    private float spawnTimer = 0f;

    // Inner-box bounds stored at start() for culling
    private float minX, minY, maxX, maxY;

    @Override
    public void start(CombatBox box) {
        bullets.clear();
        pendingSpawns.clear();
        elapsed    = 0f;
        spawnTimer = 0f;

        minX = box.getInnerX();
        minY = box.getInnerY();
        maxX = minX + box.getInnerWidth();
        maxY = minY + box.getInnerHeight();

        float cx = (minX + maxX) / 2f;
        float cy = (minY + maxY) / 2f;
        float hw = box.getInnerWidth()  / 2f;
        float hh = box.getInnerHeight() / 2f;

        for (int i = 0; i < BULLET_COUNT; i++) {
            double angle = 2 * Math.PI * i / BULLET_COUNT;
            float dx = (float) Math.cos(angle);
            float dy = (float) Math.sin(angle);

            // Find where this ray intersects the box edge, spawn there
            float tx = Math.abs(dx) > 1e-6f ? hw / Math.abs(dx) : Float.MAX_VALUE;
            float ty = Math.abs(dy) > 1e-6f ? hh / Math.abs(dy) : Float.MAX_VALUE;
            float t  = Math.min(tx, ty);

            // Velocity points inward toward center
            pendingSpawns.add(new SpawnData(cx + dx * t, cy + dy * t,
                                            -dx * BULLET_SPEED, -dy * BULLET_SPEED));
        }
    }

    @Override
    public void update(float delta) {
        elapsed    += delta;
        spawnTimer += delta;

        // Release one bullet every SPAWN_INTERVAL seconds
        while (spawnTimer >= SPAWN_INTERVAL && !pendingSpawns.isEmpty()) {
            SpawnData d = pendingSpawns.remove(0);
            bullets.add(new Bullet(d.x(), d.y(), d.vx(), d.vy()));
            spawnTimer -= SPAWN_INTERVAL;
        }

        for (Bullet b : bullets) b.update(delta);

        // Cull bullets that have left the box (travel through center and out the far edge)
        bullets.removeIf(b ->
            b.position.x < minX - 16 || b.position.x > maxX + 16 ||
            b.position.y < minY - 16 || b.position.y > maxY + 16
        );
    }

    @Override
    public boolean isFinished() { return elapsed >= DURATION; }

    @Override
    public List<Bullet> getActiveBullets() { return bullets; }
}

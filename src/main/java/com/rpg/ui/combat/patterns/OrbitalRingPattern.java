package com.rpg.ui.combat.patterns;

import com.rpg.ui.combat.Bullet;
import com.rpg.ui.combat.BulletPattern;
import com.rpg.ui.combat.CombatBox;
import com.rpg.ui.combat.OrbitalBullet;

import java.util.ArrayList;
import java.util.List;

/**
 * Patrón bullet-hell de Eclipse — cuatro anillos orbitales concéntricos.
 *
 * Los anillos aparecen escalonados para crear una curva de dificultad progresiva:
 *
 *   Anillo 1 (t= 0s): 8 balas, r=80,  ω=+0.9  rad/s  horario        — corona exterior
 *   Anillo 2 (t= 0s): 6 balas, r=52,  ω=−1.4  rad/s  antihorario   — anillo medio
 *   Anillo 3 (t= 5s): 4 balas, r=100, ω=+1.7  rad/s  horario       — órbita lejana
 *   Anillo 4 (t=10s): 5 balas, r=30,  ω=−2.0  rad/s  antihorario   — anillo interior rápido
 *
 * Con el modo WIDE (300×300, interior 292×292) todos los radios caben holgadamente
 * dentro de los 146 px de semiancho disponibles.
 *
 * Nota de diseño: las balas orbitales no salen de la arena, por lo que
 * CombatScreen limpia la lista hitBullets cuando una bala deja de solaparse
 * con el alma (ver updateBulletHell). Esto permite que el mismo objeto Bullet
 * vuelva a causar daño en órbitas siguientes una vez que los i-frames expiren.
 */
public class OrbitalRingPattern implements BulletPattern {

    private static final float DURATION = 12f;

    private record Ring(int n, float r, float omega, float t0) {}

    private static final Ring[] RINGS = {
        new Ring(8,  80f,  0.9f,  0f),
        new Ring(6,  52f, -1.4f,  0f),
        new Ring(4, 100f,  1.7f,  5f),
        new Ring(5,  30f, -2.0f, 10f),
    };

    private final List<Bullet>  bullets = new ArrayList<>();
    private final boolean[]     spawned = new boolean[RINGS.length];

    private float cx, cy;
    private float elapsed = 0f;

    @Override
    public void start(CombatBox box) {
        bullets.clear();
        elapsed = 0f;
        for (int i = 0; i < spawned.length; i++) spawned[i] = false;
        cx = box.getInnerX() + box.getInnerWidth()  / 2f;
        cy = box.getInnerY() + box.getInnerHeight() / 2f;
        checkSpawns();
    }

    @Override
    public void update(float delta) {
        elapsed += delta;
        checkSpawns();
        for (Bullet b : bullets) b.update(delta);
    }

    @Override
    public boolean isFinished() { return elapsed >= DURATION; }

    @Override
    public List<Bullet> getActiveBullets() { return bullets; }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void checkSpawns() {
        for (int i = 0; i < RINGS.length; i++) {
            if (!spawned[i] && elapsed >= RINGS[i].t0()) {
                spawnRing(RINGS[i]);
                spawned[i] = true;
            }
        }
    }

    private void spawnRing(Ring ring) {
        double step = 2 * Math.PI / ring.n();
        for (int j = 0; j < ring.n(); j++) {
            bullets.add(new OrbitalBullet(cx, cy, ring.r(), (float)(step * j), ring.omega()));
        }
    }
}

package com.rpg.ui.combat.patterns;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.rpg.ui.combat.Bullet;
import com.rpg.ui.combat.BulletPattern;
import com.rpg.ui.combat.CombatBox;
import com.rpg.ui.combat.MouseBullet;
import com.rpg.ui.combat.Soul;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Bullet-hell de Kenny: ratas que cruzan en línea recta a la altura del jugador.
 *
 * - Una rata cada 0.5 s durante 10 s.
 * - Sale del lado opuesto a donde está el jugador horizontalmente.
 * - Tarda 1.5 s en cruzar la caja.
 * - Cada rata aparece 0.3 s antes de empezar a moverse.
 * - 35 % de probabilidad de una segunda rata 0.3 s después, ligeramente debajo.
 */
public class KennyMousePattern implements BulletPattern {

    private static final String SPRITE_PATH    = "sprites/bullets/BulletKennyMouse/"
            + "pixel-art-illustration-mouse-toy-600nw-2457864779-removebg-preview.png";

    private static final float DURATION        = 10f;
    private static final float SPAWN_INTERVAL  = 0.5f;
    private static final float TRAVEL_TIME     = 1.5f;
    private static final float STARTUP_DELAY   = 0.3f;
    private static final float DOUBLE_DELAY    = 0.3f;
    private static final float Y_OFFSET        = 20f;
    private static final float DOUBLE_CHANCE   = 0.35f;
    private static final float VISUAL_SIZE     = 64f;

    private final Soul   soul;
    private final Random rng = new Random();

    // Bullet list typed as Bullet so getActiveBullets() doesn't need a cast copy
    private final List<Bullet>       bullets = new ArrayList<>();
    private final List<PendingSpawn> pending = new ArrayList<>();

    private Texture mouseTexture;
    private float   elapsed    = 0f;
    private float   spawnTimer = 0f;
    private float   innerX, innerW;

    private static final class PendingSpawn {
        float       countdown;
        final float y, startX, vx;
        PendingSpawn(float countdown, float y, float startX, float vx) {
            this.countdown = countdown; this.y = y; this.startX = startX; this.vx = vx;
        }
    }

    public KennyMousePattern(Soul soul) {
        this.soul = soul;
        if (Gdx.files.internal(SPRITE_PATH).exists()) {
            mouseTexture = new Texture(Gdx.files.internal(SPRITE_PATH));
        }
    }

    // ── BulletPattern ──────────────────────────────────────────────────────────

    @Override
    public void start(CombatBox box) {
        innerX = box.getInnerX();
        innerW = box.getInnerWidth();
        bullets.clear();
        pending.clear();
        elapsed    = 0f;
        spawnTimer = 0f;
    }

    @Override
    public void update(float delta) {
        elapsed    += delta;
        spawnTimer += delta;

        // Materialise any second-rat spawns whose countdown expired
        Iterator<PendingSpawn> it = pending.iterator();
        while (it.hasNext()) {
            PendingSpawn ps = it.next();
            ps.countdown -= delta;
            if (ps.countdown <= 0f) {
                bullets.add(new MouseBullet(ps.startX, ps.y, ps.vx, STARTUP_DELAY));
                it.remove();
            }
        }

        // Spawn rhythm — stops at DURATION so no new rats after 10 s
        while (spawnTimer >= SPAWN_INTERVAL && elapsed < DURATION) {
            spawnTimer -= SPAWN_INTERVAL;
            spawnRat();
        }

        for (Bullet b : bullets) b.update(delta);

        // Cull bullets that have fully exited the combat box
        bullets.removeIf(b ->
            b.position.x < innerX - 64 || b.position.x > innerX + innerW + 64);
    }

    @Override
    public boolean isFinished() {
        return elapsed >= DURATION && bullets.isEmpty() && pending.isEmpty();
    }

    @Override
    public List<Bullet> getActiveBullets() { return bullets; }

    @Override
    public void renderSprites(SpriteBatch batch) {
        if (mouseTexture == null) return;
        batch.begin();
        for (Bullet b : bullets) {
            if (!(b instanceof MouseBullet mb)) continue;
            float x = mb.position.x - VISUAL_SIZE / 2f;
            float y = mb.position.y - VISUAL_SIZE / 2f;
            if (mb.isFacingRight()) {
                batch.draw(mouseTexture, x, y, VISUAL_SIZE, VISUAL_SIZE);
            } else {
                // Flip horizontally: draw from right edge with negative width
                batch.draw(mouseTexture, x + VISUAL_SIZE, y, -VISUAL_SIZE, VISUAL_SIZE);
            }
        }
        batch.end();
    }

    @Override
    public void dispose() {
        if (mouseTexture != null) {
            mouseTexture.dispose();
            mouseTexture = null;
        }
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private void spawnRat() {
        float soulCX = soul.hitbox.x + soul.hitbox.width  / 2f;
        float soulCY = soul.hitbox.y + soul.hitbox.height / 2f;

        // Rat enters from the side opposite to the player's horizontal position
        boolean fromLeft = soulCX > innerX + innerW / 2f;
        float   startX   = fromLeft ? innerX - 16f : innerX + innerW + 16f;
        float   speed    = innerW / TRAVEL_TIME;
        float   vx       = fromLeft ? speed : -speed;

        bullets.add(new MouseBullet(startX, soulCY, vx, STARTUP_DELAY));

        // 35 % chance: schedule a second rat slightly below, 0.3 s later
        if (rng.nextFloat() < DOUBLE_CHANCE) {
            pending.add(new PendingSpawn(DOUBLE_DELAY, soulCY - Y_OFFSET, startX, vx));
        }
    }
}

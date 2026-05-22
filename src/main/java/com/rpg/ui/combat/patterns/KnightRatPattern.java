package com.rpg.ui.combat.patterns;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rpg.ui.combat.Bullet;
import com.rpg.ui.combat.BulletPattern;
import com.rpg.ui.combat.CombatBox;
import com.rpg.ui.combat.KnightRatBullet;
import com.rpg.ui.combat.Soul;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Bullet-hell de Kenny Fase 1: Knight Rats que cruzan la arena con un telegraph
 * visual (corredor amarillo) antes de arrancar.
 *
 * - Una rata cada 0.5 s durante 10 s.
 * - Sale del lado opuesto al jugador.
 * - 0.7 s de telegraph antes de moverse.
 * - 35 % de probabilidad de segunda rata 0.3 s después, ligeramente debajo.
 */
public class KnightRatPattern implements BulletPattern {

    private static final String SHEET_PATH     = "sprites/bullets/BulletKennyMouse/Knight Rat run.png";
    private static final float  DURATION       = 10f;
    private static final float  SPAWN_INTERVAL = 0.5f;
    private static final float  TRAVEL_TIME    = 1.5f;
    private static final float  DOUBLE_DELAY   = 0.3f;
    private static final float  Y_OFFSET       = 20f;
    private static final float  DOUBLE_CHANCE  = 0.35f;
    private static final float  VISUAL_W       = 64f;
    private static final float  VISUAL_H       = 48f;
    private static final float  FRAME_DURATION = 0.1f;
    private static final int    FRAMES_X       = 8;
    private static final float  TELEGRAPH_H    = 48f;

    private final Soul   soul;
    private final Random rng = new Random();

    private final List<Bullet>       bullets = new ArrayList<>();
    private final List<PendingSpawn> pending = new ArrayList<>();

    private Texture         sheet;
    private TextureRegion[] frames;
    private float innerX, innerW;
    private float elapsed = 0f, spawnTimer = 0f;

    private static final class PendingSpawn {
        float       countdown;
        final float y, startX, vx;
        PendingSpawn(float cd, float y, float sx, float vx) {
            this.countdown = cd; this.y = y; this.startX = sx; this.vx = vx;
        }
    }

    public KnightRatPattern(Soul soul) {
        this.soul = soul;
        if (Gdx.files.internal(SHEET_PATH).exists()) {
            sheet  = new Texture(Gdx.files.internal(SHEET_PATH));
            frames = TextureRegion.split(sheet, sheet.getWidth() / FRAMES_X, sheet.getHeight())[0];
        }
    }

    @Override
    public void start(CombatBox box) {
        innerX = box.getInnerX();
        innerW = box.getInnerWidth();
        bullets.clear(); pending.clear();
        elapsed = 0f; spawnTimer = 0f;
    }

    @Override
    public void update(float delta) {
        elapsed    += delta;
        spawnTimer += delta;

        Iterator<PendingSpawn> it = pending.iterator();
        while (it.hasNext()) {
            PendingSpawn ps = it.next();
            ps.countdown -= delta;
            if (ps.countdown <= 0f) {
                bullets.add(makeRat(ps.startX, ps.y, ps.vx));
                it.remove();
            }
        }

        while (spawnTimer >= SPAWN_INTERVAL && elapsed < DURATION) {
            spawnTimer -= SPAWN_INTERVAL;
            spawnRat();
        }

        for (Bullet b : bullets) b.update(delta);
        bullets.removeIf(b -> b.position.x < innerX - 96 || b.position.x > innerX + innerW + 96);
    }

    @Override
    public boolean isFinished() {
        return elapsed >= DURATION && bullets.isEmpty() && pending.isEmpty();
    }

    @Override
    public List<Bullet> getActiveBullets() { return bullets; }

    @Override
    public void renderShapes(ShapeRenderer shapes) {
        for (Bullet b : bullets) {
            if (!(b instanceof KnightRatBullet rat) || !rat.isTelegraph()) continue;
            shapes.setColor(1f, 0.9f, 0.2f, 0.35f);
            shapes.rect(rat.getCorridorX(), rat.getTrajectoryY() - TELEGRAPH_H / 2f,
                        rat.getCorridorW(), TELEGRAPH_H);
        }
    }

    @Override
    public void renderSprites(SpriteBatch batch) {
        if (frames == null) return;
        batch.begin();
        for (Bullet b : bullets) {
            if (!(b instanceof KnightRatBullet rat)) continue;
            int idx   = (int)(rat.getStateTime() / FRAME_DURATION) % frames.length;
            TextureRegion fr = frames[idx];
            float drawW = rat.isFacingRight() ?  VISUAL_W : -VISUAL_W;
            float drawX = rat.isFacingRight()
                ? rat.position.x - VISUAL_W / 2f
                : rat.position.x + VISUAL_W / 2f;
            batch.draw(fr, drawX, rat.position.y - VISUAL_H / 2f, drawW, VISUAL_H);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        if (sheet != null) { sheet.dispose(); sheet = null; }
        frames = null;
    }

    private void spawnRat() {
        float soulCX = soul.hitbox.x + soul.hitbox.width  / 2f;
        float soulCY = soul.hitbox.y + soul.hitbox.height / 2f;

        boolean fromLeft = soulCX > innerX + innerW / 2f;
        float   startX   = fromLeft ? innerX - 16f : innerX + innerW + 16f;
        float   speed    = innerW / TRAVEL_TIME;
        float   vx       = fromLeft ? speed : -speed;

        bullets.add(makeRat(startX, soulCY, vx));

        if (rng.nextFloat() < DOUBLE_CHANCE) {
            pending.add(new PendingSpawn(DOUBLE_DELAY, soulCY - Y_OFFSET, startX, vx));
        }
    }

    private KnightRatBullet makeRat(float startX, float y, float vx) {
        return new KnightRatBullet(startX, y, vx, innerX, innerW);
    }
}

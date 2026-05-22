package com.rpg.ui.combat.patterns;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.rpg.ui.combat.Bullet;
import com.rpg.ui.combat.BulletPattern;
import com.rpg.ui.combat.CoinBullet;
import com.rpg.ui.combat.CombatBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Monedas que rebotan en las paredes (fase sin gravedad).
 * Cada moneda entra desde un borde aleatorio con trayectoria diagonal.
 */
public class CoinPattern implements BulletPattern {

    private static final String[] FRAME_PATHS = {
        "sprites/bullets/BulletKennyCoin/goldcoin-frame1.png",
        "sprites/bullets/BulletKennyCoin/goldcoin-frame2.png",
        "sprites/bullets/BulletKennyCoin/goldcoin-frame3.png",
        "sprites/bullets/BulletKennyCoin/goldcoin-frame4.png",
        "sprites/bullets/BulletKennyCoin/goldcoin-frame5.png",
        "sprites/bullets/BulletKennyCoin/goldcoin-frame6.png",
    };
    private static final float FRAME_DUR = 0.1f;
    private static final float DURATION  = 10f;
    private static final float SPAWN_INT = 0.7f;
    private static final float SPEED     = 160f;

    private final List<Bullet> bullets = new ArrayList<>();
    private final Random       rng     = new Random();
    private final Texture[]    frames;

    private float innerX, innerY, innerW, innerH;
    private float elapsed, spawnTimer;

    public CoinPattern() {
        Texture[] loaded = new Texture[FRAME_PATHS.length];
        for (int i = 0; i < FRAME_PATHS.length; i++) {
            if (Gdx.files.internal(FRAME_PATHS[i]).exists())
                loaded[i] = new Texture(Gdx.files.internal(FRAME_PATHS[i]));
        }
        frames = loaded;
    }

    @Override
    public void start(CombatBox box) {
        innerX = box.getInnerX(); innerY = box.getInnerY();
        innerW = box.getInnerWidth(); innerH = box.getInnerHeight();
        bullets.clear();
        elapsed = 0f; spawnTimer = 0f;
    }

    @Override
    public void update(float delta) {
        elapsed    += delta;
        spawnTimer += delta;
        while (spawnTimer >= SPAWN_INT && elapsed < DURATION) {
            spawnTimer -= SPAWN_INT;
            spawnCoin();
        }
        for (Bullet b : bullets) b.update(delta);
    }

    @Override
    public boolean isFinished() { return elapsed >= DURATION; }

    @Override
    public List<Bullet> getActiveBullets() { return bullets; }

    @Override
    public void renderSprites(SpriteBatch batch) {
        if (!hasFrames()) return;
        batch.begin();
        for (Bullet b : bullets) {
            if (!(b instanceof CoinBullet coin)) continue;
            int idx = (int)(coin.getStateTime() / FRAME_DUR) % frames.length;
            if (frames[idx] == null) continue;
            batch.draw(frames[idx],
                coin.position.x - CoinBullet.W / 2f,
                coin.position.y - CoinBullet.W / 2f,
                CoinBullet.W, CoinBullet.W);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        for (Texture t : frames) if (t != null) t.dispose();
    }

    private boolean hasFrames() {
        for (Texture f : frames) if (f != null) return true;
        return false;
    }

    private void spawnCoin() {
        float half = CoinBullet.W / 2f;
        float diag = SPEED * 0.707f;
        float x, y, vx, vy;
        switch (rng.nextInt(4)) {
            case 0 -> { // from bottom edge
                x = innerX + half + rng.nextFloat() * (innerW - CoinBullet.W);
                y = innerY + half;
                vx = (rng.nextBoolean() ? 1 : -1) * diag; vy = diag;
            }
            case 1 -> { // from top edge
                x = innerX + half + rng.nextFloat() * (innerW - CoinBullet.W);
                y = innerY + innerH - half;
                vx = (rng.nextBoolean() ? 1 : -1) * diag; vy = -diag;
            }
            case 2 -> { // from left edge
                x = innerX + half;
                y = innerY + half + rng.nextFloat() * (innerH - CoinBullet.W);
                vx = diag; vy = (rng.nextBoolean() ? 1 : -1) * diag;
            }
            default -> { // from right edge
                x = innerX + innerW - half;
                y = innerY + half + rng.nextFloat() * (innerH - CoinBullet.W);
                vx = -diag; vy = (rng.nextBoolean() ? 1 : -1) * diag;
            }
        }
        bullets.add(new CoinBullet(x, y, vx, vy, innerX, innerY, innerW, innerH, true, true));
    }
}

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
 * Monedas que caen desde el techo (fase con gravedad/salto).
 * El jugador debe esquivarlas moviéndose horizontalmente o saltando.
 */
public class CoinDropPattern implements BulletPattern {

    private static final String[] FRAME_PATHS = {
        "sprites/bullets/BulletKennyCoin/goldcoin-frame1.png",
        "sprites/bullets/BulletKennyCoin/goldcoin-frame2.png",
        "sprites/bullets/BulletKennyCoin/goldcoin-frame3.png",
        "sprites/bullets/BulletKennyCoin/goldcoin-frame4.png",
        "sprites/bullets/BulletKennyCoin/goldcoin-frame5.png",
        "sprites/bullets/BulletKennyCoin/goldcoin-frame6.png",
    };
    private static final float FRAME_DUR  = 0.1f;
    private static final float DURATION   = 10f;
    private static final float SPAWN_INT  = 0.55f;
    private static final float DROP_SPEED = 220f;

    private final List<Bullet> bullets = new ArrayList<>();
    private final Random       rng     = new Random();
    private final Texture[]    frames;

    private float innerX, innerY, innerW, innerH;
    private float elapsed, spawnTimer;

    public CoinDropPattern() {
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
        bullets.removeIf(b -> b.position.y + CoinBullet.W / 2f < innerY - 16f);
    }

    @Override
    public boolean isFinished() { return elapsed >= DURATION && bullets.isEmpty(); }

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
        float x = innerX + half + rng.nextFloat() * (innerW - CoinBullet.W);
        float y = innerY + innerH + CoinBullet.W;  // spawn just above the box ceiling
        // Falls straight down, no bounce
        bullets.add(new CoinBullet(x, y, 0f, -DROP_SPEED, innerX, innerY, innerW, innerH, false, false));
    }
}

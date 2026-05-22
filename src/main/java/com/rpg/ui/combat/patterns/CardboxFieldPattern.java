package com.rpg.ui.combat.patterns;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rpg.ui.combat.Bullet;
import com.rpg.ui.combat.BulletPattern;
import com.rpg.ui.combat.CardboxBullet;
import com.rpg.ui.combat.CombatBox;
import com.rpg.ui.combat.Soul;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Patrón de campo de cajas: coloca N cajas estáticas dentro de la arena
 * con separación anti-solapamiento y margen de seguridad desde el alma.
 *
 * - 4–7 cajas en posiciones aleatorias no solapadas.
 * - Duración: 12 s.
 * - Solo aplica daño al entrar en la caja (i-frames de Soul evitan daño continuo).
 */
public class CardboxFieldPattern implements BulletPattern {

    private static final String SHEET_PATH    = "sprites/bullets/CardboardBoxKenny/cardbox_non_outlined.png";
    private static final float  DURATION      = 12f;
    private static final int    BOX_COUNT_MIN = 4;
    private static final int    BOX_COUNT_MAX = 7;
    private static final float  BOX_W         = 40f;
    private static final float  BOX_H         = 40f;
    private static final float  SOUL_MARGIN   = 24f;  // radio de seguridad alrededor del alma
    private static final float  BOX_MARGIN    = 8f;   // separación mínima entre cajas

    private final Soul   soul;
    private final Random rng = new Random();

    private final List<Bullet> bullets = new ArrayList<>();

    private Texture         sheet;
    private TextureRegion[] variants;
    private int             framesPerRow;
    private float innerX, innerY, innerW, innerH;
    private float elapsed = 0f;

    public CardboxFieldPattern(Soul soul) {
        this.soul = soul;
        if (Gdx.files.internal(SHEET_PATH).exists()) {
            sheet = new Texture(Gdx.files.internal(SHEET_PATH));
            // Frames cuadrados: columnas = ancho / alto
            framesPerRow = Math.max(1, sheet.getWidth() / sheet.getHeight());
            TextureRegion[][] grid = TextureRegion.split(
                sheet, sheet.getWidth() / framesPerRow, sheet.getHeight());
            variants = grid[0];
        }
    }

    @Override
    public void start(CombatBox box) {
        innerX = box.getInnerX(); innerY = box.getInnerY();
        innerW = box.getInnerWidth(); innerH = box.getInnerHeight();
        bullets.clear();
        elapsed = 0f;

        float soulCX = soul.hitbox.x + soul.hitbox.width  / 2f;
        float soulCY = soul.hitbox.y + soul.hitbox.height / 2f;

        int target   = BOX_COUNT_MIN + rng.nextInt(BOX_COUNT_MAX - BOX_COUNT_MIN + 1);
        int attempts = 0;
        while (bullets.size() < target && attempts < 200) {
            attempts++;
            float cx = innerX + BOX_W / 2f + rng.nextFloat() * (innerW - BOX_W);
            float cy = innerY + BOX_H / 2f + rng.nextFloat() * (innerH - BOX_H);

            if (Math.abs(cx - soulCX) < SOUL_MARGIN && Math.abs(cy - soulCY) < SOUL_MARGIN) continue;
            if (outOfBounds(cx, cy)) continue;
            if (overlapsExisting(cx, cy)) continue;

            int variant = (framesPerRow > 0) ? rng.nextInt(framesPerRow) : 0;
            bullets.add(new CardboxBullet(cx, cy, BOX_W, BOX_H, variant));
        }
    }

    @Override
    public void update(float delta) {
        elapsed += delta;
        // Cajas estáticas — no requieren update de posición
    }

    @Override
    public boolean isFinished() { return elapsed >= DURATION; }

    @Override
    public List<Bullet> getActiveBullets() { return bullets; }

    @Override
    public void renderSprites(SpriteBatch batch) {
        if (variants == null) return;
        batch.begin();
        for (Bullet b : bullets) {
            if (!(b instanceof CardboxBullet box)) continue;
            int variant = Math.min(box.getVariant(), variants.length - 1);
            batch.draw(variants[variant],
                box.position.x - box.getW() / 2f,
                box.position.y - box.getH() / 2f,
                box.getW(), box.getH());
        }
        batch.end();
    }

    @Override
    public void dispose() {
        if (sheet != null) { sheet.dispose(); sheet = null; }
        variants = null;
    }

    private boolean outOfBounds(float cx, float cy) {
        return cx - BOX_W / 2f < innerX || cx + BOX_W / 2f > innerX + innerW
            || cy - BOX_H / 2f < innerY || cy + BOX_H / 2f > innerY + innerH;
    }

    private boolean overlapsExisting(float cx, float cy) {
        for (Bullet b : bullets) {
            if (Math.abs(b.position.x - cx) < BOX_W + BOX_MARGIN
             && Math.abs(b.position.y - cy) < BOX_H + BOX_MARGIN) return true;
        }
        return false;
    }
}

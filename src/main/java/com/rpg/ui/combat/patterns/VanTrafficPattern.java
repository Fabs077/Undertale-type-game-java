package com.rpg.ui.combat.patterns;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rpg.ui.combat.Bullet;
import com.rpg.ui.combat.BulletPattern;
import com.rpg.ui.combat.CombatBox;
import com.rpg.ui.combat.VanBullet;

import java.util.ArrayList;
import java.util.List;

/**
 * Patrón de tráfico de furgonetas para Fase 2 (alma con gravedad/salto).
 * Las furgonetas cruzan horizontalmente a la altura del suelo; el alma debe saltar.
 *
 * El guion de apariciones es fijo (no aleatorio) para diseño de niveles determinista.
 * Prerequisito: Soul debe tener modo PLATFORMER antes de usar este patrón.
 */
public class VanTrafficPattern implements BulletPattern {

    private static final String SHEET_PATH     = "sprites/bullets/BulletBusKenny/VanBounceRoll-Sheet.png";
    private static final float  DURATION       = 10f;
    private static final float  FRAME_DURATION = 0.12f;
    private static final int    COLS           = 4;
    private static final int    ROWS           = 3;
    private static final int    ROLLING_ROW    = 1;  // fila del sheet para estado "rolling"

    private record Spawn(float t, float speed, boolean fromLeft) {}

    private static final Spawn[] SCRIPT = {
        new Spawn(0.5f, 180f, true),
        new Spawn(1.2f, 260f, true),
        new Spawn(2.5f, 200f, false),
        new Spawn(3.5f, 220f, false),
        new Spawn(3.8f, 320f, false),
        new Spawn(5.0f, 180f, true),
        new Spawn(6.0f, 240f, true),
        new Spawn(6.5f, 300f, false),
        new Spawn(7.5f, 200f, true),
        new Spawn(8.0f, 260f, false),
    };

    private final List<Bullet> bullets = new ArrayList<>();

    private Texture         sheet;
    private TextureRegion[] rollingFrames;
    private float innerX, innerW;
    private float groundY;
    private float elapsed     = 0f;
    private int   scriptIndex = 0;

    public VanTrafficPattern() {
        if (Gdx.files.internal(SHEET_PATH).exists()) {
            sheet = new Texture(Gdx.files.internal(SHEET_PATH));
            TextureRegion[][] grid = TextureRegion.split(
                sheet, sheet.getWidth() / COLS, sheet.getHeight() / ROWS);
            rollingFrames = grid[ROLLING_ROW];
        }
    }

    @Override
    public void start(CombatBox box) {
        innerX  = box.getInnerX();
        innerW  = box.getInnerWidth();
        groundY = box.getInnerY() + VanBullet.H / 2f;  // van bottom flush with inner floor
        bullets.clear();
        elapsed     = 0f;
        scriptIndex = 0;
    }

    @Override
    public void update(float delta) {
        elapsed += delta;

        while (scriptIndex < SCRIPT.length && elapsed >= SCRIPT[scriptIndex].t()) {
            Spawn s      = SCRIPT[scriptIndex++];
            float vx     = s.fromLeft() ? s.speed() : -s.speed();
            float startX = s.fromLeft() ? innerX - VanBullet.W : innerX + innerW + VanBullet.W;
            bullets.add(new VanBullet(startX, groundY, vx));
        }

        for (Bullet b : bullets) b.update(delta);
        bullets.removeIf(b -> b.position.x < innerX - 128 || b.position.x > innerX + innerW + 128);
    }

    @Override
    public boolean isFinished() {
        return elapsed >= DURATION && bullets.isEmpty();
    }

    @Override
    public List<Bullet> getActiveBullets() { return bullets; }

    @Override
    public void renderSprites(SpriteBatch batch) {
        if (rollingFrames == null) return;
        batch.begin();
        for (Bullet b : bullets) {
            if (!(b instanceof VanBullet van)) continue;
            int idx   = (int)(van.getStateTime() / FRAME_DURATION) % rollingFrames.length;
            TextureRegion fr = rollingFrames[idx];
            float drawW = van.isFacingRight() ?  van.getW() : -van.getW();
            float drawX = van.isFacingRight()
                ? van.position.x - van.getW() / 2f
                : van.position.x + van.getW() / 2f;
            batch.draw(fr, drawX, van.position.y - van.getH() / 2f, drawW, van.getH());
        }
        batch.end();
    }

    @Override
    public void dispose() {
        if (sheet != null) { sheet.dispose(); sheet = null; }
        rollingFrames = null;
    }
}

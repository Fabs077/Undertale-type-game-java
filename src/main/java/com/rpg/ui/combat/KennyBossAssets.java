package com.rpg.ui.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Renderiza a BossKenny como dos capas superpuestas: cuerpo (6 frames) + cabeza (8 frames).
 *
 * BodyKenny.PNG — 6 frames horizontales:
 *   0  idle           1,2  attack (brazos)     3,4  hurt (daño/slash)    5  hurt-alt
 *
 * HeadKenny.PNG — 8 frames horizontales:
 *   0  neutral        1  blink (ojos cerrados)
 *   2  enojado        3  confiado
 *   4  ojos-X #1      5  excitado     6  smirk     7  ojos-X #2
 *
 * Secuencias de animación:
 *   idle   → body[0]           head[0,1,0,1]       (blink en loop)
 *   attack → body[1,2,0]       head[2,5,3,0]       (one-shot)
 *   hurt   → body[3,4,0]       head[4,7,0]         (one-shot)
 *
 * Ajuste visual: HEAD_SCALE y HEAD_Y_CENTER controlan tamaño y posición de la cabeza
 * relativo al rect de render del cuerpo. Tunear si las proporciones no quedan bien.
 */
public class KennyBossAssets implements BossRenderer {

    private static final String BASE_PATH  = "sprites/bosses/BossKenny/";
    private static final String BODY_FILE  = BASE_PATH + "BodyKenny-removebg-preview.png";
    private static final String HEAD_FILE  = BASE_PATH + "HeadKenny-removebg-preview.png";

    private static final int BODY_FRAMES = 6;
    private static final int HEAD_FRAMES = 8;

    // Proporción cabeza respecto al ancho del rect del cuerpo (0.0–1.0)
    private static final float HEAD_SCALE    = 0.68f;
    // Centro-Y de la cabeza como fracción de la altura del rect (0=abajo, 1=arriba)
    private static final float HEAD_Y_CENTER = 0.82f;

    // Secuencias: índices de frame para cada animación
    private static final int[] BODY_IDLE   = {0};
    private static final int[] BODY_ATTACK = {1, 2, 0};
    private static final int[] BODY_HURT   = {3, 4, 0};

    private static final int[] HEAD_IDLE   = {0, 1, 0, 1};
    private static final int[] HEAD_ATTACK = {2, 5, 3, 0};
    private static final int[] HEAD_HURT   = {4, 7, 0};

    private final Texture         bodyTex;
    private final Texture         headTex;
    private final TextureRegion[] bodyFrames;
    private final TextureRegion[] headFrames;
    private final float           frameDuration;

    private String  currentAnim = "idle";
    private float   stateTime   = 0f;
    private boolean playingOnce = false;
    private boolean loaded      = false;

    /**
     * @param frameDuration segundos por frame (ej. 0.15f)
     */
    public KennyBossAssets(float frameDuration) {
        this.frameDuration = frameDuration;

        if (!Gdx.files.internal(BODY_FILE).exists() || !Gdx.files.internal(HEAD_FILE).exists()) {
            bodyTex    = null;
            headTex    = null;
            bodyFrames = null;
            headFrames = null;
            return;
        }

        bodyTex    = new Texture(Gdx.files.internal(BODY_FILE));
        headTex    = new Texture(Gdx.files.internal(HEAD_FILE));
        bodyFrames = TextureRegion.split(bodyTex, bodyTex.getWidth() / BODY_FRAMES, bodyTex.getHeight())[0];
        headFrames = TextureRegion.split(headTex, headTex.getWidth() / HEAD_FRAMES, headTex.getHeight())[0];
        loaded     = true;
    }

    // ── ciclo de vida ──────────────────────────────────────────────────────────

    @Override
    public void update(float delta) {
        if (!loaded) return;
        stateTime += delta;
        if (playingOnce) {
            int duration = animDuration(currentAnim);
            if (stateTime >= frameDuration * duration) {
                currentAnim = "idle";
                stateTime   = 0f;
                playingOnce = false;
            }
        }
    }

    @Override
    public void render(SpriteBatch batch, float x, float y, float w, float h) {
        if (!loaded) return;

        TextureRegion bodyFrame = bodyFrames[frameIndex(bodySeq(currentAnim))];
        TextureRegion headFrame = headFrames[frameIndex(headSeq(currentAnim))];

        float headW = w * HEAD_SCALE;
        float headH = headW * ((float) headFrame.getRegionHeight() / headFrame.getRegionWidth());
        float headX = x + (w - headW) / 2f;
        float headY = y + h * HEAD_Y_CENTER - headH / 2f;

        batch.begin();
        batch.draw(bodyFrame, x, y, w, h);
        batch.draw(headFrame, headX, headY, headW, headH);
        batch.end();
    }

    // ── control de animaciones ─────────────────────────────────────────────────

    @Override
    public void play(String name) {
        if (currentAnim.equals(name)) return;
        currentAnim = name;
        stateTime   = 0f;
        playingOnce = false;
    }

    @Override
    public void playOnce(String name) {
        currentAnim = name;
        stateTime   = 0f;
        playingOnce = true;
    }

    @Override
    public boolean isLoaded() { return loaded; }

    @Override
    public void dispose() {
        if (bodyTex != null) bodyTex.dispose();
        if (headTex != null) headTex.dispose();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private int frameIndex(int[] seq) {
        int i = (int)(stateTime / frameDuration);
        // en one-shot: clamp al último frame; en loop: módulo
        return playingOnce ? seq[Math.min(i, seq.length - 1)] : seq[i % seq.length];
    }

    private int animDuration(String anim) {
        return Math.max(bodySeq(anim).length, headSeq(anim).length);
    }

    private int[] bodySeq(String anim) {
        return switch (anim) {
            case "attack" -> BODY_ATTACK;
            case "hurt"   -> BODY_HURT;
            default       -> BODY_IDLE;
        };
    }

    private int[] headSeq(String anim) {
        return switch (anim) {
            case "attack" -> HEAD_ATTACK;
            case "hurt"   -> HEAD_HURT;
            default       -> HEAD_IDLE;
        };
    }

}

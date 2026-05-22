package com.rpg.ui.combat;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Renderer de Eclipse — esfera de vacío cósmico con corona de plasma.
 *
 * Genera todas sus texturas proceduralmente usando Pixmap (sin archivos externos).
 * Cada frame es una esfera oscura con un halo que varía en tamaño e intensidad:
 *
 *   idle    (4 frames): pulso suave del halo, loop continuo.
 *   attack  (4 frames): el halo explota hacia afuera y se retrae bruscamente.
 *   hurt    (3 frames): destello blanco, luego retorno a estado base.
 *
 * El filtro LINEAR en todas las texturas suaviza el upscale 64→200 px.
 */
public class EclipseAssets implements BossRenderer {

    private static final int   SZ     = 64;
    private static final int   CX     = SZ / 2;
    private static final int   CY     = SZ / 2;
    private static final int   BODY_R = 24;

    private final List<Texture> all = new ArrayList<>();
    private final Texture[]     idleFrames;
    private final Texture[]     attackFrames;
    private final Texture[]     hurtFrames;
    private final float         frameDuration;

    private String  current     = "idle";
    private float   stateTime   = 0f;
    private boolean playingOnce = false;

    public EclipseAssets(float frameDuration) {
        this.frameDuration = frameDuration;

        idleFrames = reg(
            voidFrame(6, 0.55f),
            voidFrame(7, 0.65f),
            voidFrame(8, 0.75f),
            voidFrame(7, 0.65f)
        );
        attackFrames = reg(
            voidFrame(10, 0.80f),
            voidFrame(14, 0.95f),
            voidFrame(18, 1.00f),
            voidFrame( 7, 0.50f)
        );
        hurtFrames = reg(
            whiteFlash(),
            voidFrame(8, 0.45f),
            voidFrame(6, 0.55f)
        );
    }

    // ── BossRenderer ──────────────────────────────────────────────────────────

    @Override
    public void update(float delta) {
        stateTime += delta;
        if (playingOnce && stateTime >= frameDuration * seq().length) {
            current     = "idle";
            stateTime   = 0f;
            playingOnce = false;
        }
    }

    @Override
    public void render(SpriteBatch batch, float x, float y, float w, float h) {
        Texture[] frames = seq();
        int idx = playingOnce
            ? Math.min((int)(stateTime / frameDuration), frames.length - 1)
            : (int)(stateTime / frameDuration) % frames.length;
        batch.begin();
        batch.draw(frames[idx], x, y, w, h);
        batch.end();
    }

    @Override
    public void play(String name) {
        if (current.equals(name)) return;
        current = name; stateTime = 0f; playingOnce = false;
    }

    @Override
    public void playOnce(String name) {
        current = name; stateTime = 0f; playingOnce = true;
    }

    @Override public boolean isLoaded() { return true; }

    @Override
    public void dispose() { all.forEach(Texture::dispose); }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Texture[] seq() {
        return switch (current) {
            case "attack" -> attackFrames;
            case "hurt"   -> hurtFrames;
            default       -> idleFrames;
        };
    }

    private Texture[] reg(Texture... ts) {
        for (Texture t : ts) all.add(t);
        return ts;
    }

    /**
     * Esfera oscura con corona exterior.
     *
     * @param glowExtent píxeles de radio que extiende el halo más allá del cuerpo
     * @param glowAlpha  intensidad máxima del halo (0–1)
     */
    private static Texture voidFrame(int glowExtent, float glowAlpha) {
        Pixmap pm = new Pixmap(SZ, SZ, Pixmap.Format.RGBA8888);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();

        // corona exterior: anillos concéntricos con caída cuadrática de alpha
        for (int r = BODY_R + glowExtent; r > BODY_R; r--) {
            float t = 1f - (float)(r - BODY_R) / glowExtent;
            float a = glowAlpha * t * t;
            pm.setColor(0.20f + t * 0.20f, 0.35f + t * 0.35f, 0.75f + t * 0.20f, a);
            pm.drawCircle(CX, CY, r);
        }

        // cuerpo del vacío — negro-morado profundo
        pm.setColor(0.04f, 0.02f, 0.11f, 1f);
        pm.fillCircle(CX, CY, BODY_R);

        // borde luminoso (corona de plasma en el límite del cuerpo)
        pm.setColor(0.55f, 0.78f, 1.00f, glowAlpha);
        pm.drawCircle(CX, CY, BODY_R);
        pm.drawCircle(CX, CY, BODY_R - 1);

        // anillo interior secundario (eco del borde)
        pm.setColor(0.28f, 0.48f, 0.90f, glowAlpha * 0.50f);
        pm.drawCircle(CX, CY, BODY_R - 6);

        // estrellas fijas dentro del vacío
        pm.setColor(1f, 1f, 1f, 0.80f);
        pm.drawPixel(CX - 8, CY + 5);
        pm.drawPixel(CX + 10, CY - 4);
        pm.drawPixel(CX - 5,  CY - 13);
        pm.drawPixel(CX + 6,  CY + 9);
        pm.drawPixel(CX - 13, CY - 6);
        pm.drawPixel(CX + 3,  CY - 8);

        Texture tex = new Texture(pm);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return tex;
    }

    /** Destello blanco: primer frame de la animación "hurt". */
    private static Texture whiteFlash() {
        Pixmap pm = new Pixmap(SZ, SZ, Pixmap.Format.RGBA8888);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();

        pm.setColor(1f, 1f, 1f, 0.93f);
        pm.fillCircle(CX, CY, BODY_R + 3);

        // corona oscura todavía visible en el borde durante el destello
        pm.setColor(0.10f, 0.05f, 0.30f, 1f);
        pm.drawCircle(CX, CY, BODY_R + 3);
        pm.drawCircle(CX, CY, BODY_R + 2);

        Texture tex = new Texture(pm);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return tex;
    }
}
